import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authentication
import io.ktor.auth.basicAuthentication
import io.ktor.content.resources
import io.ktor.content.static
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import online.javanese.krud.AdminPanel
import online.javanese.krud.RoutedModule
import online.javanese.krud.crud.Crud
import online.javanese.krud.crud.IdCol
import online.javanese.krud.crud.InMemoryTable
import online.javanese.krud.crud.TextCol
import online.javanese.krud.installAdmin
import online.javanese.krud.stat.*
import online.javanese.krud.template.HtmlCodeMirror
import online.javanese.krud.template.MaterialTemplate
import online.javanese.krud.template.TextArea
import java.util.*

object KrudTestServer {

    /**
     * Starts test server.
     * Required static dir address to be specified through CLI arguments.
     * After start, you should be able to see a working admin-panel
     * at http://localhost:8081/admin/.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        val noUa = UserAgent("", "", "")
        val stat = HitStat(InMemoryStatTable({ noUa }, ignoreRequestUri = { it.endsWith(".js") || it.endsWith(".css") }))
        val admin = AdminPanel(
                "/admin",
                MaterialTemplate("/admin", "/static"),
                RoutedModule("crud", Crud(
                        InMemoryTable(
                                "item", "Item", Item::id, Item::name, UUID::fromString,
                                listOf(
                                        IdCol(Item::id),
                                        TextCol(Item::name),
                                        TextCol(Item::text, controlFactory = TextArea),
                                        TextCol(Item::code, controlFactory = HtmlCodeMirror)
                                ),
                                listOf(Item(UUID(0L, 0L), "Whatever", "", "")),
                                { map -> Item(
                                        id = map["id"]?.let(UUID::fromString) ?: UUID.randomUUID(),
                                        name = map["name"]!!, // ^ update             ^ create
                                        text = map["text"]!!,
                                        code = map["code"]!!
                                ) },
                                sortable = true
                        )
                )),
                RoutedModule("hwStat", HardwareStat()),
                RoutedModule("stat", stat)
        )

        embeddedServer(Netty, 8081) {
            install(WebSockets)

            routing {

                installHitStatInterceptor(stat)

                get("/test/") {
                    call.respondText("This is a test.", ContentType.Text.Plain)
                }

                route("/admin/") {

                    installAdmin(admin)

                    authentication {
                        basicAuthentication("Admin") { cred ->
                            if (cred.name == "admin" && cred.password == "admin") UserIdPrincipal("admin")
                            else null
                        }
                    }

                }

                static("static") {
//                    val localStaticDirFile = File(staticResDir)
//                    staticRootFolder = localStaticDirFile.parentFile
//                    files(localStaticDirFile.name)
                    resources("static")
                }
            }

        }.start(true)

    }

}
