package online.javanese.krud.kwery


class UuidGeneratingMap(
        private val idColName: String
) : Map<String, Uuid> {

    override val size: Int get() = 1

    override fun isEmpty(): Boolean = false

    override fun containsKey(key: String): Boolean =
            key == idColName

    override fun containsValue(value: Uuid): Boolean =
            false

    override fun get(key: String): Uuid =
            if (key == idColName) Uuid.randomUUID()
            else throw NoSuchElementException()

    override val keys: Set<String>
        get() = setOf(idColName)

    override val values: Collection<Uuid>
        get() = setOf(Uuid.randomUUID())

    override val entries: Set<Map.Entry<String, Uuid>>
        get() = setOf(object : Map.Entry<String, Uuid> {
            override val key: String get() = idColName
            override val value: Uuid  = Uuid.randomUUID()
        })

}
