abstract class GraphNode {
    var id by NotNullable<Long>()
}

class Filter(
    private val clazz: Class<*>
) {
    val filters = mutableListOf<String>()

    fun add(filter: (String) -> String) {
        filters.add(filter(clazz.name.decapitalize()))
    }
}