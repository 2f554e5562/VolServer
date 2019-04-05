import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.GraphDatabase

object VolGraphDatabase : GraphTable(
    "vol_graph",
    GraphDatabase.driver("bolt://194.1.239.114:7687", AuthTokens.basic("vol_server", "2f4e623830"))
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
    @ObserveRelationship<GroupJoinedRelationship>
    var joined by NotNullable<Long>()
}

class EventNode : GraphNode() {
    var creatorId by NotNullable<Long>()
    var title by NotNullable<String>()
    var place by Nullable<String>()
    var datetime by Nullable<Long>()
    var duration by Nullable<Long>()
    var description by Nullable<String>()
    var link by Nullable<String>()
    @ObserveRelationship<EventJoinedRelationship>
    var joined by NotNullable<Long>()
}

class GroupCreatorRelationship : GraphRelationship()

class EventCreatorRelationship : GraphRelationship()

class GroupJoinedRelationship : GraphRelationship()

class EventJoinedRelationship : GraphRelationship()