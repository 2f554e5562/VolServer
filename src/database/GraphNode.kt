class GraphNode(
    var title: String,
    var nodeParameters: Map<String, String>
) {
    inline fun <reified T> parseTo() =
        T::class.constructors.first().call(*T::class.java.declaredFields.map { nodeParameters[it.name] }.toTypedArray())
}

fun Any.toNode(): GraphNode {
    val params = mutableMapOf<String, String>()

    this::class.java.declaredFields.forEach { field ->
        field.trySetAccessible()
        params[field.name] = field.get(this).toString()
    }

    return GraphNode(
        this::class.java.name,
        params
    )
}
