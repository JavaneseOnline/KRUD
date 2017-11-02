package online.javanese.krud.crud

import online.javanese.krud.WebEnv
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.response.respondRedirect

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
