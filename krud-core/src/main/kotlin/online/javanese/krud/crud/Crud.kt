package online.javanese.krud.crud

import online.javanese.krud.HttpRequest
import online.javanese.krud.Module
import online.javanese.krud.WebEnv
import online.javanese.krud.checkRoutes
import online.javanese.krud.template.Content
import online.javanese.krud.template.Link
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

    private val ListRoute = TableActionRoute(HttpMethod.Get, "list")
    private val ReorderRoute = TableActionRoute(HttpMethod.Post, "reorder")
    private val CreateRoute = TableActionRoute(HttpMethod.Get, "create")
    private val InsertRoute = TableActionRoute(HttpMethod.Post, "create")

    private val EditRoute = RecordActionRoute(HttpMethod.Get, "edit")
    private val ReviewRoute = RecordActionRoute(HttpMethod.Post, "review")
    private val ContinueEditingRoute = RecordActionRoute(HttpMethod.Post, "edit")
    private val DeleteRoute = RecordActionRoute(HttpMethod.Post, "delete")
    private val UpdateRoute = RecordActionRoute(HttpMethod.Post, "update")


    private val List: RoutedTableAction
    private val Reorder: RoutedTableAction
    private val Create: RoutedTableAction
    private val Insert: RoutedTableAction

    private val Edit: RoutedRecordAction
    private val Review: RoutedRecordAction
    private val ContinueEditing: RoutedRecordAction
    private val Delete: RoutedRecordAction
    private val Update: RoutedRecordAction

    init {
        List = RoutedTableAction(ListRoute, List(
                createRoute = CreateRoute, reorderRoute = ReorderRoute, editRoute = EditRoute, transformTitle = Companion::fixIfBlank
        ))
        Reorder = RoutedTableAction(ReorderRoute, Reorder())
        Create = RoutedTableAction(CreateRoute, Create(createRoute = CreateRoute))
        Insert = RoutedTableAction(InsertRoute, Insert(listRoute = ListRoute))

        val edit = Edit(deleteRoute = DeleteRoute, reviewRoute = ReviewRoute)
        Edit = RoutedRecordAction(EditRoute, edit)
        Review = RoutedRecordAction(ReviewRoute, Review(
                continueEditingRoute = ContinueEditingRoute, updateRoute = UpdateRoute
        ))
        ContinueEditing = RoutedRecordAction(ContinueEditingRoute, edit)
        Delete = RoutedRecordAction(DeleteRoute, Delete(listRoute = ListRoute))
        Update = RoutedRecordAction(UpdateRoute, Update(listRoute = ListRoute))
    }

    private val tableActions: List<RoutedTableAction> = listOf(List, Reorder, Create, Insert)
    private val recordActions: List<RoutedRecordAction> = listOf(Edit, Review, ContinueEditing, Delete, Update)

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
                        .single { it.route.method == httpRequest.method && it.route.keyword == pSegm[1] }.action
                findTableAndRun(call, pSegm[0]) { table ->
                    action(env, call, table, httpRequest.query, httpRequest.post)
                }
            }

            pSegm.size == 3 -> {
                val action = recordActions                  // {METHOD} /admin/{table}/action/{id}/
                        .single { it.route.method == httpRequest.method && it.route.keyword == pSegm[1] }.action
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
                        tables.map { Link(ListRoute.addressOf(env, it), fixIfBlank(it.displayName), it.count.toString()) }
                ) // todo: webSocket reactive update
        )
    }

    private inline suspend fun findTableAndRun(call: ApplicationCall, tableRoute: String, code: (Table<*, *>) -> Unit) {
        val table = tables.firstOrNull { it.route == tableRoute }
                ?: return call.respondText("Not found", ContentType.Text.Plain, HttpStatusCode.NotFound)

        code(table)
    }

    private inline suspend fun <E : Any, ID> findOneAndRun(
            call: ApplicationCall, table: Table<E, ID>, recordIDStr: String, code: (E) -> Unit
    ) {
        val id = table.stringToId(recordIDStr)
        val item = table.findOne(id)
                ?: return call.respondText("Item was not found", ContentType.Text.Plain, HttpStatusCode.NotFound)

        code(item)
    }

    private companion object {
        private fun fixIfBlank(str: String) = if (str.isBlank()) " ( blank ) " else str
    }

}
