@file:JvmName("KrudTestServer")

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.*
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


/**
 * Starts test server.
 * After start, you should be able to see a working admin-panel
 * at http://localhost:8081/admin/.
 */
fun main(args: Array<String>) {
    val noUa = UserAgent("", "", "")
    val stat = HitStat(
            InMemoryStatTable { noUa }
    )
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
                                    EnumeratedCol(Item::colour, EnumColAdapter<Colour>()),
                                    MultiEnumeratedCol(Item::bestWith, EnumColAdapter<Colour>())
                            ),
                            listOf(
                                    Item(UUID(0L, 0L), "Cool item", "", "", true, Colour.Black, emptySet()),
                                    Item(UUID(0L, 1L), "OK item", "", "", false, Colour.DarkerBlack, EnumSet.of(Colour.LighterBlack, Colour.Black))
                            ),
                            { map -> Item(
                                    id = map["id"]?.let(UUID::fromString) ?: UUID.randomUUID(),
                                    name = map["name"]!!, // ^ update             ^ create
                                    text = map["text"]!!,
                                    code = map["code"]!!,
                                    cool = map["cool"] == "true",
                                    colour = Colour.valueOf(map["colour"]!!),
                                    bestWith = (map.getAll("bestWith") ?: emptySet<String>()).mapTo(EnumSet.noneOf(Colour::class.java), Colour::valueOf)
                            ) },
                            sortable = true
                    )
            )),
            RoutedModule("hwStat", HardwareStat()),
            RoutedModule("stat", stat)
    )

    embeddedServer(Netty, 8081, "127.0.0.1") {
        install(WebSockets)

        routing {

            installHitStatInterceptor(stat)

            get("/test/") {
                call.respondText("This is a test.", ContentType.Text.Plain)
            }

            route("/admin/") {

                installAdmin(admin)

                authentication {
                    basic {
                        realm = "Admin"
                        validate { cred ->
                            if (cred.name == "admin" && cred.password == "admin") UserIdPrincipal("admin")
                            else null
                        }
                    }
                }

            }

            krudStaticResources("krud-static")
        }

    }.start(true)

}
