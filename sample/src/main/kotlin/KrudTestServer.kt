import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authentication
import io.ktor.auth.basicAuthentication
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
import online.javanese.krud.crud.*
import online.javanese.krud.installAdmin
import online.javanese.krud.krudStaticResources
import online.javanese.krud.stat.*
import online.javanese.krud.template.control.CodeMirror
import online.javanese.krud.template.control.Html
import online.javanese.krud.template.MaterialTemplate
import online.javanese.krud.template.control.TextArea
import java.util.*

object KrudTestServer {

    /**
     * Starts test server.
     * After start, you should be able to see a working admin-panel
     * at http://localhost:8081/admin/.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        val noUa = UserAgent("", "", "")
        val stat = HitStat(InMemoryStatTable({ noUa }, ignoreRequestUri = { it.endsWith(".js") || it.endsWith(".css") }))
        val admin = AdminPanel(
                "/admin",
                MaterialTemplate("/admin", "/krud-static"),
                RoutedModule("crud", Crud(
                        InMemoryTable(
                                "item", "Item", Item::id, Item::name, UUID::fromString,
                                listOf(
                                        IdCol(Item::id),
                                        TextCol(Item::name),
                                        TextCol(Item::text, createControlFactory = TextArea),
                                        TextCol(Item::code, createControlFactory = CodeMirror.Html),
                                        BooleanCol(Item::cool),
                                        EnumeratedCol(Item::colour, EnumColAdapter<Colour>())
                                ),
                                listOf(
                                        Item(UUID(0L, 0L), "Cool item", "", "", true, Colour.Black),
                                        Item(UUID(0L, 1L), "OK item", "", "", false, Colour.DarkerBlack)
                                ),
                                { map -> Item(
                                        id = map["id"]?.let(UUID::fromString) ?: UUID.randomUUID(),
                                        name = map["name"]!!, // ^ update             ^ create
                                        text = map["text"]!!,
                                        code = map["code"]!!,
                                        cool = map["cool"] == "true",
                                        colour = Colour.valueOf(map["colour"]!!)
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

                krudStaticResources("krud-static")
            }

        }.start(true)

    }

}
