package online.javanese.krud.crud

import io.ktor.application.ApplicationCall
import io.ktor.response.respondRedirect
import io.ktor.util.ValuesMap
import online.javanese.krud.WebEnv
import online.javanese.krud.toStringMap

internal fun Insert(listRoute: TableActionRoute): TableAction = { env, call, table, _, post ->
    captureEAndInsert(env, call, table, post, listRoute)
}
private suspend fun <E : Any> captureEAndInsert(
        env: WebEnv, call: ApplicationCall, table: Table<E, *>, post: ValuesMap,
        listRoute: TableActionRoute
) {
    val new = table.createFromMap(post.toStringMap())
    table.save(new)
    call.respondRedirect(listRoute.addressOf(env, table))
}
