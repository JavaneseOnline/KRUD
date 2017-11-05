package online.javanese.krud.template

import kotlinx.html.*
import online.javanese.krud.FrontendDependencies
import online.javanese.krud.NoFrontendDependencies

/**
 * Represents a UI control.
 */
interface Control {

    /**
     * HTML `id` and `name`
     */
    val id: String

    /**
     * Template may decorate control if supports its type.
     */
    val type: Type

    /**
     * User-visible title/label, used by template.
     */
    val title: String


    /**
     * CSS and JS files required by this control.
     */
    val frontendDependencies: FrontendDependencies


    /**
     * Render this Control on [html] with [value] and [classes].
     */
    fun render(html: FlowContent, value: String, classes: String?)

    enum class Type {
        Input, TextArea, Custom
    }
}

/**
 * A plain HTML Input tag.
 */
class TextInput(
        override val id: String,
        override val title: String,
        private val editable: Boolean = true
) : Control {
    override val type: Control.Type get() = Control.Type.Input
    override val frontendDependencies: FrontendDependencies get() = NoFrontendDependencies

    override fun render(html: FlowContent, value: String, classes: String?) {
        html.input(type = InputType.text, classes = classes) {
            this@input.name = this@TextInput.id
            this@input.id = this@TextInput.id
            this@input.readonly = !this@TextInput.editable
            this@input.value = value
        }
    }

    companion object : (String, String) -> Control {
        override fun invoke(name: String, title: String): Control =
                TextInput(name, title)
    }
}

/**
 * Multiline HTML TextArea tag.
 */
class TextArea(
        override val id: String,
        override val title: String,
        private val editable: Boolean = true
) : Control {
    override val type: Control.Type get() = Control.Type.TextArea
    override val frontendDependencies: FrontendDependencies get() = NoFrontendDependencies

    override fun render(html: FlowContent, value: String, classes: String?) {
        html.textArea(classes = classes) {
            this@textArea.name = this@TextArea.id
            this@textArea.id = this@TextArea.id
            this@textArea.readonly = !this@TextArea.editable

            +value
        }
    }

    companion object : (String, String) -> Control {
        override fun invoke(name: String, title: String): Control =
                TextArea(name, title)
    }
}

/**
 * HTML TextArea which will be replaced
 * with CodeMirror editor on the client-side.
 */
class HtmlCodeMirror constructor(
        override val id: String,
        override val title: String
) : Control {
    override val type: Control.Type get() = Control.Type.Custom

    override val frontendDependencies: FrontendDependencies = object : FrontendDependencies {
        override fun requiredCss(staticPath: String): Set<String> = setOf(
                "$staticPath/codemirror_ambiance.min.css"
        )

        override fun requiredJs(staticPath: String): Set<String> = setOf(
                "$staticPath/codemirror_html.min.js",
                "$staticPath/codemirror_html_init.js"
        )
    }

    override fun render(html: FlowContent, value: String, classes: String?) {

        html.div(classes = "codemirror-html${if (classes == null) "" else ' ' + classes}") {
            style = "padding-bottom: 16px"

            label("someClass") {
                for_ = ""
                +this@HtmlCodeMirror.title
            }

            textArea {
                this@textArea.id = this@HtmlCodeMirror.id
                this@textArea.name = this@HtmlCodeMirror.id

                +value
            }
        }
    }

    companion object : (String, String) -> Control {
        override fun invoke(name: String, title: String): Control =
                HtmlCodeMirror(name, title)
    }
}

/**
 * No control. For fields which are not visible.
 */
object EmptyControl : Control {
    override val id: String get() = "unused"
    override val type: Control.Type get() = Control.Type.Custom // don't decorate me
    override val title: String get() = "Won't be rendered"
    override val frontendDependencies: FrontendDependencies get() = NoFrontendDependencies

    override fun render(html: FlowContent, value: String, classes: String?) {
        // no-op
    }
}
