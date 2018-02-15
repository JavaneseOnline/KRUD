package online.javanese.krud.kwery

import com.github.andrewoma.kwery.core.Session

class SelectCount(
        private val session: Session,
        private val escapedQuotedTableName: String
): () -> Int {

    private val query = "SELECT COUNT(*) as \"c\" FROM $escapedQuotedTableName"

    override fun invoke(): Int =
            session.select(query) { row -> row.int("c") }.single()

}
