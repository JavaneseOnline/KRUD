package online.javanese.krud.crud.action

import io.ktor.html.respondHtml
import online.javanese.krud.crud.Col
import online.javanese.krud.crud.TableAction
import online.javanese.krud.crud.TableActionRoute
import online.javanese.krud.template.Content

internal fun Create(createRoute: TableActionRoute): TableAction = { env, call, table, _, post ->

    call.respondHtml {
        env.template(
                this,
                "Creating new ${table.displayName} — Crud",
                Content.Form(
                        "New ${table.displayName}",
                        Content.Form.Mode.Create,
                        table.cols.asSequence()
                                .map(Col<*>::createControl)
                                .filterNotNull().map { it to "" }
                                .toList(),
                        createRoute.addressOf(env, table)
                )
        )
    }
}
