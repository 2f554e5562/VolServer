import org.jetbrains.exposed.dao.*

object UsersTable: IntIdTable("users") {
    val login = varchar("login", 64).uniqueIndex()
    val password = varchar("password", 64)
    val firstName = text("first_name")
    val lastName = text("last_name")
    val middleName = text("middle_name")
    val about = text("about").nullable()
    val phoneNumber = text("phone_number").nullable()
    val image = text("image").default("default.png").nullable()
    val email = text("email").nullable()
    val vkLink = text("vk_link").nullable()
}
class UserRow(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<UserRow>(UsersTable)

    var login           by UsersTable.login
    var password        by UsersTable.password
    var firstName       by UsersTable.firstName
    var lastName        by UsersTable.lastName
    var middleName      by UsersTable.middleName
    var about           by UsersTable.about
    var phoneNumber     by UsersTable.phoneNumber
    var image           by UsersTable.image
    var email           by UsersTable.email
    var vkLink          by UsersTable.vkLink
}

object GroupsTable: IntIdTable("groups") {
    val title = text("title")
    val canPost = text("can_post")
    val image = integer("image")
    val description = long("description")
    val vkLink = text("vk_link").nullable()
    val color = text("color").default("64B5F6").nullable()
}
class GroupRow(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<GroupRow>(GroupsTable)

    var title           by GroupsTable.title
    var canPost         by GroupsTable.canPost
    var image           by GroupsTable.image
    var description     by GroupsTable.description
    var vkLink          by GroupsTable.vkLink
    var color           by GroupsTable.color
}

object EventsTable: IntIdTable("events") {
    val title = text("title")
    val authorId = integer("author_id")
    val description = text("description")
    val placeId = integer("place_id")
    val datetime = long("datetime")
    val images = text("images")
    val links = text("links")
}
class EventRow(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<EventRow>(EventsTable)

    var title           by EventsTable.title
    var authorId        by EventsTable.authorId
    var description     by EventsTable.description
    var placeId         by EventsTable.placeId
    var datetime        by EventsTable.datetime
    var images          by EventsTable.images
    var links           by EventsTable.links
}