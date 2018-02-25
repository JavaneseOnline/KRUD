package online.javanese.krud.crud.action

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import io.ktor.util.StringValues
import io.ktor.util.toMap
import online.javanese.krud.WebEnv
import online.javanese.krud.crud.RecordActionRoute
import online.javanese.krud.crud.RecordEndpoint
import online.javanese.krud.crud.TableAndRecord
import online.javanese.krud.template.Content

internal fun Review(
        continueEditingRoute: RecordActionRoute, updateRoute: RecordActionRoute
): RecordEndpoint = { env, call, tableAndRecord, _, post ->
    captureEAndReturnPreUpdateTable(env, call, tableAndRecord, post, continueEditingRoute, updateRoute)
}
private suspend fun <E : Any> captureEAndReturnPreUpdateTable(
        env: WebEnv, call: ApplicationCall, tableAndRecord: TableAndRecord<E, *>, map: StringValues,
        continueEditingRoute: RecordActionRoute, updateRoute: RecordActionRoute
) {
    val (table, _) = tableAndRecord
    // TODO: calculate diff
    val newRecord = table.createFrom(map)
    call.respondHtml {
        env.template(
                this,
                "Reviewing ${table.getTitle(newRecord)} in ${table.displayName} — Crud",
                listOf(Content.Review(
                        table.getTitle(newRecord),
                        map.toMap().mapNotNull { (key, values) ->
                            if (values.isEmpty()) null // skip values which were not passed
                            else Triple(key, table.cols.single { it.name == key }.editControl.title, values)
                        },
                        continueEditingRoute.addressOf(env, table, newRecord),
                        updateRoute.addressOf(env, table, newRecord)
                ))
        )
    }
}
