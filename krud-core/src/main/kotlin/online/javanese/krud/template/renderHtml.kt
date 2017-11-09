package online.javanese.krud.template

import kotlinx.html.TagConsumer
import kotlinx.html.stream.appendHTML

internal fun renderHtml(dsl: TagConsumer<*>.() -> Unit) =
        buildString { appendHTML(false).dsl() }
