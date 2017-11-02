package online.javanese.krud.crud

import io.ktor.application.ApplicationCall
import io.ktor.response.respondRedirect
import online.javanese.krud.WebEnv

internal fun Delete(listRoute: TableActionRoute): RecordAction = { env, call, tableAndRecord, _, _ ->
    captureEIdAndDelete(env, call, tableAndRecord, listRoute)
}
private suspend fun <E : Any, ID> captureEIdAndDelete(
        env: WebEnv, call: ApplicationCall, tableAndRecord: TableAndRecord<E, ID>, listRoute: TableActionRoute
) {
    val (table, record) = tableAndRecord
    table.delete(table.getId(record))
    call.respondRedirect(listRoute.addressOf(env, table))
}
