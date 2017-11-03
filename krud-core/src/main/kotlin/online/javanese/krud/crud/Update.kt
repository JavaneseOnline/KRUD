package online.javanese.krud.crud

import io.ktor.application.ApplicationCall
import io.ktor.response.respondRedirect
import io.ktor.util.ValuesMap
import online.javanese.krud.WebEnv
import online.javanese.krud.toStringMap

internal fun Update(listRoute: TableActionRoute): RecordAction = { env, call, tableAndRecord, _, post ->
    captureEAndPatch(env, call, tableAndRecord, post, listRoute)
}
private suspend fun <E : Any> captureEAndPatch(
        env: WebEnv, call: ApplicationCall, tableAndRecord: TableAndRecord<E, *>, post: ValuesMap,
        listRoute: TableActionRoute
) {
    val (table, record) = tableAndRecord
    val updatedMap = table.toMap(record) + post.toStringMap()
    val updatedE = table.createFromMap(updatedMap)
    table.save(updatedE)

    call.respondRedirect(listRoute.addressOf(env, table))
}