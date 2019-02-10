import dataClasses.CreateUserI
import database.UserAlreadyExists
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.reflect.jvm.jvmName

class DatabaseModule {
    fun connect() {
        Database.connect(
            url = "jdbc:postgresql://localhost:19396/vol_data",
            driver = "org.postgresql.Driver",
            user = "vol_server",
            password = "123581321345589144233377"
        )
    }

    fun initUserTable() {
        transaction {
            exec(UsersTable.createStatement().joinToString())
            println("${UsersTable::class.jvmName}: OK")
        }
    }

    fun createUser(user: CreateUserI): UserRow = transaction {
        if (UserRow.find { UsersTable.login eq user.login.trim() }.count() == 0) {
            return@transaction UserRow.new {
                login = user.login.trim()
                password = user.password.trim()
                firstName = user.firstName.trim()
                lastName = user.lastName.trim()
                middleName = user.middleName.trim()

                if (user.about != null)
                    about = user.about.trim()

                if (user.phoneNumber != null)
                    phoneNumber = user.phoneNumber.trim()

                if (user.image != null)
                    image = user.image.trim()

                if (user.email != null)
                    email = user.email.trim()

                if (user.vkLink != null)
                    vkLink = user.vkLink.trim()
            }
        } else {
            throw UserAlreadyExists()
        }
    }

    fun findByLoginAndPassword(login: String, password: String): UserRow? = transaction {
        UserRow.find { (UsersTable.login eq login) and (UsersTable.password eq password) }.firstOrNull()
    }
}