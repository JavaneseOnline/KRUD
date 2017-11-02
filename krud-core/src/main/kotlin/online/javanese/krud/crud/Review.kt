package online.javanese.krud.crud

import online.javanese.krud.WebEnv
import online.javanese.krud.template.Content
import online.javanese.krud.toStringMap
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.html.respondHtml
import org.jetbrains.ktor.util.ValuesMap
import org.jetbrains.ktor.util.toMap

internal fun Review(
        continueEditingRoute: RecordActionRoute, updateRoute: RecordActionRoute
): RecordAction = { env, call, tableAndRecord, _, post ->
    captureEAndReturnPreUpdateTable(env, call, tableAndRecord, post, continueEditingRoute, updateRoute)
}
private suspend fun <E : Any> captureEAndReturnPreUpdateTable(
        env: WebEnv, call: ApplicationCall, tableAndRecord: TableAndRecord<E, *>, map: ValuesMap,
        continueEditingRoute: RecordActionRoute, updateRoute: RecordActionRoute
) {
    val (table, _) = tableAndRecord
    // TODO: calculate diff
    val newRecord = table.createFromMap(map.toStringMap())
    call.respondHtml {
        env.template(
                this,
                "Reviewing ${table.getTitle(newRecord)} in ${table.displayName} — Crud",
                Content.Review(
                        table.getTitle(newRecord),
                        map.toMap().mapNotNull { (key, values) ->
                            if (values.isEmpty()) null // skip values which were not passed
                            else Triple(key, table.cols.single { it.name == key }.editControl.title, values.single())
                        },
                        continueEditingRoute.addressOf(env, table, newRecord),
                        updateRoute.addressOf(env, table, newRecord)
                )
        )
    }
}
