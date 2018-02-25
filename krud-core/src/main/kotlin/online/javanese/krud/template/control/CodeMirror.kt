package online.javanese.krud.template.control

import kotlinx.html.*
import online.javanese.krud.FrontendDependencies

/**
 * TextArea which will be replaced
 * with CodeMirror editor on the client-side.
 */
class CodeMirror constructor(
        override val id: String,
        override val title: String,
        private val modeSlug: String
) : Control {
    override val type: Control.Type get() = Control.Type.Custom

    override val frontendDependencies: FrontendDependencies = object : FrontendDependencies {
        override fun requiredCss(staticPath: String): Set<String> = setOf(
                "$staticPath/codemirror_ambiance.min.css"
        )

        override fun requiredJs(staticPath: String): Set<String> = setOf(
                "$staticPath/codemirror_$modeSlug.min.js",
                "$staticPath/codemirror_${modeSlug}_init.js"
        )
    }

    override fun render(html: FlowContent, values: List<String>, classes: String?) {

        html.div(classes = "codemirror-$modeSlug${if (classes == null) "" else ' ' + classes}") {

            label("someClass") {
                htmlFor = ""
                +this@CodeMirror.title
            }

            textArea {
                this@textArea.id = this@CodeMirror.id
                this@textArea.name = this@CodeMirror.id

                values.singleOrNull()?.let { +it }
            }
        }
    }

    companion object Factories

}

val CodeMirror.Factories.Html : (String, String) -> Control
    get() = { name, title ->
        CodeMirror(name, title, "html")
    }
