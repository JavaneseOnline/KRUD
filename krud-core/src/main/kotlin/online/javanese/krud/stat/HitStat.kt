package online.javanese.krud.stat

import io.ktor.application.ApplicationCall
import io.ktor.request.header
import io.ktor.request.uri
import io.ktor.request.userAgent
import kotlinx.html.*
import online.javanese.krud.HttpRequest
import online.javanese.krud.Module
import online.javanese.krud.WebEnv
import online.javanese.krud.WsRequest
import online.javanese.krud.template.Content
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.CopyOnWriteArrayList

class HitStat(
        private val statTable: StatTable
) : Module {

    override val name: String get() = "Hits"

    suspend override fun summary(env: WebEnv): Content {
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

    suspend override fun http(env: WebEnv, call: ApplicationCall, httpRequest: HttpRequest) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    suspend override fun webSocket(routePrefix: String, request: WsRequest) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    suspend fun trackVisit(call: ApplicationCall) {
        statTable.add(
                remoteAddress = call.request.local.remoteHost,
                requestUri = call.request.uri,
                referrer = call.request.header("Referer") ?: "",
                userAgentStr = call.request.userAgent() ?: ""
        )
    }

}

interface StatRecord {
    val time: LocalDateTime
    val remoteAddress: String
    val requestUri: String
    val referrer: String
    val userAgentStr: String
    val userAgent: UserAgent
}

interface StatTable {
    suspend fun add(remoteAddress: String, requestUri: String, referrer: String, userAgentStr: String)
    suspend fun getHitsAfter(date: LocalDateTime): Int
    suspend fun getHostsAfter(date: LocalDateTime): Int
}

data class UserAgent(
        val operatingSystem: String,
        val device: String,
        val browser: String
)

class InMemoryStatRecord(
        override val time: LocalDateTime,
        override val remoteAddress: String,
        override val requestUri: String,
        override val referrer: String,
        override val userAgentStr: String,
        override val userAgent: UserAgent
) : StatRecord

class InMemoryStatTable(
        private val parseUa: (String) -> UserAgent,
        private val ignoreRequestUri: (String) -> Boolean = { false }
) : StatTable {

    private val records = CopyOnWriteArrayList<InMemoryStatRecord>()
    private val remoteAddresses = ConcurrentHashMap<String, String>()
    private val requestUris = ConcurrentHashMap<String, String>()
    private val referrers = ConcurrentHashMap<String, String>()
    private val userAgentStrs = ConcurrentHashMap<String, String>()
    private val userAgents = ConcurrentHashMap<UserAgent, UserAgent>()

    override suspend fun add(remoteAddress: String, requestUri: String, referrer: String, userAgentStr: String) {
        if (ignoreRequestUri(requestUri)) return

        val now = LocalDateTime.now()
        val cRemoteAddress = canonical(remoteAddresses, remoteAddress)
        val cRequestUri = canonical(requestUris, requestUri)
        val cReferrer = canonical(referrers, referrer)
        val cUserAgentStr = canonical(userAgentStrs, userAgentStr)
        val userAgent = canonical(userAgents, parseUa(userAgentStr))
        records.add(InMemoryStatRecord(now, cRemoteAddress, cRequestUri, cReferrer, cUserAgentStr, userAgent))
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

    private fun <T : Any> canonical(canonicalMap: ConcurrentMap<T, T>, s: T): T =
            canonicalMap.putIfAbsent(s, s) ?: s

}
