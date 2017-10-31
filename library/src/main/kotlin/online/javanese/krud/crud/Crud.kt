package online.javanese.krud.crud

import online.javanese.krud.Module
import online.javanese.krud.checkRoutes
import online.javanese.krud.template.Content
import online.javanese.krud.template.Control
import online.javanese.krud.template.Link
import online.javanese.krud.template.ModuleTemplate
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.html.respondHtml
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.http.HttpMethod
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.response.respondRedirect
import org.jetbrains.ktor.response.respondText
import org.jetbrains.ktor.util.ValuesMap
import org.jetbrains.ktor.util.toMap

class Crud(
        private vararg val tables: Table<*, *>
) : Module {

    init {
        tables.checkRoutes("Table", "tables", Table<*, *>::route)
    }

    override val name: String get() = "CRUD"

    suspend override fun request(
            call: ApplicationCall, routePrefix: String, template: ModuleTemplate,
            method: HttpMethod, pathSegments: List<String>, query: ValuesMap, post: ValuesMap
    ) {
        val pSegm = pathSegments
        when {
            method == HttpMethod.Get && pSegm.isEmpty() ->                          // GET /admin/
                index(routePrefix, template, call)

            method == HttpMethod.Get && pSegm.size == 2 && pSegm[1] == "list" ->    // GET /admin/{table}/list/
                list(routePrefix, template, call, tableRoute = pSegm[0])

            method == HttpMethod.Get && pSegm.size == 2 && pSegm[1] == "create"  -> // GET /admin/{table}/create/
                create(routePrefix, template, call, tableRoute = pSegm[0])

            method == HttpMethod.Post && pSegm.size == 2 && pSegm[1] == "create"  -> // GET /admin/{table}/create/
                insert(routePrefix, template, call, tableRoute = pSegm[0], post = post)

            method == HttpMethod.Get && pSegm.size == 3 && pSegm[1] == "edit"  ->   // GET /admin/{table}/edit/{id}/
                edit(routePrefix, template, call, tableRoute = pSegm[0], recordIdStr = pSegm[2], post = ValuesMap.Empty)

            method == HttpMethod.Post && pSegm.size == 3 && pSegm[1] == "review" -> // GET /admin/{table}/review/{id}/
                review(routePrefix, template, call, tableRoute = pSegm[0], recordIdStr = pSegm[2], post = post)

            method == HttpMethod.Post && pSegm.size == 3 && pSegm[1] == "edit" ->   // POST /admin/{table}/edit/{id}/
                edit(routePrefix, template, call, tableRoute = pSegm[0], recordIdStr = pSegm[2], post = post)

            method == HttpMethod.Post && pSegm.size == 3 && pSegm[1] == "update" -> // POST /admin/{table}/update/{id}/
                update(routePrefix, call, tableRoute = pSegm[0], recordIdStr = pSegm[2], post = post)

            else ->
                call.respondText("Not found.", ContentType.Text.Plain, HttpStatusCode.NotFound)
        }
    }

    private suspend fun index(routePrefix: String, template: ModuleTemplate, call: ApplicationCall) = call.respondHtml {
        template(
                this,
                "Crud index",
                Content.LinkList(
                        "Tables",
                        tables.map { Link("$routePrefix/${it.route}/list/", "${it.displayName} (${it.count})") }
                ) // todo: webSocket reactive update
        )
    }

    private suspend fun list(routePrefix: String, template: ModuleTemplate, call: ApplicationCall, tableRoute: String) = findTableAndRun(call, tableRoute) { table ->
        captureEAndReturnList(routePrefix, template, call, table)
    }
    private suspend fun <E : Any> captureEAndReturnList(
            routePrefix: String, template: ModuleTemplate, call: ApplicationCall, table: Table<E, *>
    ) = call.respondHtml {
        template(
                this,
                "${table.displayName} — Crud",
                Content.LinkList(
                        table.displayName,
                        table.findAll().map {
                            Link("$routePrefix/${table.route}/edit/${table.getId(it)}/", table.getTitle(it))
                        } + Link("$routePrefix/${table.route}/create/", " + create new")
                )
        )
    }

    private suspend fun create(
            routePrefix: String, template: ModuleTemplate, call: ApplicationCall, tableRoute: String
    ) = findTableAndRun(call, tableRoute) { table ->
        returnForm(
                call, template,
                "Creating new ${table.displayName} — Crud", "New ${table.displayName}",
                Content.Form.Mode.Create,
                table.cols.asSequence()
                        .map(Col<*>::createControl)
                        .filterNotNull().map { it to "" }
                        .toList(),
                "$routePrefix/${table.route}/create/")
    }

    private suspend fun insert(
            routePrefix: String, template: ModuleTemplate, call: ApplicationCall, tableRoute: String, post: ValuesMap
    ) = findTableAndRun(call, tableRoute) { table ->
        captureEAndInsert(routePrefix, call, table, post)
    }
    private suspend fun <E : Any> captureEAndInsert(routePrefix: String, call: ApplicationCall, table: Table<E, *>, post: ValuesMap) {
        val new = table.createFromMap(post.toStringMap())
        table.save(new)
        call.respondRedirect("$routePrefix/${table.route}/list")
    }

    private suspend fun edit(
            routePrefix: String, template: ModuleTemplate, call: ApplicationCall,
            tableRoute: String, recordIdStr: String, post: ValuesMap
    ) = findTableAndRun(call, tableRoute) { table ->
        captureEIdAndReturnEditForm(routePrefix, call, template, table, recordIdStr, post)
    }
    private suspend fun <E : Any, ID> captureEIdAndReturnEditForm(
            routePrefix: String, call: ApplicationCall, template: ModuleTemplate,
            table: Table<E, ID>, recordIdStr: String, post: ValuesMap
    ) = findOneAndRun(call, table, recordIdStr) { record ->

        val updated = table.toMap(record) + post.toStringMap()
        val recordTitle = table.getTitle(record)
        returnForm(
                call, template,
                "Editing $recordTitle in ${table.displayName} — Crud", recordTitle,
                Content.Form.Mode.Edit,
                table.cols.asSequence()
                        .filter { it.editControl != null }
                        .map { it.editControl!! to updated[it.name]!! }
                        .toList(),
                "$routePrefix/${table.route}/review/${table.getId(record)}"
        )
    }

    private suspend fun review(routePrefix: String, template: ModuleTemplate, call: ApplicationCall, tableRoute: String, recordIdStr: String, post: ValuesMap) = findTableAndRun(call, tableRoute) { table ->
        captureEIdAndReturnPreUpdateTable(routePrefix, template, call, table, recordIdStr, post)
    }
    private suspend fun <E : Any, ID> captureEIdAndReturnPreUpdateTable(
            routePrefix: String, template: ModuleTemplate, call: ApplicationCall,
            table: Table<E, ID>, recordIdStr: String, map: ValuesMap
    ) {
        findOneAndRun(call, table, recordIdStr) { _ -> // TODO: calculate diff
            val newRecord = table.createFromMap(map.toStringMap())
            call.respondHtml {
                template(
                        this,
                        "Reviewing ${table.getTitle(newRecord)} in ${table.displayName} — Crud",
                        Content.Review(
                                table.getTitle(newRecord),
                                map.toMap().map { (key, values) ->
                                    Triple(key, table.cols.single { it.name == key }.editControl!!.title, values.single())
                                },
                                "$routePrefix/${table.route}/edit/${table.getId(newRecord)}",
                                "$routePrefix/${table.route}/update/${table.getId(newRecord)}"
                        )
                )
            }
        }
    }

    private suspend fun update(routePrefix: String, call: ApplicationCall, tableRoute: String, recordIdStr: String, post: ValuesMap) = findTableAndRun(call, tableRoute) { table ->
        captureEIdAndPatch(routePrefix, call, table, recordIdStr, post)
    }
    private suspend fun <E : Any, ID> captureEIdAndPatch(
            routePrefix: String, call: ApplicationCall, table: Table<E, ID>, recordIdStr: String, post: ValuesMap
    ) = findOneAndRun(call, table, recordIdStr) { record ->
        val updatedMap = table.toMap(record) + post.toStringMap()
        val updatedE = table.createFromMap(updatedMap)
        table.save(updatedE)

        call.respondRedirect("$routePrefix/${table.route}/list/")
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

    private suspend fun returnForm(
            call: ApplicationCall, template: ModuleTemplate,
            title: String, formTitle: String,
            mode: Content.Form.Mode, controlsAndValues: List<Pair<Control, String>>, submitAction: String
    ) = call.respondHtml {
        template(
                this, title,
                Content.Form(formTitle, mode, controlsAndValues, submitAction)
        )
    }

    private fun <E : Any> Table<E, *>.toMap(e: E) = cols.associateBy({ it.name }, { it.getValue(e) })
    private fun ValuesMap.toStringMap() = toMap().mapValues { (_, v) -> v.single() }

}
