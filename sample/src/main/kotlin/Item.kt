import java.util.*

class Item(
        val id: UUID,
        val name: String,
        val text: String,
        val code: String,
        val cool: Boolean,
        val colour: Colour,
        val bestWith: Set<Colour>
)
