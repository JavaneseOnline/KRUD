package online.javanese.krud.crud

import online.javanese.krud.Module
import online.javanese.krud.checkRoutes
import online.javanese.krud.template.Content
import online.javanese.krud.template.Link
import online.javanese.krud.template.ModuleTemplate
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.html.respondHtml
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.http.HttpMethod
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.response.respondText
import org.jetbrains.ktor.util.ValuesMap

class Crud(
        private vararg val tables: Table<*, *>
) : Module {

    init {
        tables.checkRoutes("Table", "tables", Table<*, *>::route)
    }

    override val name: String get() = "CRUD"

    suspend override fun request(
            call: ApplicationCall, routePrefix: String, template: ModuleTemplate,
            method: HttpMethod, pathSegments: List<String>, parameters: ValuesMap
    ) {
        when {
            method == HttpMethod.Get && pathSegments.isEmpty() -> index(routePrefix, template, call, parameters)
            method == HttpMethod.Get && pathSegments.size == 1 -> list(routePrefix, template, call, pathSegments[0], parameters)
            method == HttpMethod.Get && pathSegments.size == 2 -> record(routePrefix, template, call, pathSegments[0], pathSegments[1], parameters)
        }
    }

    private suspend fun index(routePrefix: String, template: ModuleTemplate, call: ApplicationCall, parameters: ValuesMap) {
        call.respondHtml {
            template(
                    this,
                    "Crud index",
                    Content.LinkList(
                            "Tables",
                            tables.map { Link("$routePrefix/${it.route}/", "${it.displayName} (${it.count})") } // todo + Link(create new)
                    ) // todo: webSocket interactive update
            )
        }
    }

    private suspend fun list(routePrefix: String, template: ModuleTemplate, call: ApplicationCall, tableRoute: String, parameters: ValuesMap) {
        findTableAndRun(call, tableRoute) { table ->
            captureEAndReturnList(routePrefix, template, call, table)
        }
    }

    private suspend fun <E : Any> captureEAndReturnList(
            routePrefix: String, template: ModuleTemplate, call: ApplicationCall, table: Table<E, *>
    ) = call.respondHtml {
        template(
                this,
                "${table.displayName} — Crud",
                Content.LinkList(
                        table.displayName,
                        table.findAll().map { Link("$routePrefix/${table.route}/${table.getId(it)}", table.getTitle(it)) }
                )
        )
    }

    private suspend fun record(
            routePrefix: String, template: ModuleTemplate, call: ApplicationCall, tableRoute: String, recordIdStr: String, parameters: ValuesMap
    ) {
        findTableAndRun(call, tableRoute) { table ->
            captureEIdAndReturnEditForm(call, template, table, recordIdStr)
        }
    }

    private suspend fun <E : Any, ID> captureEIdAndReturnEditForm(
            call: ApplicationCall, template: ModuleTemplate, table: Table<E, ID>, recordIdStr: String
    ) = findOneAndRun(call, table, recordIdStr) { record ->
        call.respondHtml {
            val recordTitle = table.getTitle(record)
            template(
                    this,
                    "$recordTitle — ${table.displayName} — Crud",
                    Content.Form(
                            recordTitle,
                            Content.Form.Mode.Edit,
                            table.cols.map { it.control to it.getValue(record) }
                    )
            )
        }
    }

    private inline suspend fun findTableAndRun(call: ApplicationCall, tableRoute: String, code: (Table<*, *>) -> Unit) {
        val table = tables.firstOrNull { it.route == tableRoute }
                ?: return call.respondText("Not found", ContentType.Text.Plain, HttpStatusCode.NotFound)

        code(table)
    }

    private inline suspend fun <E : Any, ID> findOneAndRun(call: ApplicationCall, table: Table<E, ID>, recordIDStr: String, code: (E) -> Unit) {
        val id = table.stringToId(recordIDStr)
        val item = table.findOne(id)
                ?: return call.respondText("Item was not found", ContentType.Text.Plain, HttpStatusCode.NotFound)

        code(item)
    }

}
