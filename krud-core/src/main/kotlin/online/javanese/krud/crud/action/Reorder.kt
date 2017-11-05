package online.javanese.krud.crud.action

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.util.ValuesMap
import online.javanese.krud.crud.Sort
import online.javanese.krud.crud.Table
import online.javanese.krud.crud.TableAction

internal fun Reorder(): TableAction = { env, call, table, _, post ->
    captureIdAndReorder(call, table, post)
}
private suspend fun <ID> captureIdAndReorder(call: ApplicationCall, table: Table<*, ID>, post: ValuesMap) {
    val sort = table.sort as? Sort.Explicit
            ?: return call.respondText("This table cannot be reordered.", ContentType.Text.Plain, HttpStatusCode.BadRequest)

    val ids = post.getAll("ids[]")
            ?: return call.respondText("'ids[]' request field is required.", ContentType.Text.Plain, HttpStatusCode.BadRequest)

    val newOrder = ids.map(table::stringToId)
    sort.updateOrder(newOrder)

    call.respondText("", status = HttpStatusCode.NoContent)
}
