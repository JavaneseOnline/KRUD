package online.javanese.krud.kwery

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.Column
import com.github.andrewoma.kwery.mapper.Table
import online.javanese.krud.crud.Sort

class KweryExplicitSort<T : Any, ID>(
        private val session: Session,
        /**
         * @note my fork of kwery has method on Dialect for this, but I don't want to rely on it
         */
        private val escapeName: (String) -> String,
        private val table: Table<T, ID>,
        private val sortIdxCol: Column<T, Int>
) : Sort.Explicit<ID>() {
    override fun updateOrder(newOrder: List<ID>) {
        val idCol = table.idColumns.single()
        val query = """UPDATE ${escapeName(table.name)} SET ${escapeName(sortIdxCol.name)} = :idx WHERE ${escapeName(idCol.name)} = :id"""
        session.transaction {
            newOrder.forEachIndexed { idx, id ->
                session.update(query, mapOf("idx" to idx, "id" to id))
            }
        }
    }
}
