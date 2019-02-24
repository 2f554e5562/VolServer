import dataClasses.GroupData
import dataClasses.UserData
import dataClasses.UsersCreateI
import database.GroupAlreadyExists
import database.UserAlreadyExists
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseModule {
    fun connect() = Database.connect(
        url = "jdbc:postgresql://localhost:19396/vol_data",
        driver = "org.postgresql.Driver",
        user = "vol_server",
        password = "123581321345589144233377"
    )

    fun createUser(user: UsersCreateI): UserRow = transaction {
        if (UserRow.find { UsersTable.login eq user.login.trim() }.count() == 0) {
            return@transaction UserRow.new {
                login = user.login.trim()
                password = user.password.trim()
                firstName = user.data.firstName.trim()
                lastName = user.data.lastName.trim()
                middleName = user.data.middleName.trim()
                birthday = user.data.birthday

                if (user.data.about != null)
                    about = user.data.about.trim()

                if (user.data.phoneNumber != null)
                    phoneNumber = user.data.phoneNumber.trim()

                if (user.data.image != null)
                    image = user.data.image.trim()

                if (user.data.email != null)
                    email = user.data.email.trim()

                if (user.data.vkLink != null)
                    vkLink = user.data.vkLink.trim()
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
        if (GroupRow.find { GroupsTable.title eq group.title.trim() }.count() == 0) {
            return@transaction GroupRow.new {
                title = group.title.trim()
                creatorId = userId
                description = group.description.trim()
                canPost = group.canPost

                group.color?.let {
                    color = it.trim()
                }

                group.image?.let {
                    image = it.trim()
                }

                group.vkLink?.let {
                    vkLink = it.trim()
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