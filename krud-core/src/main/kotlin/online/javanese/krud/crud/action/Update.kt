package online.javanese.krud.crud.action

import io.ktor.application.ApplicationCall
import io.ktor.response.respondRedirect
import io.ktor.util.StringValues
import online.javanese.krud.WebEnv
import online.javanese.krud.crud.RecordEndpoint
import online.javanese.krud.crud.TableAndRecord
import online.javanese.krud.crud.TablePageRoute
import online.javanese.krud.crud.toMap
import online.javanese.krud.updatedWith


internal fun Update(listRoute: TablePageRoute): RecordEndpoint = { env, call, tableAndRecord, _, post ->
    captureEAndPatch(env, call, tableAndRecord, post, listRoute)
}
private suspend fun <E : Any> captureEAndPatch(
        env: WebEnv, call: ApplicationCall, tableAndRecord: TableAndRecord<E, *>, post: StringValues,
        listRoute: TablePageRoute
) {
    val (table, record) = tableAndRecord
    val updatedMap = table.toMap(record) updatedWith post
    val updatedE = table.createFrom(updatedMap)
    table.update(updatedE)

    call.respondRedirect(listRoute.addressOf(env, table))
}
