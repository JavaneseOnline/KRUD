package online.javanese.krud.stat

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.websocket.Frame
import kotlinx.coroutines.experimental.delay
import kotlinx.html.*
import online.javanese.krud.HttpRequest
import online.javanese.krud.Module
import online.javanese.krud.WebEnv
import online.javanese.krud.WsRequest
import online.javanese.krud.template.Content

class HardwareStat : Module {

    override val name: String get() = "Hardware"

    private val units = arrayOf("B", "KiB", "MiB", "GiB")

    private val divId = "hwstat${hashCode()}"

    override suspend fun summary(env: WebEnv): Content {
        val rt = Runtime.getRuntime()
        val maxBytes = rt.maxMemory()
        val allocatedBytes = rt.totalMemory()
        val usedBytes = allocatedBytes - rt.freeMemory()

        val max = maxBytes.normalized()
        val allocated = allocatedBytes.normalized()
        val used = usedBytes.normalized()

        return Content.Card(
                "Hardware"/*,
                dependencies = SimpleFrontendDependencies(
                        js = setOf("https://cdnjs.cloudflare.com/ajax/libs/vue/2.5.3/vue.min.js")
                )*/
        ) {
            h6 { +"Heap memory" }

            div {
                id = divId

                ul {
                    li { +"Used: $used" }
                    li { +"Allocated: $allocated" }
                    li { +"Max: $max" }
                }
            }

            script(src = "https://cdnjs.cloudflare.com/ajax/libs/vue/2.5.3/vue.min.js")
            script {
                unsafe {
+"""
'use strict';

var units = [ "B", "KiB", "MiB", "GiB" ];
function normalized(bytes) {
    if (bytes == -1) return '...';

    var unitIdx = 0;
    var bytez = bytes
    while (bytez > 1536 && unitIdx < units.length-1) {
        bytez /= 1024;
        unitIdx++;
    }
    return bytez.toFixed(2) + ' ' + units[unitIdx];
}

new Vue({
    el: '#$divId',
    data: {
        usedBytes: -1,
        allocatedBytes: -1,
        maxBytes: -1,
        status: 'Connecting...',
        connection: null
    },
    computed: {
        used: function() {
            return normalized(this.usedBytes);
        },
        allocated: function() {
            return normalized(this.allocatedBytes);
        },
        max: function() {
            return normalized(this.maxBytes);
        }
    },
    template:
        '<div><ul><li>Used: {{used}}</li>\n<li>Allocated: {{allocated}}</li><li>Max: {{max}}</li></ul><span>{{status}}</span></div>',
    created: function() {
        this.connection = new WebSocket('ws://' + location.host + '${env.routePrefix}/reactiveHwStat');
        var zis = this;
        this.connection.onopen = function() {
            zis.status = 'Connected.';
        }
        this.connection.onclose = function() {
            zis.status = 'Disconnected.';
        }
        this.connection.onmessage = function(message) {
            console.log(message);
            var payload = JSON.parse(message.data);
            zis.usedBytes = payload.usedBytes;
            zis.allocatedBytes = payload.allocatedBytes;
            zis.maxBytes = payload.maxBytes;
        }
        this.connection.onerror = function() {
            zis.status = 'Error.';
        }
    }
});
"""
                }
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
        if (request.pathSegments.size != 1 || request.pathSegments[0] != "reactiveHwStat") {
            return request.call.respondText(
                    "No such WebSocket endpoint.", ContentType.Text.Plain,
                    HttpStatusCode.NotFound)
        }

        val outgoing = request.outgoing
        val rt = Runtime.getRuntime()

        while (true) {
            val maxBytes = rt.maxMemory()
            val allocatedBytes = rt.totalMemory()
            val usedBytes = allocatedBytes - rt.freeMemory()
            outgoing.send(
                    Frame.Text("""{"usedBytes":$usedBytes,"allocatedBytes":$allocatedBytes,"maxBytes":$maxBytes}""")
            )
            delay(1000)
        }
    }

}
