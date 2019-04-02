import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Routing
import io.ktor.routing.post

fun Routing.usersProfileGet(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/users/profile/get") {
        try {
            checkPermission(tokenManager, volDatabase) { token, user ->
                respondOk(
                    UsersProfileGetO(
                        user
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
