package online.javanese.krud.template

import kotlinx.html.*

typealias AdminTemplate = (
        root: HTML,
        titleText: String,
        sidebarLinks: List<Link>,
        content: Content
) -> Unit

typealias ModuleTemplate = (
        root: HTML,
        titleText: String,
        content: Content
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

    class LinkList(
            val title: String,
            val links: List<Link>
    ) : Content()

    class SortableLinkList(
            val title: String,
            val linksAndIds: List<Pair<Link, String?>>,
            val updateAction: String
    ) : Content()

    class Form(
            val title: String,
            val mode: Mode,
            val controlsAndValues: List<Pair<Control, String>>,
            val submitAction: String
    ) : Content() {
        sealed class Mode(val name: String) {
            object Create : Mode("Create")
            class Edit(val removeAction: String) : Mode("Edit")
        }
    }

    class Review(
            val title: String,
            val namesTitlesValues: List<Triple<String, String, String>>,
            val editAction: String,
            val updateAction: String
    ) : Content()

}


/**
 * Represents a UI control.
 */
interface Control {
    val id: String
    val type: Type
    val title: String

    fun render(html: FlowContent, value: String, classes: String?)

    enum class Type {
        Input, TextArea, Custom
    }
}

class TextInput(
        private val name: String,
        override val id: String,
        override val title: String,
        private val editable: Boolean = true
) : Control {
    override fun render(html: FlowContent, value: String, classes: String?) {
        html.apply {
            input(type = InputType.text, classes = classes) {
                this@input.name = this@TextInput.name
                this@input.value = value
                this@input.id = this@TextInput.id
                this@input.readonly = !this@TextInput.editable
            }
        }
    }

    override val type: Control.Type get() = Control.Type.Input
}
