package online.javanese.krud.crud

import online.javanese.krud.WebEnv
import online.javanese.krud.template.Content
import online.javanese.krud.template.Link
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.html.respondHtml

internal fun List(
        createRoute: TableActionRoute, reorderRoute: TableActionRoute, editRoute: RecordActionRoute,
        transformTitle: (String) -> String
): TableAction = { env, call, table, _, _ ->
    captureEAndReturnList(createRoute, reorderRoute, editRoute, env, call, table, transformTitle)
}
private suspend fun <E : Any> captureEAndReturnList(
        createRoute: TableActionRoute, reorderRoute: TableActionRoute, editRoute: RecordActionRoute,
        env: WebEnv, call: ApplicationCall, table: Table<E, *>,
        transformTitle: (String) -> String
) = call.respondHtml {
    val all = table.findAll()
    val createNew = Link(createRoute.addressOf(env, table), " + create new")
    env.template(
            this,
            "${table.displayName} — Crud",
            when (table.sort) {
                is Sort.NoneOrImplicit -> Content.LinkList(
                        table.displayName,
                        all.map { createLinkToEditRecord(editRoute, env, table, it, transformTitle) } + createNew
                )
                is Sort.Explicit<*> -> Content.SortableLinkList(
                        table.displayName,
                        all.map { createLinkToEditRecord(editRoute, env, table, it, transformTitle) to table.getId(it).toString() } + (createNew to null),
                        reorderRoute.addressOf(env, table)
                )
            }
    )
}
private fun <T : Any> createLinkToEditRecord(editRoute: RecordActionRoute, env: WebEnv, table: Table<T, *>, record: T, transformTitle: (String) -> String) =
        Link(editRoute.addressOf(env, table, record), transformTitle(table.getTitle(record)))
