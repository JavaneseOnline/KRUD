package online.javanese.krud

import online.javanese.krud.template.AdminTemplate
import online.javanese.krud.template.Link
import online.javanese.krud.template.ModuleTemplate
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.http.HttpMethod
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.response.respondText
import org.jetbrains.ktor.util.ValuesMap

/**
 * This is a base, a host, a router for [Module]s.
 */
class AdminPanel(
        private val routePrefix: String,
        template: AdminTemplate,
        private vararg val modules: RoutedModule
) {

    init {
        modules.checkRoutes("Module", "modules", RoutedModule::route)
    }

    private val moduleTemplate: ModuleTemplate
    init {
        val sidebarLinks = modules.map { Link("$routePrefix/${it.route}/", it.module.name) }
        moduleTemplate = { root, titleText, content -> template(root, titleText, sidebarLinks, content) }
    }

    private val webEnvs = HashMap<Module, WebEnv>(modules.size)

    /**
     * Proxies call to according [Module], if any.
     */
    suspend fun request(
            call: ApplicationCall,
            method: HttpMethod,
            modulePath: String,
            pathSegments: List<String>,
            query: ValuesMap,
            post: ValuesMap
    ) {
        val routedModule = modules.firstOrNull { it.route == modulePath }
                ?: return call.respondText("No such module.", ContentType.Text.Plain, HttpStatusCode.NotFound)

        val module = routedModule.module

        module.request(
                webEnvs.getOrPut(module) { WebEnv("$routePrefix/${routedModule.route}", moduleTemplate) },
                call, HttpRequest(method, pathSegments, query = query, post = post))
    }

}
