package online.javanese.krud.stat

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import kotlinx.html.*
import online.javanese.krud.HttpRequest
import online.javanese.krud.Module
import online.javanese.krud.WebEnv
import online.javanese.krud.WsRequest
import online.javanese.krud.template.Content

class HardwareStat : Module {

    override val name: String get() = "Hardware"

    private val units = arrayOf("B", "KiB", "MiB", "GiB")

    override suspend fun summary(env: WebEnv): Content {
        val rt = Runtime.getRuntime()
        val maxBytes = rt.maxMemory()
        val allocatedBytes = rt.totalMemory()
        val usedBytes = allocatedBytes - rt.freeMemory()

        val max = maxBytes.normalized()
        val allocated = allocatedBytes.normalized()
        val used = usedBytes.normalized()

        return Content.Card("Hardware") {
            h6 { +"Heap memory" }

            ul {
                li { +"Used: $used" }
                li { +"Allocated: $allocated" }
                li { +"Max: $max" }
            }
        }
    }

    private fun Long.normalized(): String {
        var unitIdx = 0
        var bytez = toDouble()
        while (bytez > 1536 && unitIdx < units.size-1) {
            bytez /= 1024
            unitIdx++
        }

        return "%.2f %s".format(bytez, units[unitIdx])
    }

    override suspend fun http(env: WebEnv, call: ApplicationCall, httpRequest: HttpRequest) {
        val summary = summary(env)
        call.respondHtml {
            env.template(this, "Hardware", listOf(summary))
        }
    }

    override suspend fun webSocket(routePrefix: String, request: WsRequest) {
        request.call.respondText(
                "This module does not support WebSocket.", ContentType.Text.Plain,
                HttpStatusCode.MethodNotAllowed)
    }

}
