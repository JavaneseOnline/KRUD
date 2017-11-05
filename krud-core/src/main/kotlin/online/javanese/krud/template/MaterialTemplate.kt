package online.javanese.krud.template

import kotlinx.html.*

/**
 * Template implementation based on Material Design Lite
 */
class MaterialTemplate(
        private val homePath: String,
        private val staticPath: String
) : AdminTemplate {

    private val flatButtonClasses = "mdl-button mdl-js-button mdl-js-ripple-effect"
    private val raisedColouredButtonClasses = "mdl-button mdl-js-button mdl-button--raised mdl-button--colored mdl-js-ripple-effect"
    private val raisedAccentedButtonClasses = "mdl-button mdl-js-button mdl-button--raised mdl-button--accent mdl-js-ripple-effect"

    override fun invoke(
            root: HTML,
            titleText: String,
            sidebarLinks: List<Link>,
            contents: List<Content>
    ) {
        root.apply {

            head {
                unsafe {
                    +"\n    <meta charset=\"utf-8\" />"
                    +"\n    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />"
                    +"\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0\" />\n"
                }

                title("$titleText — Admin")

                styleLink("https://fonts.googleapis.com/css?family=PT+Sans:400,700&subset=latin,cyrillic")
                styleLink("//fonts.googleapis.com/icon?family=Material+Icons")
                styleLink("https://cdnjs.cloudflare.com/ajax/libs/dialog-polyfill/0.4.3/dialog-polyfill.min.css")
                styleLink("$staticPath/admin-material.min.css")
                styleLink("$staticPath/dashboard.css")

                val styles = LinkedHashSet<String>()
                contents.forEach { content ->
                    content.dependencies.requiredCss(staticPath).forEach {
                        if (styles.add(it))
                            styleLink(it)
                    }
                }

            }

            body {

                div("demo-layout mdl-layout mdl-js-layout mdl-layout--fixed-drawer mdl-layout--fixed-header") {
                    header("demo-header mdl-layout__header mdl-color--grey-100 mdl-color-text--grey-600") {
                        div("mdl-layout__header-row") {

                            //                            if (showHome) {
                            span("mdl-layout-title") {
                                a("$homePath/") { +"Home" }
                            }
//                            }

                            /*div("mdl-layout-spacer")

                            div("mdl-textfield mdl-js-textfield mdl-textfield--expandable") {

                                label("mdl-button mdl-js-button mdl-button--icon") {
                                    for_ = "search"

                                    i("material-icons") { +"search" }
                                }

                                div("mdl-textfield__expandable-holder") {

                                    input(type = InputType.text, classes = "mdl-textfield__input") {
                                        id = "search"
                                    }

                                    label("mdl-textfield__label") {
                                        for_ = "search"
                                        +"Enter your query..."
                                    }

                                }

                            }

                            button(classes = "mdl-button mdl-js-button mdl-js-ripple-effect mdl-button--icon") {
                                id = "hdrbtn"

                                i(classes = "material-icons") { +"more_vert" }
                            }

                            ul(classes = "mdl-menu mdl-js-menu mdl-js-ripple-effect mdl-menu--bottom-right") {
                                attributes["for"] = "hdrbtn"

                                li("mdl-menu__item") { +"About" }
                                li("mdl-menu__item") { +"Contact" }
                                li("mdl-menu__item") { +"Legal information" }
                            }*/
                        }
                    }

                    div("demo-drawer mdl-layout__drawer mdl-color--blue-grey-900 mdl-color-text--blue-grey-50") {

                        nav("demo-navigation mdl-navigation mdl-color--blue-grey-800") {

                            sidebarLinks.forEach {
                                a(href = it.href, classes = "mdl-navigation__link") { +it.text }
                            }

                            /*div("mdl-layout-spacer")

                            a(classes = "mdl-navigation__link") {
                                materialIcon("help_outline", "", "mdl-color-text--blue-grey-400")
                                span("visuallyhidden") { +"Help" }
                            }*/
                        }
                    }

                    main("mdl-layout__content mdl-color--grey-100") {

                        div("mdl-grid demo-content") {

                            contents.forEach { content ->
                                when (content) {
                                    is Content.LinkList -> renderLinkList(content)
                                    is Content.SortableLinkList -> renderSortableLinkList(content)
                                    is Content.Form -> renderForm(content)
                                    is Content.Review -> renderReview(content)
                                    is Content.Card -> renderTitledBlock(content.title, content.renderContent)
                                }.also { }
                            }
                        }
                    }

                }

                script(src = "http://zeptojs.com/zepto.min.js")
                script(src = "//cdnjs.cloudflare.com/ajax/libs/dialog-polyfill/0.4.3/dialog-polyfill.min.js")
                script(src = "$staticPath/zepto-dnd.min.js")
                script(src = "//code.getmdl.io/1.1.3/material.min.js")

                script {
                    unsafe {
+"""$('.sortable').sortable().on('sortable:change', function() {
    var list = [];
    var ${'$'}this = $(this);
    ${'$'}this.children().each(function() {
        var id = $(this).data('id');
        if (typeof id !== 'undefined') {
            list.push(id);
        }
    });
    $.post(${'$'}this.data('action'), {
        ids: list
    });
});"""
                    }
                }


                val scripts = LinkedHashSet<String>()
                contents.forEach { content ->
                    content.dependencies.requiredJs(staticPath).forEach {
                        if (scripts.add(it))
                            script(src = it)
                    }
                }

            }
        }
    }

    private fun FlowContent.main(classes: String?, visitor: MAIN.() -> Unit) = MAIN(consumer, classes).visit(visitor)

    private class MAIN(consumer: TagConsumer<*>, classes: String?) :
            HTMLTag("main", consumer, mapOf("class" to (classes ?: "")), inlineTag = false, emptyTag = false),
            FlowContent


    private fun FlowContent.renderLinkList(content: Content.LinkList) {
        blockWithList(content.title, content.links, "", {}, { link ->
            a(href = link.href, classes = "mdl-navigation__link" +
                    if (link.badge != null) " mdl-badge" else "") {

                link.badge?.let { attributes.put("data-badge", it) }

                // link to class page
                +link.text
            }
        })
    }

    private fun FlowContent.renderSortableLinkList(content: Content.SortableLinkList) {
        blockWithList(content.title, content.linksAndIds, "sortable", {
            attributes["data-action"] = content.updateAction
        }, { (link, id) ->
            id?.let { attributes["data-id"] = it }

            a(href = link.href, classes = "mdl-navigation__link" +
                    if (link.badge != null) " mdl-badge" else "") {
                link.badge?.let { attributes["data-badge"] = it }

                // link to class page
                +link.text
            }
        })
    }

    private fun FlowContent.renderForm(content: Content.Form) {
        blockWithTitleAndForm(content.title) {

            content.controlsAndValues.forEach { (ctl, value) ->
                renderControl(ctl, value)
            }

            div {
                button(type = ButtonType.submit, classes = raisedColouredButtonClasses) {
                    formAction = content.submitAction
                    +when (content.mode) {
                        is Content.Form.Mode.Edit -> "Review"
                        is Content.Form.Mode.Create -> "Create"
                    }
                }

                if (content.mode is Content.Form.Mode.Edit) {
                    button(type = ButtonType.submit, classes = raisedAccentedButtonClasses) {
                        style = "margin-left: 16px"
                        formAction = content.mode.removeAction
                        onClick = "return confirm('O RLY?');"
                        +"Remove"
                    }
                }
            }
        }
    }

    private fun FlowContent.renderReview(content: Content.Review) {
        blockWithTitleAndForm(content.title) {
            table("mdl-data-table mdl-js-data-table") {
                tbody {
                    content.namesTitlesValues.forEach { (name, title, value) ->
                        tr {
                            td("mdl-data-table__cell--non-numeric") {
                                +title
                            }
                            td("mdl-data-table__cell--non-numeric") {
                                +value

                                input(type = InputType.hidden, name = name) {
                                    this@input.value = value
                                }
                            }
                        }
                    }
                }
            }

            div {
                style = "margin-top: 16px"

                button(type = ButtonType.submit, classes = flatButtonClasses) {
                    formAction = content.editAction
                    +"Edit"
                }

                button(type = ButtonType.submit, classes = raisedColouredButtonClasses) {
                    style = "margin-left: 16px"
                    formAction = content.updateAction
                    +"Update"
                }
            }
        }
    }

    private fun FlowContent.renderTitledBlock(title: String, renderContent: FlowContent.() -> Unit) {
        div("mdl-card mdl-color--white mdl-shadow--2dp mdl-cell mdl-cell--4-col") {
            div("mdl-card__supporting-text") {
                h3 { +title }

                renderContent()
            }
        }
    }




    private fun FlowOrPhrasingContent.materialIcon(icon: String, text: String, classes: String? = null) {
        i(classes?.let { "$it material-icons" } ?: "material-icons") {
            role = "presentation"
            +icon
        }
        +text
    }

    private fun FlowContent.blockWithTitleAndForm(title: String, form: FORM.() -> Unit) {
        div("mdl-color--white mdl-shadow--2dp mdl-cell mdl-cell--12-col") {
            h4("mdl-card__title") { +title }
            form(
                    method = FormMethod.post,
                    classes = "mdl-tooltip--large"
            ) {
                form()
            }
        }
    }

    private inline fun <T> FlowContent.blockWithList(
            title: String, items: List<T>, ulClasses: String,
            crossinline configureList: UL.() -> Unit, crossinline renderItem: LI.(T) -> Unit
    ) {
        renderTitledBlock(title) {
            ul("mdl-list m-v-0 p-v-0${if (ulClasses.isNotBlank()) ' ' + ulClasses else ""}") {
                configureList()
                items.forEach { item ->
                    li("mdl-list__item") {
                        renderItem(this, item)
                    }
                }
            }
        }
    }

    private fun FlowContent.renderControl(control: Control, value: String) {
        when (control.type) {
            Control.Type.Input -> renderInput(control, value)
            Control.Type.TextArea -> renderTextArea(control, value)
            Control.Type.Custom -> control.render(this, value, null)
        }
    }

    private fun FlowContent.renderInput(control: Control, value: String) {
        div("mdl-textfield mdl-js-textfield mdl-textfield--floating-label") {
            control.render(this, value, "mdl-textfield__input")
            label("mdl-textfield__label") {
                for_ = control.id
                +control.title
            }
        }
    }

    private fun FlowContent.renderTextArea(control: Control, value: String) {
        div("mdl-textfield mdl-js-textfield mdl-textfield--floating-label") {
            control.render(this, value, "mdl-textfield__input")
            label("mdl-textfield__label") {
                for_ = control.id
                +control.title
            }
        }
    }

}
