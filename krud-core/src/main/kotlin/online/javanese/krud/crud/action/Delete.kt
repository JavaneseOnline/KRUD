package online.javanese.krud.crud.action

import io.ktor.application.ApplicationCall
import io.ktor.response.respondRedirect
import online.javanese.krud.WebEnv
import online.javanese.krud.crud.RecordEndpoint
import online.javanese.krud.crud.TableAndRecord
import online.javanese.krud.crud.TablePageRoute

internal fun Delete(listRoute: TablePageRoute): RecordEndpoint = { env, call, tableAndRecord, _, _ ->
    captureEIdAndDelete(env, call, tableAndRecord, listRoute)
}
private suspend fun <E : Any, ID> captureEIdAndDelete(
        env: WebEnv, call: ApplicationCall, tableAndRecord: TableAndRecord<E, ID>,
        listRoute: TablePageRoute
) {
    val (table, record) = tableAndRecord
    table.delete(table.getId(record))
    call.respondRedirect(listRoute.addressOf(env, table))
}
