import online.javanese.krud.AdminPanel
import online.javanese.krud.RoutedModule
import online.javanese.krud.crud.Crud
import online.javanese.krud.crud.IdCol
import online.javanese.krud.crud.InMemoryTable
import online.javanese.krud.crud.TextCol
import online.javanese.krud.template.MaterialTemplate
import online.javanese.krud.template.TextArea
import org.jetbrains.ktor.content.files
import org.jetbrains.ktor.content.static
import org.jetbrains.ktor.content.staticRootFolder
import org.jetbrains.ktor.host.embeddedServer
import org.jetbrains.ktor.http.HttpMethod
import org.jetbrains.ktor.netty.Netty
import org.jetbrains.ktor.request.receiveParameters
import org.jetbrains.ktor.response.respondRedirect
import org.jetbrains.ktor.routing.get
import org.jetbrains.ktor.routing.post
import org.jetbrains.ktor.routing.route
import org.jetbrains.ktor.routing.routing
import org.jetbrains.ktor.util.ValuesMap
import java.io.File
import java.util.*

object KrudTestServer {

    @JvmStatic
    fun main(args: Array<String>) {

        check(args.size == 2 && args[0] == "--static") {
            "Must specify static resources dir. " +
                    "Sample usage: java -Xms8M -Xmx8M -Xss180K " +
                    "-jar KrudTestServer.jar " +
                    "--static \"/home/<user>/IdeaProjects/krud/krud-core/src/main/resources/static\""
        }
        val staticResDir = args[1]

        val admin = AdminPanel(
                "/admin",
                MaterialTemplate("/admin", "/admin/path/to/static/resources"),
                RoutedModule("crud", Crud(
                        InMemoryTable(
                                "item", "Item", Item::id, Item::name, UUID::fromString,
                                listOf(
                                        IdCol(Item::id),
                                        TextCol(Item::name),
                                        TextCol(Item::text, controlFactory = TextArea),
                                        TextCol(Item::code, controlFactory = TextArea)
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
                ))
        )

        embeddedServer(Netty, 8081) {

            routing {
                route("/admin/") {
//                    intercept(ApplicationCallPipeline.Infrastructure) { … } TODO

                    get("") {
                        call.respondRedirect("/admin/crud/")
                    }

                    get("{module}/{tail...}") {
                        admin.request(
                                call,
                                HttpMethod.Get,
                                call.parameters["module"]!!,
                                call.parameters.getAll("tail")!!,
                                call.parameters,
                                ValuesMap.Empty
                        )
                    }

                    post("{module}/{tail...}") {
                        admin.request(
                                call,
                                HttpMethod.Post,
                                call.parameters["module"]!!,
                                call.parameters.getAll("tail")!!,
                                call.parameters,
                                call.receiveParameters()
                        )
                    }

                    static("path/to/static/resources") {
                        val localStaticDirFile = File(staticResDir)
                        staticRootFolder = localStaticDirFile.parentFile
                        files(localStaticDirFile.name)
                    }

                }
            }

        }.start(true)

    }

}
