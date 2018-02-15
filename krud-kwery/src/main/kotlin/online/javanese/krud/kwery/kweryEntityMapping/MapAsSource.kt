package online.javanese.krud.kwery.kweryEntityMapping

class MapAsSource(
        private val valuesMap: Map<String, String>,
        private val nameTransform: (String) -> String = { it }
): ValueFactory.Source<String> {
    override fun contains(key: String): Boolean = valuesMap.contains(nameTransform(key))
    override fun get(key: String): String = valuesMap[nameTransform(key)]!!
}
