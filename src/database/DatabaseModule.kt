import dataClasses.CreateUserI
import dataClasses.UserProfile
import database.UserAlreadyExists
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.reflect.jvm.jvmName

class DatabaseModule {
    fun connect() = Database.connect(
        url = "jdbc:postgresql://localhost:19396/vol_data",
        driver = "org.postgresql.Driver",
        user = "vol_server",
        password = "123581321345589144233377"
    )

    fun initUserTable() = transaction {
        exec(UsersTable.createStatement().joinToString())
        println("${UsersTable::class.jvmName}: OK")
    }

    fun createUser(user: CreateUserI): UserRow = transaction {
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
            UserProfile(
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
}