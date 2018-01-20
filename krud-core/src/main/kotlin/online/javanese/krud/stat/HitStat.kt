package online.javanese.krud.stat

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.ApplicationRequest
import io.ktor.request.header
import io.ktor.request.uri
import io.ktor.request.userAgent
import io.ktor.response.respondText
import io.ktor.response.respondWrite
import kotlinx.html.*
import online.javanese.krud.HttpRequest
import online.javanese.krud.Module
import online.javanese.krud.WebEnv
import online.javanese.krud.WsRequest
import online.javanese.krud.template.Content
import online.javanese.krud.template.Link
import java.time.LocalDateTime
import java.util.Collections.unmodifiableList
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.CopyOnWriteArrayList


class HitStat(
        private val statTable: StatTable,
        private val remoteAddr: (ApplicationRequest) -> String = GetRemoteAddr,
        private val isBot: (userAgent: String) -> Boolean = IsBot,
        private val ignoreRequestUri: (String) -> Boolean = IgnoreRequestUri
) : Module {

    override val name: String get() = "Hits"

    override suspend fun summary(env: WebEnv): Content =
            statOverview()

    override suspend fun http(env: WebEnv, call: ApplicationCall, httpRequest: HttpRequest) {
        if (httpRequest.method != HttpMethod.Get) {
            return call.respondText("Not found", status = HttpStatusCode.NotFound)
        }

        if (httpRequest.pathSegments.isEmpty()) {
            val stats = statOverview()

            // index
            return call.respondHtml {
                env.template(this, "Hit stats", listOf(stats, Content.LinkList("Export", listOf(
                        Link("${env.routePrefix}/hits.csv", "Hits CSV"),
                        Link("${env.routePrefix}/counted-hits.csv", "Counted hits CSV")
                ))))
            }
        }

        if (httpRequest.pathSegments.size == 1) {
            return when (httpRequest.pathSegments[0]) {
                "hits.csv" -> renderHits(call, false)
                "counted-hits.csv" -> renderHits(call, true)
                else -> call.respondText("Not found", status = HttpStatusCode.NotFound)
            }
        }
    }

    private suspend fun statOverview() : Content.Card {
        val now = LocalDateTime.now()

        val dayAgo = now.minusDays(1)
        val hostsIn1d = statTable.getHostsAfter(dayAgo).toString()
        val hitsIn1d = statTable.getHitsAfter(dayAgo).toString()

        val weekAgo = now.minusDays(7)
        val hostsIn7d = statTable.getHostsAfter(weekAgo).toString()
        val hitsIn7d = statTable.getHitsAfter(weekAgo).toString()

        val monthAgo = now.minusDays(30)
        val hostsIn30d = statTable.getHostsAfter(monthAgo).toString()
        val hitsIn30d = statTable.getHitsAfter(monthAgo).toString()

        return Content.Card("Stats") {
            table("mdl-data-table mdl-js-data-table") {
                thead {
                    tr {
                        th(classes = "mdl-data-table__cell--non-numeric") { +"Period" }
                        th { +"Last day" }
                        th { +"7 days" }
                        th { +"30 days" }
                    }
                }
                tbody {
                    tr {
                        td("mdl-data-table__cell--non-numeric") { +"Hosts" }
                        td { +hostsIn1d }
                        td { +hostsIn7d }
                        td { +hostsIn30d }
                    }
                    tr {
                        td("mdl-data-table__cell--non-numeric") { +"Hits" }
                        td { +hitsIn1d }
                        td { +hitsIn7d }
                        td { +hitsIn30d }
                    }
                }
            }
        }
    }

    private suspend fun renderHits(call: ApplicationCall, countedOnly: Boolean) {
        val showAll = !countedOnly
        val recs = statTable.getRecords()
        return call.respondWrite(ContentType.Text.Plain) {
            recs.forEach {
                if (showAll || it.counted) {
                    write(it.time.toString())
                    write(",\t")
                    write(it.remoteAddress)
                    write(",\t")
                    write(it.requestUri)
                    write(",\t")
                    write(it.referrer)
                    write(",\t")
                    write(it.userAgentStr)
                    write(",\n")
                }
            }
        }
    }

    override suspend fun webSocket(routePrefix: String, request: WsRequest) {
        throw UnsupportedOperationException()
    }

    suspend fun trackVisit(call: ApplicationCall) {
        val requestUri = call.request.uri
        val userAgent = call.request.userAgent() ?: ""
        val counted = !ignoreRequestUri(requestUri) && !isBot(userAgent)
        statTable.add(
                counted = counted,
                remoteAddress = remoteAddr(call.request),
                requestUri = requestUri,
                referrer = call.request.header("Referer") ?: "",
                userAgentStr = userAgent
        )
    }

    companion object {
        val GetRemoteAddr = { req: ApplicationRequest ->
            req.header("X-Forwarded-For")?.split(", ")?.first() ?: req.local.remoteHost
        }

        private val notALetter = Regex("\\W")

        val IsBot = { userAgent: String ->
            userAgent.split(notALetter).any { it.toLowerCase().endsWith("bot") }
        }

        val IgnoreRequestUri = { uri: String ->
            uri.endsWith(".js") || uri.endsWith(".css")
        }
    }

}

interface StatRecord {
    val counted: Boolean
    val time: LocalDateTime
    val remoteAddress: String
    val requestUri: String
    val referrer: String
    val userAgentStr: String
    val userAgent: UserAgent
}

interface StatTable {
    suspend fun add(counted: Boolean, remoteAddress: String, requestUri: String, referrer: String, userAgentStr: String)
    suspend fun getHitsAfter(date: LocalDateTime): Int
    suspend fun getHostsAfter(date: LocalDateTime): Int
    suspend fun getRecords(): List<StatRecord>
}

data class UserAgent(
        val operatingSystem: String,
        val device: String,
        val browser: String
)

class InMemoryStatRecord(
        override val counted: Boolean,
        override val time: LocalDateTime,
        override val remoteAddress: String,
        override val requestUri: String,
        override val referrer: String,
        override val userAgentStr: String,
        override val userAgent: UserAgent
) : StatRecord

class InMemoryStatTable(
        private val parseUa: (String) -> UserAgent
) : StatTable {

    private val records = CopyOnWriteArrayList<InMemoryStatRecord>()
    private val remoteAddresses = ConcurrentHashMap<String, String>()
    private val requestUris = ConcurrentHashMap<String, String>()
    private val referrers = ConcurrentHashMap<String, String>()
    private val userAgentStrs = ConcurrentHashMap<String, String>()
    private val userAgents = ConcurrentHashMap<UserAgent, UserAgent>()

    override suspend fun add(counted: Boolean, remoteAddress: String, requestUri: String, referrer: String, userAgentStr: String) {
        val now = LocalDateTime.now()
        val cRemoteAddress = canonical(remoteAddresses, remoteAddress)
        val cRequestUri = canonical(requestUris, requestUri)
        val cReferrer = canonical(referrers, referrer)
        val cUserAgentStr = canonical(userAgentStrs, userAgentStr)
        val userAgent = canonical(userAgents, parseUa(userAgentStr))
        records.add(InMemoryStatRecord(counted, now, cRemoteAddress, cRequestUri, cReferrer, cUserAgentStr, userAgent))
    }

    override suspend fun getHitsAfter(date: LocalDateTime): Int {
        val itr = records.listIterator(records.size)
        var cnt = 0
        while (itr.hasPrevious()) {
            if (itr.previous().time > date) cnt++
            else break
        }
        return cnt
    }

    override suspend fun getHostsAfter(date: LocalDateTime): Int {
        val set = HashSet<String>()
        val itr = records.listIterator(records.size)
        while (itr.hasPrevious()) {
            val rec = itr.previous()
            if (rec.time <= date) break
            set.add(rec.remoteAddress)
        }
        return set.size
    }

    override suspend fun getRecords(): List<StatRecord> =
            unmodifiableList(records)

    private fun <T : Any> canonical(canonicalMap: ConcurrentMap<T, T>, s: T): T =
            canonicalMap.putIfAbsent(s, s) ?: s

}
