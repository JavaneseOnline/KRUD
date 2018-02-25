package online.javanese.krud.crud.action

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import io.ktor.util.StringValues
import online.javanese.krud.WebEnv
import online.javanese.krud.crud.RecordActionRoute
import online.javanese.krud.crud.RecordEndpoint
import online.javanese.krud.crud.TableAndRecord
import online.javanese.krud.crud.toMap
import online.javanese.krud.template.Content
import online.javanese.krud.updatedWith


internal fun Edit(
        deleteRoute: RecordActionRoute, reviewRoute: RecordActionRoute
): RecordEndpoint = { env, call, tableAndRecord, _, post ->
    captureTAndReturnForm(env, call, tableAndRecord, post, deleteRoute, reviewRoute)
}
private suspend fun <T : Any> captureTAndReturnForm(
        env: WebEnv, call: ApplicationCall, tableAndRecord: TableAndRecord<T, *>, post: StringValues,
        deleteRoute: RecordActionRoute, reviewRoute: RecordActionRoute
) {
    val (table, record) = tableAndRecord
    val updated = table.toMap(record) updatedWith post
    val recordTitle = table.getTitle(record)

    call.respondHtml {
        env.template(
                this,
                "Editing $recordTitle in ${table.displayName} — Crud",
                listOf(Content.Form(
                        recordTitle,
                        Content.Form.Mode.Edit(deleteRoute.addressOf(env, table, record)),
                        table.cols.mapNotNull {
                            col -> updated.getAll(col.name)?.let { values -> col.editControl to values }
                        },
                        reviewRoute.addressOf(env, table, record)
                ))
        )
    }
}
