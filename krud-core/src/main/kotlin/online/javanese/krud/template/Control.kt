package online.javanese.krud.template

import kotlinx.html.*
import online.javanese.krud.FrontendDependencies
import online.javanese.krud.NoFrontendDependencies

/**
 * Represents a UI control.
 */
interface Control {

    /**
     * HTML `id` and `name`.
     * Typically, this means `<input type="..." id="{id}" name="{name}" ... />`,
     * but this is not guaranteed.
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
        Input, TextArea, CheckBox, Select, Custom
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

    companion object Editable : (String, String) -> Control {
        override fun invoke(name: String, title: String): Control =
                TextInput(name, title, true)
    }

    object ReadOnly : (String, String) -> Control {
        override fun invoke(name: String, title: String): Control =
                TextInput(name, title, false)
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
 * Input with `type=checkbox` for `Boolean` values.
 *
 * Shipped with hidden input which helps converting 'checked' state
 * into a valid boolean value.
 */
class CheckBox(
        override val id: String,
        override val title: String
) : Control {
    override val type: Control.Type get() = Control.Type.CheckBox
    override val frontendDependencies: FrontendDependencies get() = NoFrontendDependencies

    override fun render(html: FlowContent, value: String, classes: String?) {
        html.checkBoxInput(classes = classes) {
            this@checkBoxInput.id = this@CheckBox.id
            this@checkBoxInput.checked = value == "true"

            // convert on/undefined to true/false
            this@checkBoxInput.onChange = this@CheckBox.id + "_crutch.value = this.checked"
        }
        html.hiddenInput {
            this@hiddenInput.id = this@CheckBox.id + "_crutch"
            this@hiddenInput.name = this@CheckBox.id
            this@hiddenInput.value = value
        }
    }

    companion object : (String, String) -> Control {
        override fun invoke(name: String, title: String): Control =
                CheckBox(name, title)
    }
}

/**
 * Renders HTML <select>, you know.
 */
class ComboBox(
        override val id: String,
        override val title: String,
        private val names: List<String>,
        private val titles: List<String>
) : Control {

    init {
        if (names.size != titles.size)
            throw IllegalArgumentException("Names: ${names.size} strings; titles: ${titles.size} strings. Must be equal.")
    }

    override val type: Control.Type get() = Control.Type.Select
    override val frontendDependencies: FrontendDependencies get() = NoFrontendDependencies

    override fun render(html: FlowContent, value: String, classes: String?) {
        html.select(classes = classes) {
            this@select.id = this@ComboBox.id
            this@select.name = this@ComboBox.id

            names.forEachIndexed { i, name ->
                option {
                    this@option.value = name
                    if (value == name) selected = true

                    +titles[i]
                }
            }
        }
    }

    companion object : (String, String, List<String>, List<String>) -> Control {
        override fun invoke(name: String, title: String, names: List<String>, titles: List<String>): Control =
                ComboBox(name, title, names, titles)
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
