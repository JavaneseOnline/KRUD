package online.javanese.krud.crud

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.util.StringValues
import online.javanese.krud.*
import online.javanese.krud.crud.action.*
import online.javanese.krud.template.Content
import online.javanese.krud.template.Link

/**
 * Handles basic CRUD operations in context of [AdminPanel].
 */
class Crud(
        private vararg val tables: Table<*, *>
) : Module {

    init {
        tables.checkRoutes("Table", "tables", Table<*, *>::route)
    }

    override val name: String get() = "CRUD"

    private val ListRoute = TablePageRoute("list")
    private val ReorderRoute = TableActionRoute("reorder")
    private val CreateRoute = TablePageRoute("create")
    private val InsertRoute = TableActionRoute("create")

    private val EditRoute = RecordPageRoute("edit")
    private val ReviewRoute = RecordActionRoute("review")
    private val ContinueEditingRoute = RecordActionRoute("edit")
    private val DeleteRoute = RecordActionRoute("delete")
    private val UpdateRoute = RecordActionRoute("update")


    private val List: RoutedTableEndpoint
    private val Reorder: RoutedTableEndpoint
    private val Create: RoutedTableEndpoint
    private val Insert: RoutedTableEndpoint

    private val Edit: RoutedRecordEndpoint
    private val Review: RoutedRecordEndpoint
    private val ContinueEditing: RoutedRecordEndpoint
    private val Delete: RoutedRecordEndpoint
    private val Update: RoutedRecordEndpoint

    init {
        List = RoutedTableEndpoint(ListRoute, List(
                createRoute = CreateRoute, reorderRoute = ReorderRoute, editRoute = EditRoute, transformTitle = Companion::fixIfBlank
        ))
        Reorder = RoutedTableEndpoint(ReorderRoute, Reorder())
        Create = RoutedTableEndpoint(CreateRoute, Create(createRoute = CreateRoute))
        Insert = RoutedTableEndpoint(InsertRoute, Insert(listRoute = ListRoute))

        val edit = Edit(deleteRoute = DeleteRoute, reviewRoute = ReviewRoute)
        Edit = RoutedRecordEndpoint(EditRoute, edit)
        Review = RoutedRecordEndpoint(ReviewRoute, Review(
                continueEditingRoute = ContinueEditingRoute, updateRoute = UpdateRoute
        ))
        ContinueEditing = RoutedRecordEndpoint(ContinueEditingRoute, edit)
        Delete = RoutedRecordEndpoint(DeleteRoute, Delete(listRoute = ListRoute))
        Update = RoutedRecordEndpoint(UpdateRoute, Update(listRoute = ListRoute))
    }

    private val tableActions: List<RoutedTableEndpoint> = listOf(List, Reorder, Create, Insert)
    private val recordActions: List<RoutedRecordEndpoint> = listOf(Edit, Review, ContinueEditing, Delete, Update)

    suspend override fun http(
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
            env: WebEnv, call: ApplicationCall, table: Table<T, *>, recordIdStr: String, action: RecordEndpoint,
            query: StringValues, post: StringValues
    ) = findOneAndRun(call, table, recordIdStr) { record ->
        action(env, call, TableAndRecord(table, record), query, post)
    }

    override suspend fun summary(env: WebEnv): Content = Content.LinkList(
            "Tables",
            tables.map { Link(ListRoute.addressOf(env, it), fixIfBlank(it.displayName), it.count.toString()) }
    )

    private suspend fun index(env: WebEnv, call: ApplicationCall) {
        val summary = summary(env)

        call.respondHtml {
            env.template(
                    this,
                    "Crud index",
                    listOf(summary)
            )
        }
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

    override suspend fun webSocket(routePrefix: String, request: WsRequest) {
        request.call.respondText(
                "This module does not support WebSocket.", ContentType.Text.Plain,
                HttpStatusCode.MethodNotAllowed)
    }

    private companion object {
        private fun fixIfBlank(str: String) = if (str.isBlank()) " ( blank ) " else str
    }

}
