package online.javanese.krud.template

import kotlinx.html.FlowContent
import kotlinx.html.HTML
import online.javanese.krud.CompositeFrontendDependencies
import online.javanese.krud.FrontendDependencies
import online.javanese.krud.NoFrontendDependencies

typealias AdminTemplate = (
        root: HTML,
        titleText: String,
        sidebarLinks: List<Link>,
        contents: List<Content>
) -> Unit

typealias ModuleTemplate = (
        root: HTML,
        titleText: String,
        contents: List<Content>
) -> Unit

class Link(
        val href: String,
        val text: String,
        val badge: String? = null
)

/**
 * Represents content which can be renderen in a template.
 */
sealed class Content {

    abstract val dependencies: FrontendDependencies

    /**
     * Just a list of links.
     */
    class LinkList(
            val title: String,
            val links: List<Link>
    ) : Content() {
        override val dependencies: FrontendDependencies get() = NoFrontendDependencies
    }

    /**
     * A list of links which can be sorted.
     * 'ids[]' parameter will be POSTed to [updateAction] endpoint.
     */
    class SortableLinkList(
            val title: String,
            val linksAndIds: List<Pair<Link, String?>>,
            val updateAction: String
    ) : Content() {
        override val dependencies: FrontendDependencies get() = NoFrontendDependencies
    }

    /**
     * Represents HTML form.
     * Method is POST (in forms, GET is stupid, other are not allowed.
     */
    class Form(
            val title: String,
            val mode: Mode,
            val controlsAndValues: List<Pair<Control, String>>,
            val submitAction: String
    ) : Content() {

        override val dependencies: FrontendDependencies =
                CompositeFrontendDependencies(
                        controlsAndValues.map { (ctl, _) -> ctl.frontendDependencies }
                )

        sealed class Mode {
            object Create : Mode()
            class Edit(val removeAction: String) : Mode()
        }
    }

    /**
     * Show review table before committing changes.
     */
    class Review(
            val title: String,
            val namesTitlesValues: List<Triple<String, String, String>>,
            val editAction: String,
            val updateAction: String
    ) : Content() {
        override val dependencies: FrontendDependencies get() = NoFrontendDependencies
    }

    /**
     * Show block with title and custom content.
     */
    class Card(
            val title: String,
            override val dependencies: FrontendDependencies = NoFrontendDependencies,
            val width: Width = Width.Normal,
            val renderContent: FlowContent.() -> Unit
    ) : Content() {

        enum class Width { Slim, Normal, Wide, Full }

    }

}
