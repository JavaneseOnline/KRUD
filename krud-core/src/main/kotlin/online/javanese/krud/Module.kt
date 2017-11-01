package online.javanese.krud

import online.javanese.krud.template.ModuleTemplate
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.HttpMethod
import org.jetbrains.ktor.util.ValuesMap

/**
 * Describes a part of admin panel.
 * @see online.javanese.krud.crud.Crud
 */
interface Module {

    /**
     * This name is going to be shown in the sidebar.
     */
    val name: String

    /**
     * Handle a request. Finally, must call `call.respond...`.
     */
    suspend fun request(
            call: ApplicationCall,
            routePrefix: String,
            template: ModuleTemplate,
            method: HttpMethod,
            pathSegments: List<String>,
            query: ValuesMap,
            post: ValuesMap
    )
}
