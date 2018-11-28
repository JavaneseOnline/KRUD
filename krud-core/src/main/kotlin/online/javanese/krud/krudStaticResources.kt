package online.javanese.krud

import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.routing.Route


fun Route.krudStaticResources(exposeAs: String) {
    static(exposeAs) {
        resources("online/javanese/krud/static")
    }
}
