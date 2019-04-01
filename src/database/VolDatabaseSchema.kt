import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object UsersTable : IntIdTable("users") {
    val login = varchar("login", 64).uniqueIndex()
    val password = varchar("password", 64)
    val firstName = text("first_name")
    val lastName = text("last_name")
    val middleName = text("middle_name")
    val birthday = long("birthday")
    val about = text("about").nullable()
    val phoneNumber = text("phone_number").nullable()
    val image = text("image").default("default.png").nullable()
    val email = text("email").nullable()
    val link = text("link").nullable()
}

class UserRow(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserRow>(UsersTable)

    var login by UsersTable.login
    var password by UsersTable.password
    var firstName by UsersTable.firstName
    var lastName by UsersTable.lastName
    var birthday by UsersTable.birthday
    var middleName by UsersTable.middleName
    var about by UsersTable.about
    var phoneNumber by UsersTable.phoneNumber
    var image by UsersTable.image
    var email by UsersTable.email
    var link by UsersTable.link
}

object GroupsTable : IntIdTable("groups") {
    val title = text("title")
    val creatorId = integer("creator_id")
    val description = text("description").nullable()
    val canPost = bool("can_post").default(false)
    val color = text("color").default("64B5F6").nullable()
    val image = text("image").nullable()
    val link = text("link").nullable()
}

class GroupRow(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<GroupRow>(GroupsTable)

    var creatorId by GroupsTable.creatorId
    var title by GroupsTable.title
    var description by GroupsTable.description
    var canPost by GroupsTable.canPost
    var color by GroupsTable.color
    var image by GroupsTable.image
    var link by GroupsTable.link
}

object EventsTable : IntIdTable("events") {
    val title = text("title")
    val authorId = integer("author_id")
    val place = text("place")
    val datetime = long("datetime")
    val duration = long("duration")
    val description = text("description")
    val link = text("link")
}

class EventRow(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<EventRow>(EventsTable)

    var title by EventsTable.title
    var authorId by EventsTable.authorId
    var place by EventsTable.place
    var datetime by EventsTable.datetime
    var duration by EventsTable.duration
    var description by EventsTable.description
    var link by EventsTable.link
}
