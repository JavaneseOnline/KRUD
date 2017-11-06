package online.javanese.krud

import io.ktor.application.ApplicationCall
import online.javanese.krud.template.Content

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
     * Render short summary block in a dashboard.
     */
    suspend fun summary(env: WebEnv): Content // todo: webSocket reactive update

    /**
     * Handle a request. Finally, implementation must call `call.respond...`.
     */
    suspend fun http(
            env: WebEnv,
            call: ApplicationCall,
            httpRequest: HttpRequest
    )

    suspend fun webSocket(
            routePrefix: String,
            request: WsRequest
    )

}
