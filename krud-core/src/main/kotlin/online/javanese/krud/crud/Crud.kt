package online.javanese.krud.crud

import online.javanese.krud.HttpRequest
import online.javanese.krud.Module
import online.javanese.krud.WebEnv
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

    private val List = TableActionRoute(HttpMethod.Get, "list", List())
    private val Reorder = TableActionRoute(HttpMethod.Post, "reorder", Reorder())
    private val Create = TableActionRoute(HttpMethod.Get, "create", Create())
    private val Insert = TableActionRoute(HttpMethod.Post, "create", Insert())

    private val Edit: RecordActionRoute
    private val Review = RecordActionRoute(HttpMethod.Post, "review", Review())
    private val ContinueEditing: RecordActionRoute
    private val Delete = RecordActionRoute(HttpMethod.Post, "delete", Delete())
    private val Update = RecordActionRoute(HttpMethod.Post, "update", Update())

    init {
        val edit = Edit() // for both GET and POST
        Edit = RecordActionRoute(HttpMethod.Get, "edit", edit)
        ContinueEditing = RecordActionRoute(HttpMethod.Post, "edit", edit)
    }

    private val tableActions: List<TableActionRoute> = listOf(List, Reorder, Create, Insert)
    private val recordActions: List<RecordActionRoute> = listOf(Edit, Review, ContinueEditing, Delete, Update)

    suspend override fun request(
            env: WebEnv,
            call: ApplicationCall,
            httpRequest: HttpRequest
    ) {
        val pSegm = httpRequest.pathSegments
        when {
            httpRequest.method == HttpMethod.Get && pSegm.isEmpty() ->  // GET /admin/
                index(env, call)

            pSegm.size == 2 -> {                                        // {METHOD} /admin/{table}/{action}/
                val action = tableActions
                        .single { it.method == httpRequest.method && it.keyword == pSegm[1] }.action
                findTableAndRun(call, pSegm[0]) { table ->
                    action(env, call, table, httpRequest.query, httpRequest.post)
                }
            }

            pSegm.size == 3 -> {
                val action = recordActions                  // {METHOD} /admin/{table}/action/{id}/
                        .single { it.method == httpRequest.method && it.keyword == pSegm[1] }.action
                findTableAndRun(call, pSegm[0]) { table ->
                    captureTFindRecordAndPerform(env, call, table, pSegm[2], action, httpRequest.query, httpRequest.post)
                }
            }

            else ->
                call.respondText("Not found.", ContentType.Text.Plain, HttpStatusCode.NotFound)
        }
    }

    private suspend fun <T : Any> captureTFindRecordAndPerform(
            env: WebEnv, call: ApplicationCall, table: Table<T, *>, recordIdStr: String, action: RecordAction,
            query: ValuesMap, post: ValuesMap
    ) = findOneAndRun(call, table, recordIdStr) { record ->
        action(env, call, TableAndRecord(table, record), query, post)
    }

    private suspend fun index(env: WebEnv, call: ApplicationCall) = call.respondHtml {
        env.template(
                this,
                "Crud index",
                Content.LinkList(
                        "Tables",
                        tables.map { Link(List.addressOf(env, it), it.displayName.fixIfBlank(), it.count.toString()) }
                ) // todo: webSocket reactive update
        )
    }

    private fun List(): TableAction = { env, call, table, _, _ ->
        captureEAndReturnList(env, call, table)
    }
    private suspend fun <E : Any> captureEAndReturnList(
            env: WebEnv, call: ApplicationCall, table: Table<E, *>
    ) = call.respondHtml {
        val all = table.findAll()
        val createNew = Link(Create.addressOf(env, table), " + create new")
        env.template(
                this,
                "${table.displayName} — Crud",
                when (table.sort) {
                    is Sort.NoneOrImplicit -> Content.LinkList(
                            table.displayName,
                            all.map { createLinkToEditRecord(env, table, it) } + createNew
                    )
                    is Sort.Explicit<*> -> Content.SortableLinkList(
                            table.displayName,
                            all.map { createLinkToEditRecord(env, table, it) to table.getId(it).toString() } + (createNew to null),
                            Reorder.addressOf(env, table)
                    )
                }
        )
    }
    private fun <T : Any> createLinkToEditRecord(env: WebEnv, table: Table<T, *>, record: T) =
            Link(Edit.addressOf(env, table, record), table.getTitle(record).fixIfBlank())

    private fun Reorder(): TableAction = { env, call, table, _, post ->
        captureIdAndReorder(env, call, table, post)
    }
    private suspend fun <ID> captureIdAndReorder(env: WebEnv, call: ApplicationCall, table: Table<*, ID>, post: ValuesMap) {
        val sort = table.sort as? Sort.Explicit
                ?: return call.respondText("This table cannot be reordered.", ContentType.Text.Plain, HttpStatusCode.BadRequest)

        val ids = post.getAll("ids[]")
                ?: return call.respondText("'ids[]' request field is required.", ContentType.Text.Plain, HttpStatusCode.BadRequest)

        val newOrder = ids.map(table::stringToId)
        sort.updateOrder(newOrder)

        call.respondText("", status = HttpStatusCode.NoContent)
    }

    private fun Create(): TableAction = { env, call, table, _, post ->
        returnForm(
                call, env.template,
                "Creating new ${table.displayName} — Crud", "New ${table.displayName}",
                Content.Form.Mode.Create,
                table.cols.asSequence()
                        .map(Col<*>::createControl)
                        .filterNotNull().map { it to "" }
                        .toList(),
                Create.addressOf(env, table)
        )
    }

    private fun Insert(): TableAction = { env, call, table, _, post ->
        captureEAndInsert(env, call, table, post)
    }
    private suspend fun <E : Any> captureEAndInsert(env: WebEnv, call: ApplicationCall, table: Table<E, *>, post: ValuesMap) {
        val new = table.createFromMap(post.toStringMap())
        table.save(new)
        call.respondRedirect(List.addressOf(env, table))
    }


    private fun Edit(): RecordAction = { env, call, tableAndRecord, _, post ->
        captureTAndReturnForm(env, call, tableAndRecord, post)
    }
    private suspend fun <T : Any> captureTAndReturnForm(env: WebEnv, call: ApplicationCall, tableAndRecord: TableAndRecord<T, *>, post: ValuesMap) {
        val (table, record) = tableAndRecord
        val updated = table.toMap(record) + post.toStringMap()
        val recordTitle = table.getTitle(record)
        returnForm(
                call, env.template,
                "Editing $recordTitle in ${table.displayName} — Crud", recordTitle,
                Content.Form.Mode.Edit(Delete.addressOf(env, table, record)),
                table.cols.asSequence()
                        .filter { it.editControl != null }
                        .map { it.editControl!! to updated[it.name]!! }
                        .toList(),
                Review.addressOf(env, table, record)
        )
    }

    private fun Review(): RecordAction = { env, call, tableAndRecord, _, post ->
        captureEAndReturnPreUpdateTable(env, call, tableAndRecord, post)
    }
    private suspend fun <E : Any> captureEAndReturnPreUpdateTable(
            env: WebEnv, call: ApplicationCall, tableAndRecord: TableAndRecord<E, *>, map: ValuesMap
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
                            map.toMap().map { (key, values) ->
                                Triple(key, table.cols.single { it.name == key }.editControl!!.title, values.single())
                            },
                            ContinueEditing.addressOf(env, table, newRecord),
                            Update.addressOf(env, table, newRecord)
                    )
            )
        }
    }

    private fun Delete(): RecordAction = { env, call, tableAndRecord, _, _ ->
        captureEIdAndDelete(env, call, tableAndRecord)
    }
    private suspend fun <E : Any, ID> captureEIdAndDelete(env: WebEnv, call: ApplicationCall, tableAndRecord: TableAndRecord<E, ID>) {
        val (table, record) = tableAndRecord
        table.delete(table.getId(record))
        call.respondRedirect(List.addressOf(env, table))
    }

    private fun Update(): RecordAction = { env, call, tableAndRecord, _, post ->
        captureEAndPatch(env, call, tableAndRecord, post)
    }
    private suspend fun <E : Any> captureEAndPatch(
            env: WebEnv, call: ApplicationCall, tableAndRecord: TableAndRecord<E, *>, post: ValuesMap
    ) {
        val (table, record) = tableAndRecord
        val updatedMap = table.toMap(record) + post.toStringMap()
        val updatedE = table.createFromMap(updatedMap)
        table.save(updatedE)

        call.respondRedirect(List.addressOf(env, table))
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
    private fun String.fixIfBlank() = if (isBlank()) " ( blank ) " else this

}
