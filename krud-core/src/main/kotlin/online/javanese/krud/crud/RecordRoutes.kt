package online.javanese.krud.crud

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpMethod
import io.ktor.util.ValuesMap
import online.javanese.krud.WebEnv

/**
 * Describes a route, an address template of an endpoint
 * @see RecordPageRoute
 * @see RecordActionRoute
 */
interface RecordRoute {
    val method: HttpMethod
    val keyword: String
    fun <E : Any> addressOf(env: WebEnv, table: Table<E, *>, record: E): String
}

/**
 * A route to a page which shows something of a record.
 */
class RecordPageRoute(
        override val keyword: String
) : RecordRoute {
    override val method: HttpMethod get() = HttpMethod.Get
    override fun <E : Any> addressOf(env: WebEnv, table: Table<E, *>, record: E) =
            "${env.routePrefix}/${table.route}/$keyword/${table.getId(record)}/"
}

/**
 * A route to an action which can be performed on a record.
 */
class RecordActionRoute(
        override val keyword: String
) : RecordRoute {
    override val method: HttpMethod get() = HttpMethod.Post
    override fun <E : Any> addressOf(env: WebEnv, table: Table<E, *>, record: E) =
            "${env.routePrefix}/${table.route}/$keyword/${table.getId(record)}/"
}

class TableAndRecord<T : Any, ID>(
        val table: Table<T, ID>,
        val record: T
) {
    operator fun component1() = table
    operator fun component2() = record
}

typealias RecordEndpoint = suspend (
        env: WebEnv, call: ApplicationCall, tableAndRecord: TableAndRecord<*, *>, query: ValuesMap, post: ValuesMap
) -> Unit

/**
 * Encapsulates route and record action
 */
class RoutedRecordEndpoint(
        val route: RecordRoute,
        val action: RecordEndpoint
)
