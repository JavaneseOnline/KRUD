package online.javanese.krud.crud

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpMethod
import io.ktor.util.ValuesMap
import online.javanese.krud.WebEnv

/**
 * Describes a route, an address template of an endpoint
 * @see TablePageRoute for GET
 * @see TableActionRoute for POST
 */
interface TableRoute {
    val method: HttpMethod
    val keyword: String
    fun addressOf(env: WebEnv, table: Table<*, *>): String
}

/**
 * A route to page which represents something of a [Table].
 */
class TablePageRoute(
        override val keyword: String
) : TableRoute {
    override val method: HttpMethod get() = HttpMethod.Get
    override fun addressOf(env: WebEnv, table: Table<*, *>) =
            "${env.routePrefix}/${table.route}/$keyword/"
}

/**
 * A route to action which can be performed on a [Table].
 */
class TableActionRoute(
        override val keyword: String
) : TableRoute {
    override val method: HttpMethod get() = HttpMethod.Post
    override fun addressOf(env: WebEnv, table: Table<*, *>) =
            "${env.routePrefix}/${table.route}/$keyword/"
}

/**
 * Action that can be performed on a table, e. g. list, create new record, etc.
 */
typealias TableEndpoint = suspend (
        env: WebEnv, call: ApplicationCall, table: Table<*, *>, query: ValuesMap, post: ValuesMap
) -> Unit

/**
 * Encapsulates route and table action
 */
class RoutedTableEndpoint(
        val route: TableRoute,
        val action: TableEndpoint
)
