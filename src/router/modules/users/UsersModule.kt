@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Routing
import io.ktor.routing.post
import router.json
import router.tokenManager
import router.volDatabase

fun Routing.usersProfileGet() =
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
            e.printStackTrace()
            respondBadRequest()
        }
    }


fun Routing.usersFind() =
    post("/users/list/find") {
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
            e.printStackTrace()
            respondBadRequest()
        }
    }


fun Routing.usersFindByGroup() =
    post("/users/find/list/byGroup") {
        try {
            val usersFindByGroupI = json.readValue<UsersFindByGroupI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                respondOk(
                    UsersFindByGroupO(
                        volDatabase.findUsersByGroup(usersFindByGroupI.groupId, usersFindByGroupI.parameters, usersFindByGroupI.offset, usersFindByGroupI.amount)
                    ).writeValueAsString()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            respondBadRequest()
        }
    }


fun Routing.usersFindByEvent() =
    post("/users/find/list/byEvent") {
        try {
            val usersFindByEventI = json.readValue<UsersFindByEventI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                respondOk(
                    UsersFindByEventO(
                        volDatabase.findUsersByEvent(usersFindByEventI.eventId, usersFindByEventI.parameters, usersFindByEventI.offset, usersFindByEventI.amount)
                    ).writeValueAsString()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            respondBadRequest()
        }
    }


fun Routing.usersProfileEdit() =
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
            e.printStackTrace()
            respondBadRequest()
        }
    }
