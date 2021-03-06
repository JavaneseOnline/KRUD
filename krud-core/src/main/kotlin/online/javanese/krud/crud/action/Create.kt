package online.javanese.krud.crud.action

import io.ktor.html.respondHtml
import online.javanese.krud.crud.Column
import online.javanese.krud.crud.TableEndpoint
import online.javanese.krud.crud.TablePageRoute
import online.javanese.krud.template.Content

internal fun Create(createRoute: TablePageRoute): TableEndpoint = { env, call, table, _, post ->

    call.respondHtml {
        env.template(
                this,
                "Creating new ${table.displayName} — Crud",
                listOf(Content.Form(
                        "New ${table.displayName}",
                        Content.Form.Mode.Create,
                        table.cols.asSequence()
                                .map(Column<*>::createControl)
                                .filterNotNull().map { it to emptyList<String>() }
                                .toList(),
                        createRoute.addressOf(env, table)
                ))
        )
    }
}
