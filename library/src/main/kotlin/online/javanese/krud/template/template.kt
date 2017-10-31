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

sealed class Content {

    class LinkList(
            val title: String,
            val links: List<Link>
    ) : Content()

    class Form(
            val title: String,
            val mode: Mode,
            val controlsAndValues: List<Pair<Control, String>>,
            val submitAction: String
    ) : Content() {
        enum class Mode { Create, Edit }
    }

    class Review(
            val title: String,
            val namesTitlesValues: List<Triple<String, String, String>>,
            val editAction: String,
            val updateAction: String
    ) : Content()

}

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
