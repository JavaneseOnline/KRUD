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
import io.ktor.response.respondTextWriter
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
        val weekAgo = now.minusDays(7)
        val monthAgo = now.minusDays(30)

        val hitsAndHosts = arrayOf(dayAgo, weekAgo, monthAgo).map { period ->
            arrayOf(
                    statTable.getHostsAfter(period, false),
                    statTable.getHostsAfter(period, true),
                    statTable.getHitsAfter(period, false),
                    statTable.getHitsAfter(period, true)
            )
        }

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
                    arrayOf("Hosts", "Counted hosts", "Hits", "Counted hits").forEachIndexed { index, title ->
                        tr {
                            td("mdl-data-table__cell--non-numeric") { +title }
                            hitsAndHosts.forEach { hitsHosts ->
                                td { +hitsHosts[index].toString() }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun renderHits(call: ApplicationCall, countedOnly: Boolean) {
        val showAll = !countedOnly
        val recs = statTable.getRecords()
        return call.respondTextWriter(ContentType.Text.Plain) {
            recs.forEach {
                if (showAll || it.counted) {
                    write(it.time.toString())
                    write(",\t")
                    write(it.statusCode.value.toString())
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
        val statusCode = call.response.status() ?: WtfStatusCode
        val counted = statusCode == HttpStatusCode.OK && !ignoreRequestUri(requestUri) && !isBot(userAgent)
        statTable.add(
                counted = counted,
                statusCode = statusCode,
                remoteAddress = remoteAddr(call.request),
                requestUri = requestUri,
                referrer = call.request.header("Referer") ?: "",
                userAgentStr = userAgent
        )
    }

    companion object {
        val WtfStatusCode = HttpStatusCode(-1, "statusCode was null")

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
    val statusCode: HttpStatusCode
    val time: LocalDateTime
    val remoteAddress: String
    val requestUri: String
    val referrer: String
    val userAgentStr: String
    val userAgent: UserAgent
}

interface StatTable {
    suspend fun add(counted: Boolean, statusCode: HttpStatusCode, remoteAddress: String, requestUri: String, referrer: String, userAgentStr: String)
    suspend fun getHitsAfter(date: LocalDateTime, countedOnly: Boolean): Int
    suspend fun getHostsAfter(date: LocalDateTime, countedOnly: Boolean): Int
    suspend fun getRecords(): List<StatRecord>
}

data class UserAgent(
        val operatingSystem: String,
        val device: String,
        val browser: String
)

class InMemoryStatRecord(
        override val counted: Boolean,
        override val statusCode: HttpStatusCode,
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

    override suspend fun add(counted: Boolean, statusCode: HttpStatusCode, remoteAddress: String, requestUri: String, referrer: String, userAgentStr: String) {
        val now = LocalDateTime.now()
        val cRemoteAddress = canonical(remoteAddresses, remoteAddress)
        val cRequestUri = canonical(requestUris, requestUri)
        val cReferrer = canonical(referrers, referrer)
        val cUserAgentStr = canonical(userAgentStrs, userAgentStr)
        val userAgent = canonical(userAgents, parseUa(userAgentStr))
        records.add(InMemoryStatRecord(counted, statusCode, now, cRemoteAddress, cRequestUri, cReferrer, cUserAgentStr, userAgent))
    }

    override suspend fun getHitsAfter(date: LocalDateTime, countedOnly: Boolean): Int {
        val itr = records.listIterator(records.size)
        var cnt = 0
        while (itr.hasPrevious()) {
            val rec = itr.previous()
            if (rec.time > date) {
                if (!countedOnly || rec.counted)
                    cnt++
            } else {
                break
            }
        }
        return cnt
    }

    override suspend fun getHostsAfter(date: LocalDateTime, countedOnly: Boolean): Int {
        val set = HashSet<String>()
        val itr = records.listIterator(records.size)
        while (itr.hasPrevious()) {
            val rec = itr.previous()
            if (rec.time <= date) break
            if (!countedOnly || rec.counted) {
                set.add(rec.remoteAddress)
            }
        }
        return set.size
    }

    override suspend fun getRecords(): List<StatRecord> =
            unmodifiableList(records)

    private fun <T : Any> canonical(canonicalMap: ConcurrentMap<T, T>, s: T): T =
            canonicalMap.putIfAbsent(s, s) ?: s

}
