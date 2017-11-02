package online.javanese.krud

import io.ktor.application.ApplicationCall

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
     * Handle a request. Finally, implementation must call `call.respond...`.
     */
    suspend fun request(
            env: WebEnv,
            call: ApplicationCall,
            httpRequest: HttpRequest
    )

}
