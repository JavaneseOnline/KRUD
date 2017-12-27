package online.javanese.krud.template.control

import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.input
import online.javanese.krud.FrontendDependencies
import online.javanese.krud.NoFrontendDependencies

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

    companion object

}

val TextInput.Companion.Editable: (String, String) -> Control
    get() = { name, title -> TextInput(name, title, true) }

val TextInput.Companion.ReadOnly: (String, String) -> Control
    get() = { name, title -> TextInput(name, title, false) }
