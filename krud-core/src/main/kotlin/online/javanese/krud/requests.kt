package online.javanese.krud

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpMethod
import io.ktor.util.ValuesMap
import io.ktor.websocket.Frame
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.SendChannel

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

/**
 * Describes WebSocket request.
 */
class WsRequest(
        val call: ApplicationCall,
        val pathSegments: List<String>,
        val query: ValuesMap,
        val incoming: ReceiveChannel<Frame>,
        val outgoing: SendChannel<Frame>
)