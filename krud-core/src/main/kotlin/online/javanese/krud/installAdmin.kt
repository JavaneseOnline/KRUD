package online.javanese.krud

import io.ktor.application.call
import io.ktor.http.HttpMethod
import io.ktor.request.receiveParameters
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.util.StringValues
import io.ktor.websocket.webSocket

fun Route.installAdmin(admin: AdminPanel) {
    get("") {
        admin.dashboard(call)
    }

    get("{module}/{tail...}") {
        admin.http(
                call,
                HttpMethod.Get,
                call.parameters["module"]!!,
                call.parameters.getAll("tail")!!,
                call.parameters,
                StringValues.Empty
        )
    }

    post("{module}/{tail...}") {
        admin.http(
                call,
                HttpMethod.Post,
                call.parameters["module"]!!,
                call.parameters.getAll("tail")!!,
                call.parameters,
                call.receiveParameters()
        )
    }

    webSocket(path = "{module}/{tail...}") {
        admin.webSocket(
                call,
                call.parameters["module"]!!,
                call.parameters.getAll("tail")!!,
                call.parameters,
                incoming,
                outgoing
        )
    }
}
