import dataClasses.GroupData
import dataClasses.UserData
import dataClasses.UsersCreateI
import database.GroupAlreadyExists
import database.UserAlreadyExists
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import utils.trimAllSpaces

class DatabaseModule {
    fun connect() = Database.connect(
        url = "jdbc:postgresql://localhost:19396/vol_data",
        driver = "org.postgresql.Driver",
        user = "vol_server",
        password = "123581321345589144233377"
    )

    fun createUser(user: UsersCreateI): UserRow = transaction {
        if (UserRow.find { UsersTable.login eq user.login.trimAllSpaces() }.count() == 0) {
            return@transaction UserRow.new {
                login = user.login.trimAllSpaces()
                password = user.password.trimAllSpaces()
                firstName = user.data.firstName.trimAllSpaces()
                lastName = user.data.lastName.trimAllSpaces()
                middleName = user.data.middleName.trimAllSpaces()
                birthday = user.data.birthday

                if (user.data.about != null)
                    about = user.data.about.trimAllSpaces()

                if (user.data.phoneNumber != null)
                    phoneNumber = user.data.phoneNumber.trimAllSpaces()

                if (user.data.image != null)
                    image = user.data.image.trimAllSpaces()

                if (user.data.email != null)
                    email = user.data.email.trimAllSpaces()

                if (user.data.vkLink != null)
                    vkLink = user.data.vkLink.trimAllSpaces()
            }
        } else {
            throw UserAlreadyExists()
        }
    }

    fun findByLoginAndPassword(login: String, password: String): UserRow? = transaction {
        UserRow.find { (UsersTable.login eq login) and (UsersTable.password eq password) }.firstOrNull()
    }

    fun findUserById(id: Int): UserRow? = transaction {
        UserRow.findById(id)
    }

    fun findUsersByParameters(query: Query, offset: Int, amount: Int) = transaction {
        query.limit(amount, offset).map {
            UserData(
                it[UsersTable.id].value,
                it[UsersTable.firstName],
                it[UsersTable.lastName],
                it[UsersTable.middleName],
                it[UsersTable.birthday],
                it[UsersTable.about],
                it[UsersTable.phoneNumber],
                it[UsersTable.image],
                it[UsersTable.email],
                it[UsersTable.vkLink]
            )
        }
    }

    fun createGroup(group: GroupData, userId: Int): GroupRow = transaction {
        if (GroupRow.find { GroupsTable.title eq group.title.trimAllSpaces() }.count() == 0) {
            return@transaction GroupRow.new {
                title = group.title.trimAllSpaces()
                creatorId = userId
                description = group.description.trimAllSpaces()
                canPost = group.canPost

                group.color?.let {
                    color = it.trimAllSpaces()
                }

                group.image?.let {
                    image = it.trimAllSpaces()
                }

                group.vkLink?.let {
                    vkLink = it.trimAllSpaces()
                }
            }
        } else {
            throw GroupAlreadyExists()
        }
    }

    fun findGroupById(id: Int): GroupRow? = transaction {
        GroupRow.findById(id)
    }

    fun findGroupsByParameters(query: Query, offset: Int, amount: Int) = transaction {
        query.limit(amount, offset).map {
            GroupData(
                it[GroupsTable.title],
                it[GroupsTable.description],
                it[GroupsTable.canPost],
                it[GroupsTable.color],
                it[GroupsTable.image],
                it[GroupsTable.vkLink],
                it[GroupsTable.creatorId]
            )
        }
    }
}