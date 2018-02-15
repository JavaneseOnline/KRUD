package online.javanese.krud.kwery

import online.javanese.krud.kwery.kweryEntityMapping.ValueFactory

class UuidGeneratingSource(
        private val idColName: String
) : ValueFactory.Source<Uuid> {

    override fun contains(key: String): Boolean =
            key == idColName

    override fun get(key: String): Uuid =
            if (key == idColName) Uuid.randomUUID()
            else throw NoSuchElementException()

}
