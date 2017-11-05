package online.javanese.krud

/**
 * Defines CSS and JS files this thing depends on.
 */
interface FrontendDependencies {

    /**
     * Set of CSS-files this control depends on.
     */
    fun requiredCss(staticPath: String): Set<String>

    /**
     * Set of scripts this control depends on.
     */
    fun requiredJs(staticPath: String): Set<String>

}

/**
 * Null-object implementation of [FrontendDependencies]
 */
object NoFrontendDependencies : FrontendDependencies {
    override fun requiredCss(staticPath: String): Set<String> = emptySet()
    override fun requiredJs(staticPath: String): Set<String> = emptySet()
}

/**
 * Just a container.
 */
class SimpleFrontendDependencies(
        private val css: Set<String> = emptySet(),
        private val js: Set<String> = emptySet()
) : FrontendDependencies {
    override fun requiredCss(staticPath: String): Set<String> = css
    override fun requiredJs(staticPath: String): Set<String> = js
}

/**
 * Merges gives style and script addresses.
 */
class CompositeFrontendDependencies(
        private val deps: List<FrontendDependencies>
) : FrontendDependencies {

    override fun requiredCss(staticPath: String): Set<String> =
            deps.flatMapTo(LinkedHashSet()) { it.requiredCss(staticPath) }

    override fun requiredJs(staticPath: String): Set<String> =
            deps.flatMapTo(LinkedHashSet()) { it.requiredJs(staticPath) }
}
