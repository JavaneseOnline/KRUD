package online.javanese.krud

import io.ktor.http.HttpMethod
import io.ktor.util.ValuesMap

/**
 * Describes HTTP request.
 * If [method] is not [HttpMethod.Post], [post] must be [ValuesMap.Empty].
 */
class HttpRequest(
        val method: HttpMethod,
        val pathSegments: List<String>,
        val query: ValuesMap,
        val post: ValuesMap
)
