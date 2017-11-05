package online.javanese.krud.template

import kotlinx.html.FlowContent
import kotlinx.html.HTML

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

    /**
     * Just a list of links.
     */
    class LinkList(
            val title: String,
            val links: List<Link>
    ) : Content()

    /**
     * A list of links which can be sorted.
     * 'ids[]' parameter will be POSTed to [updateAction] endpoint.
     */
    class SortableLinkList(
            val title: String,
            val linksAndIds: List<Pair<Link, String?>>,
            val updateAction: String
    ) : Content()

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
    ) : Content()

    /**
     * Show block with title and custom content.
     */
    class Card(
            val title: String,
            val renderContent: FlowContent.() -> Unit
    ) : Content()

}
