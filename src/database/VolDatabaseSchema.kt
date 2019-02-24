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
    val vkLink = text("vk_link").nullable()
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
    var vkLink by UsersTable.vkLink
}

object GroupsTable : IntIdTable("groups") {
    val creatorId = integer("creator_id")
    val title = text("title")
    val description = text("description")
    val canPost = bool("can_post")
    val color = text("color").default("64B5F6").nullable()
    val image = text("image").nullable()
    val vkLink = text("vk_link").nullable()
}

class GroupRow(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<GroupRow>(GroupsTable)

    var creatorId by GroupsTable.creatorId
    var title by GroupsTable.title
    var description by GroupsTable.description
    var canPost by GroupsTable.canPost
    var color by GroupsTable.color
    var image by GroupsTable.image
    var vkLink by GroupsTable.vkLink
}

object EventsTable : IntIdTable("events") {
    val title = text("title")
    val authorId = integer("author_id")
    val description = text("description")
    val placeId = integer("place_id")
    val datetime = long("datetime")
    val images = text("images")
    val links = text("links")
}

class EventRow(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<EventRow>(EventsTable)

    var title by EventsTable.title
    var authorId by EventsTable.authorId
    var description by EventsTable.description
    var placeId by EventsTable.placeId
    var datetime by EventsTable.datetime
    var images by EventsTable.images
    var links by EventsTable.links
}