import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.GraphDatabase

object VolGraphDatabase : GraphTable(
    "vol_graph",
    GraphDatabase.driver(
        confFile.graphServerUrl,
        AuthTokens.basic(confFile.graphServerUser, confFile.graphServerPassword)
    )
)

class UserNode : GraphNode() {
    var login by NotNullable<String>()
    var password by NotNullable<String>()
    var firstName by NotNullable<String>()
    var lastName by NotNullable<String>()
    var middleName by NotNullable<String>()
    var birthday by NotNullable<Long>()
    var about by Nullable<String>()
    var phoneNumber by Nullable<String>()
    var image by Nullable<String>()
    var email by Nullable<String>()
    var link by Nullable<String>()
}

class GroupNode : GraphNode() {
    var creatorId by NotNullable<Long>()
    var title by NotNullable<String>()
    var description by Nullable<String>()
    var color by Nullable<String>()
    var image by Nullable<String>()
    var link by Nullable<String>()
    @ObserveRelationship("groupJoinedRelationship", "groupAdministratorRelationship", "groupCreatorRelationship")
    var joined by NotNullable<Long>()
    @ObserveRelationship("groupAdministratorRelationship", "groupCreatorRelationship")
    var administrated by NotNullable<Long>()
    @ObserveAllRelationship("groupJoinedRelationship")
    var memberCount by NotNullable<Long>()
}

class EventNode : GraphNode() {
    var creatorId by NotNullable<Long>()
    var title by NotNullable<String>()
    var place by Nullable<String>()
    var datetime by Nullable<Long>()
    var duration by Nullable<Long>()
    var description by Nullable<String>()
    var link by Nullable<String>()
    var image by Nullable<String>()
    @ObserveRelationship("eventLikedRelationship")
    var liked by NotNullable<Long>()
    @ObserveAllRelationship("eventLikedRelationship")
    var likeCount by NotNullable<Long>()
    @ObserveRelationship("eventJoinedRelationship")
    var joined by NotNullable<Long>()
    @ObserveAllRelationship("eventJoinedRelationship")
    var joinCount by NotNullable<Long>()
}

class GroupCreatorRelationship : GraphRelationship(UserNode::class.java, GroupNode::class.java)

class GroupJoinedRelationship : GraphRelationship(UserNode::class.java, GroupNode::class.java)

class GroupAdministratorRelationship : GraphRelationship(UserNode::class.java, GroupNode::class.java)

class EventCreatorRelationship : GraphRelationship(UserNode::class.java, EventNode::class.java)

class EventJoinedRelationship : GraphRelationship(UserNode::class.java, EventNode::class.java)

class EventLikedRelationship : GraphRelationship(UserNode::class.java, EventNode::class.java)
