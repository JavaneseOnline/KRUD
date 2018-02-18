package online.javanese.krud.crud.action

import io.ktor.application.ApplicationCall
import io.ktor.response.respondRedirect
import io.ktor.util.StringValues
import online.javanese.krud.WebEnv
import online.javanese.krud.crud.Table
import online.javanese.krud.crud.TableEndpoint
import online.javanese.krud.crud.TablePageRoute
import online.javanese.krud.toStringMap

internal fun Insert(listRoute: TablePageRoute): TableEndpoint = { env, call, table, _, post ->
    captureEAndInsert(env, call, table, post, listRoute)
}
private suspend fun <E : Any> captureEAndInsert(
        env: WebEnv, call: ApplicationCall, table: Table<E, *>, post: StringValues,
        listRoute: TablePageRoute
) {
    val new = table.createFromMap(post.toStringMap())
    table.insert(new)
    call.respondRedirect(listRoute.addressOf(env, table))
}
