package online.javanese.krud

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.util.ValuesMap
import io.ktor.websocket.Frame
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.SendChannel
import online.javanese.krud.template.AdminTemplate
import online.javanese.krud.template.Link
import online.javanese.krud.template.ModuleTemplate

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

    private val webEnvs = HashMap<RoutedModule, WebEnv>(modules.size)
    private val RoutedModule.webEnv
        get() = webEnvs.getOrPut(this) { WebEnv("$routePrefix/$route", moduleTemplate) }

    /**
     * Renders a dashboard.
     */
    suspend fun dashboard(call: ApplicationCall) {
        val summaries = modules.map { it.module.summary(it.webEnv) }

        call.respondHtml {
            moduleTemplate(this, "Dashboard", summaries)
        }
    }

    /**
     * Proxies call to according [Module], if any.
     */
    suspend fun http(
            call: ApplicationCall,
            method: HttpMethod,
            modulePath: String,
            pathSegments: List<String>,
            query: ValuesMap,
            post: ValuesMap
    ) {
        val routedModule = modules.firstOrNull { it.route == modulePath }
                ?: return call.respondText("No such module.", ContentType.Text.Plain, HttpStatusCode.NotFound)

        routedModule.module.http(
                routedModule.webEnv,
                call, HttpRequest(method, pathSegments, query = query, post = post))
    }

    suspend fun webSocket(
            call: ApplicationCall,
            modulePath: String,
            pathSegments: List<String>,
            query: ValuesMap,
            incoming: ReceiveChannel<Frame>,
            outgoing: SendChannel<Frame>
    ) {
        val routedModule = modules.firstOrNull { it.route == modulePath }
                ?: return call.respondText("No such module.", ContentType.Text.Plain, HttpStatusCode.NotFound)

        routedModule.module.webSocket(
                routePrefix,
                WsRequest(call, pathSegments, query = query, incoming = incoming, outgoing = outgoing)
        )
    }

}
