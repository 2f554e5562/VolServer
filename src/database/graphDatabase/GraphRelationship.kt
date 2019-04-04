abstract class GraphRelationship {
    var id by NotNullable<Long>()

    var startNodeId by NotNullable<Long>()
    var endNodeId by NotNullable<Long>()
}
