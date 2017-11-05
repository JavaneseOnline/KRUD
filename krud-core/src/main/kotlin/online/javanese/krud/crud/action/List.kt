package online.javanese.krud.crud.action

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import online.javanese.krud.WebEnv
import online.javanese.krud.crud.*
import online.javanese.krud.template.Content
import online.javanese.krud.template.Link

internal fun List(
        createRoute: TablePageRoute, reorderRoute: TableActionRoute, editRoute: RecordPageRoute,
        transformTitle: (String) -> String
): TableEndpoint = { env, call, table, _, _ ->
    captureEAndReturnList(createRoute, reorderRoute, editRoute, env, call, table, transformTitle)
}
private suspend fun <E : Any> captureEAndReturnList(
        createRoute: TablePageRoute, reorderRoute: TableActionRoute, editRoute: RecordPageRoute,
        env: WebEnv, call: ApplicationCall, table: Table<E, *>,
        transformTitle: (String) -> String
) = call.respondHtml {
    val all = table.findAll()
    val createNew = Link(createRoute.addressOf(env, table), " + create new")
    env.template(
            this,
            "${table.displayName} — Crud",
            listOf(when (table.sort) {
                is Sort.NoneOrImplicit -> Content.LinkList(
                        table.displayName,
                        all.map { createLinkToEditRecord(editRoute, env, table, it, transformTitle) } + createNew
                )
                is Sort.Explicit<*> -> Content.SortableLinkList(
                        table.displayName,
                        all.map { createLinkToEditRecord(editRoute, env, table, it, transformTitle) to table.getId(it).toString() } + (createNew to null),
                        reorderRoute.addressOf(env, table)
                )
            })
    )
}
private fun <T : Any> createLinkToEditRecord(editRoute: RecordPageRoute, env: WebEnv, table: Table<T, *>, record: T, transformTitle: (String) -> String) =
        Link(editRoute.addressOf(env, table, record), transformTitle(table.getTitle(record)))
