package online.javanese.krud

import io.ktor.content.resources
import io.ktor.content.static
import io.ktor.routing.Route

fun Route.krudStaticResources(exposeAs: String) {
    static(exposeAs) {
        resources("online/javanese/krud/static")
    }
}
