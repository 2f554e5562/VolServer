import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Routing
import io.ktor.routing.post
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll

fun Routing.usersProfileGet(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/users/profile/get") {
        try {
            val token = tokenManager.parseToken(call.request.headers["Access-Token"] ?: throw IllegalStateException())

            checkPermission(tokenManager, volDatabase) { token, user ->
                respondOk(
                    UsersProfileGetO(
                        UserFullData(
                            user.id.value,
                            user.firstName,
                            user.lastName,
                            user.middleName,
                            user.birthday,
                            user.about,
                            user.phoneNumber,
                            user.image,
                            user.email,
                            user.link
                        )
                    ).writeValueAsString()
                )
            }
        } catch (e: Exception) {
            respondBadRequest()
        }
    }

fun Routing.usersFind(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/users/list/get") {
        try {
            val usersFindI = json.readValue<UsersFindI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                respondOk(
                    UsersFindO(
                        volDatabase.findUsersByParameters(usersFindI.parameters, usersFindI.offset, usersFindI.amount)
                    ).writeValueAsString()
                )
            }
        } catch (e: Exception) {
            respondBadRequest()
        }
    }

fun Routing.usersProfileEdit(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/users/profile/edit") {
        try {
            val usersProfileEditI = json.readValue<UsersProfileEditI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                val userData = volDatabase.editUser(usersProfileEditI.newData, token.userId)

                if (userData != null) {
                    respondOk(
                        UsersProfileEditO(
                            userData
                        ).writeValueAsString()
                    )
                } else {
                    respondNotFound()
                }
            }
        } catch (e: Exception) {
            respondBadRequest()
        }
    }
