package online.javanese.krud.crud

import online.javanese.krud.WebEnv
import online.javanese.krud.template.Content
import online.javanese.krud.toStringMap
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.html.respondHtml
import org.jetbrains.ktor.util.ValuesMap

internal fun Edit(
        deleteRoute: RecordActionRoute, reviewRoute: RecordActionRoute
): RecordAction = { env, call, tableAndRecord, _, post ->
    captureTAndReturnForm(env, call, tableAndRecord, post, deleteRoute, reviewRoute)
}
private suspend fun <T : Any> captureTAndReturnForm(
        env: WebEnv, call: ApplicationCall, tableAndRecord: TableAndRecord<T, *>, post: ValuesMap,
        deleteRoute: RecordActionRoute, reviewRoute: RecordActionRoute
) {
    val (table, record) = tableAndRecord
    val updated = table.toMap(record) + post.toStringMap()
    val recordTitle = table.getTitle(record)

    call.respondHtml {
        env.template(
                this,
                "Editing $recordTitle in ${table.displayName} — Crud",
                Content.Form(
                        recordTitle,
                        Content.Form.Mode.Edit(deleteRoute.addressOf(env, table, record)),
                        table.cols.mapNotNull { col -> updated[col.name]?.let { value -> col.editControl to value } },
                        reviewRoute.addressOf(env, table, record)
                )
        )
    }
}
