package online.javanese.krud

import online.javanese.krud.template.AdminTemplate
import online.javanese.krud.template.Link
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.http.HttpMethod
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.response.respondText
import org.jetbrains.ktor.util.ValuesMap


class AdminPanel(
        private val routePrefix: String,
        private val template: AdminTemplate,
        private vararg val modules: RoutedModule
) {

    init {
        modules.checkRoutes("Module", "modules", RoutedModule::route)
    }

    private val sidebarLinks = modules.map { Link("$routePrefix/${it.route}/", it.module.name) }

    suspend fun request(
            call: ApplicationCall,
            method: HttpMethod,
            modulePath: String,
            pathSegments: List<String>,
            parameters: ValuesMap
    ) {
        val routedModule = modules.firstOrNull { it.route == modulePath }
                ?: return call.respondText("No such module.", ContentType.Text.Plain, HttpStatusCode.NotFound)

        val module = routedModule.module

        module.request(call,
                "$routePrefix/${routedModule.route}",
                { root, titleText, content -> template(root, titleText, sidebarLinks, content) },
                method,
                pathSegments,
                parameters)
    }

}
