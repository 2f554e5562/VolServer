abstract class GraphRelationship(
    val startNode: Class<out GraphNode>? = null,
    val endNode: Class<out GraphNode>? = null
) {
    var id by NotNullable<Long>()

    var startNodeId by NotNullable<Long>()
    var endNodeId by NotNullable<Long>()
}
