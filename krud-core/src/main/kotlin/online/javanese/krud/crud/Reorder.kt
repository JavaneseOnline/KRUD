package online.javanese.krud.crud

import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.response.respondText
import org.jetbrains.ktor.util.ValuesMap

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
