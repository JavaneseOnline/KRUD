package online.javanese.krud.template

import kotlinx.html.*

class MaterialTemplate(
        private val homePath: String,
        private val staticPath: String
) : AdminTemplate {

    override fun invoke(
            root: HTML,
            titleText: String,
            sidebarLinks: List<Link>,
            content: Content
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
                // <link rel="stylesheet" th:href="@{/sandbox/codemirror_ambiance.min.css}" />
                styleLink("https://cdnjs.cloudflare.com/ajax/libs/dialog-polyfill/0.4.3/dialog-polyfill.min.css")
                styleLink("$staticPath/admin-material.min.css")
                style(content =
""".demo-layout .demo-header .mdl-textfield {
    padding: 0;
    margin-top: 41px;
}
.demo-layout .demo-header .mdl-textfield .mdl-textfield__expandable-holder {
    bottom: 19px;
}
.demo-layout .mdl-layout__header .mdl-layout__drawer-button {
    color: rgba(0, 0, 0, 0.54);
}
.demo-drawer {
    border: none;
}
/* iOS Safari specific workaround */
.demo-drawer .mdl-menu__container {
    z-index: -1;
}
.demo-drawer .demo-navigation {
    z-index: -2;
}
/* END iOS Safari specific workaround */
.demo-drawer .mdl-menu .mdl-menu__item {
    display: flex;
    align-items: center;
}

.demo-navigation {
    flex-grow: 1;
}
.demo-layout .demo-navigation .mdl-navigation__link {
    display: flex !important;
    flex-direction: row;
    align-items: center;
    color: rgba(255, 255, 255, 0.56);
    font-weight: 500;
}
.demo-layout .demo-navigation .mdl-navigation__link:hover {
    background-color: #00BCD4;
    color: #37474F;
}
.demo-navigation .mdl-navigation__link .material-icons {
    font-size: 24px;
    color: rgba(255, 255, 255, 0.56);
    margin-right: 32px;
}

.demo-content {
    max-width: 1080px;
}

.demo-options h3 {
    margin: 0;
}
.demo-options .mdl-checkbox__box-outline {
    border-color: rgba(255, 255, 255, 0.89);
}
.demo-options ul {
    margin: 0;
    list-style-type: none;
}
.demo-options li {
    margin: 4px 0;
}
.demo-options .material-icons {
    color: rgba(255, 255, 255, 0.89);
}
.demo-options .mdl-card__actions {
    height: 64px;
    display: flex;
    box-sizing: border-box;
    align-items: center;
}

.todo.todo-done {
    text-decoration: line-through;
}

.p-v-0 {
    padding-top: 0;
    padding-bottom: 0;
}
.m-v-0 {
    margin-top: 0;
    margin-bottom: 0;
}
ul.sortable > li.placeholder {
    height: 3em;
    margin: 0;
    padding: 0;
    background-color: rgba(51, 102, 170, .2);
}""")
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

                            div("mdl-layout-spacer")

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
                            }
                        }
                    }

                    div("demo-drawer mdl-layout__drawer mdl-color--blue-grey-900 mdl-color-text--blue-grey-50") {

                        nav("demo-navigation mdl-navigation mdl-color--blue-grey-800") {

                            sidebarLinks.forEach {
                                a(href = it.href, classes = "mdl-navigation__link") { +it.text }
                            }

                            div("mdl-layout-spacer")

                            a(classes = "mdl-navigation__link") {
                                materialIcon("help_outline", "", "mdl-color-text--blue-grey-400")
                                span("visuallyhidden") { +"Help" }
                            }
                        }
                    }

                    main("mdl-layout__content mdl-color--grey-100") {

                        div("mdl-grid demo-content") {

                            when (content) {
                                is Content.LinkList -> {
                                    div("mdl-color--white mdl-shadow--2dp mdl-cell mdl-cell--4-col") {
                                        h4("mdl-card__title") { +content.title }

                                        ul("mdl-list m-v-0 p-v-0") {
                                            content.links.forEach { link ->
                                                li("mdl-list__item") {
                                                    // for each entity class
                                                    a(href = link.href, classes = "mdl-navigation__link") {
                                                        // link to class page
                                                        +link.text
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                is Content.Form -> {
                                    blockWithTitleAndForm(content.mode.name) {

                                        content.controlsAndValues.forEach { (ctl, value) ->
                                            renderControl(ctl, value)
                                        }

                                        div {
                                            button(type = ButtonType.submit, classes = "mdl-button mdl-js-button") {
                                                formAction = content.reviewAction
                                                +"Review"
                                            }
                                        }
                                    }

                                    /*form(method = FormMethod.delete, action = "$homePath/module/table/id") { // TODO
                                        style = "position:relative;bottom:2.5em"
                                        button(type = ButtonType.submit, classes = "mdl-button mdl-js-button") {
                                            onClick = "return confirm('O RLY?');"
                                            style ="position:absolute;right:0em"
                                            +"Remove"
                                        }
                                    }*/
                                }

                                is Content.Review -> {
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

                                            button(type = ButtonType.submit, classes = "mdl-button mdl-js-button") {
                                                formAction = content.editAction
                                                +"Edit"
                                            }

                                            button(type = ButtonType.submit, classes = "mdl-button mdl-js-button") {
                                                formAction = content.updateAction
                                                +"Update (TODO)" // TODO
                                            }
                                        }
                                    }
                                }
                            }.also { }
                        }
                    }

                }

//                script(src = "$staticPath/codemirror_html.min.js") // todo: move to extension
                script(src = "http://zeptojs.com/zepto.min.js")
                script(src = "//cdnjs.cloudflare.com/ajax/libs/dialog-polyfill/0.4.3/dialog-polyfill.min.js")
//                script(src = "$staticPath/zepto-dnd.min.js")
                script(src = "//code.getmdl.io/1.1.3/material.min.js")
/*
                <script type="text/javascript" th:inline="javascript">
                function insertAtCursor(field, value) {
                //IE support
                if (document.selection) {
                    field.focus();
                    sel = document.selection.createRange();
                    sel.text = value;
                }
                //MOZILLA and others
                else if (field.selectionStart || field.selectionStart == '0') {
                    var startPos = field.selectionStart;
                    var endPos = field.selectionEnd;
                    field.value = field.value.substring(0, startPos)
                    + value
                    + field.value.substring(endPos, field.value.length);
                } else {
                    field.value += value;
                }
            }

                $('div.wysiwyg').each(function() {
                var $this = $(this);
                /*var $textarea = $this.find('textarea').hide();
                var $preview = $('<div/>').insertAfter($this).html($textarea.val());
                var $editor = $('<div class="editor" />')
                        .text($textarea.val())
                        .insertAfter($textarea);
                ace.config.set('workerPath', '/js/');
                var editor = ace.edit($editor[0]);
                editor.setTheme('ace/theme/kuroir');
                editor.getSession().setMode('ace/mode/html');
                editor.getSession().on('change', function(e) {
                    var val = editor.getValue();
                    $textarea.val(val);
                    $preview.html(val);
                });*/
                var textarea = $this.find('textarea').hide()[0];
                var editor = CodeMirror(function(elt) {
                    textarea.parentNode.appendChild(elt);
                }, {value: textarea.value,
                    lineNumbers: true,
                    indentUnit: 4});
                editor.on('change', function(inst) {
                    textarea.value = inst.getValue();
                });
            });
                $('.sortable').sortable().on('sortable:change', function() {
                var list = [];
                $(this).children().each(function() {
                var id = $(this).data('id');
                if (typeof id !== 'undefined') {
                list.push(id);
            }
            });
                $.post('/admin/sort/', {
                clazz: /*[[${currentEntityClass}]]*/ "none",
                order: list,
                /*[[${_csrf.parameterName}]]*/ "param"
                :
                /*[[${_csrf.token}]]*/ "token"
            });
            });
                </script>

                <th:block th:utext="${presenter.getAdditionalMarkup(_csrf)}" />
                */
            }
        }
    }

    private fun FlowContent.main(classes: String?, visitor: MAIN.() -> Unit) = MAIN(consumer, classes).visit(visitor)

    class MAIN(consumer: TagConsumer<*>, classes: String?) :
            HTMLTag("main", consumer, mapOf("class" to (classes ?: "")), inlineTag = false, emptyTag = false),
            FlowContent

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

    private fun renderTextArea(control: Control, value: String) {
        TODO()
    }

}
