package online.javanese.krud

import online.javanese.krud.template.ModuleTemplate
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.HttpMethod
import org.jetbrains.ktor.util.ValuesMap

interface Module {

    val name: String

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
