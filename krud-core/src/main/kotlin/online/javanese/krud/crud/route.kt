package online.javanese.krud.crud

import online.javanese.krud.WebEnv
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.HttpMethod
import org.jetbrains.ktor.util.ValuesMap

/**
 * A route to action which can be performed on a [Table].
 */
class TableActionRoute(
        val method: HttpMethod,
        val keyword: String,
        val action: TableAction
)

/**
 * Action that can be performed on a table, e. g. list, create new record, etc.
 */
typealias TableAction = suspend (
        env: WebEnv, call: ApplicationCall, table: Table<*, *>, query: ValuesMap, post: ValuesMap
) -> Unit


/**
 * A route to action which can be performed on a record.
 */
class RecordActionRoute(
        val method: HttpMethod,
        val keyword: String,
        val action: RecordAction
)

class TableAndRecord<T : Any, ID>(
        val table: Table<T, ID>,
        val record: T
) {
    operator fun component1() = table
    operator fun component2() = record
}

typealias RecordAction = suspend (
        env: WebEnv, call: ApplicationCall, tableAndRecord: TableAndRecord<*, *>, query: ValuesMap, post: ValuesMap
) -> Unit
