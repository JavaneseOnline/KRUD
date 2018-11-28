package online.javanese.krud

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.util.StringValues
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel

/**
 * Describes HTTP request.
 * If [method] is not [HttpMethod.Post], [post] must be [StringValues.Empty].
 */
class HttpRequest(
        val method: HttpMethod,
        val pathSegments: List<String>,
        val query: StringValues,
        val post: StringValues
)

/**
 * Describes WebSocket request.
 */
class WsRequest(
        val call: ApplicationCall,
        val pathSegments: List<String>,
        val query: StringValues,
        val incoming: ReceiveChannel<Frame>,
        val outgoing: SendChannel<Frame>
)
