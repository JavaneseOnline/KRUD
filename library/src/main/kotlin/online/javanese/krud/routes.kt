package online.javanese.krud

internal val PathSegmentRegex = Regex("^[a-zA-Z_-]+$")

internal inline fun <T> Array<T>.checkRoutes(Item: String, items: String, getRoute: (T) -> String) {
    val routeSet = mapTo(HashSet()) {
        val route = getRoute(it)
        if (!PathSegmentRegex.matches(route))
            throw IllegalArgumentException("$Item route must be [a-zA-Z_-]+.")

        route
    }

    if (routeSet.size != size) {
        throw IllegalArgumentException("Some of $items are mapped to the same path.")
    }
}
