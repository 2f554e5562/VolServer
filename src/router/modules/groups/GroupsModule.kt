@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

import com.fasterxml.jackson.module.kotlin.readValue
import database.GroupAlreadyExists
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Routing
import io.ktor.routing.post
import router.json
import router.tokenManager
import router.volDatabase


fun Routing.groupsCreate() =
    post("/groups/create") {
        try {
            val groupCreateI = json.readValue<GroupCreateI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                val group = volDatabase.createGroup(
                    groupCreateI.data, user.id
                )

                respondCreated(
                    GroupCreateO(
                        group
                    ).writeValueAsString()
                )
            }
        } catch (e: GroupAlreadyExists) {
            respondConflict()
        } catch (e: Exception) {
            e.printStackTrace()
            respondBadRequest()
        }
    }


fun Routing.groupsFind() =
    post("/groups/find/list") {
        try {
            val groupFindI = json.readValue<GroupsFindI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                respondOk(
                    GroupsFindO(
                        volDatabase.findGroupsByParameters(user.id, groupFindI.parameters, groupFindI.offset, groupFindI.amount)
                    ).writeValueAsString()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            respondBadRequest()
        }
    }


fun Routing.groupsFindByUser() =
    post("/groups/find/list/byUser") {
        try {
            val groupsFindByUserI = json.readValue<GroupsFindByUserI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                respondOk(
                    GroupsFindByUserO(
                        volDatabase.findGroupsByUser(groupsFindByUserI.userId, groupsFindByUserI.parameters, groupsFindByUserI.offset, groupsFindByUserI.amount)
                    ).writeValueAsString()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            respondBadRequest()
        }
    }


fun Routing.groupsEdit() =
    post("/groups/edit") {
        try {
            val groupsEditI = json.readValue<GroupsEditI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                val groupData = volDatabase.editGroup(groupsEditI.newData, groupsEditI.id, user.id)

                if (groupData != null) {
                    respondOk(
                        GroupsEditO(
                            groupData
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


fun Routing.groupsJoin() =
    post("/groups/leave") {
        try {
            val groupsEditI = json.readValue<GroupsJoinI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                val successful = volDatabase.leaveGroup(groupsEditI.groupId, user.id)

                if (successful) {
                    respondOk(
                        GroupsJoinO(
                            successful
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


fun Routing.groupsApplyCode() =
    post("/groups/code/apply") {
        try {
            checkPermission(tokenManager, volDatabase) { token, user ->
                val applyCodeI = json.readValue<ApplyCodeI>(call.receive<ByteArray>())

                val code = tokenManager.parseCode(applyCodeI.code)

                if (code != null) {
                    val creatorUser = volDatabase.findUserById(code.userId)

                    if (creatorUser != null) {
                        val rightCode = tokenManager.createCode(code.administratorPermission, code.nodeId, tokenManager.createToken(AuthUserData(creatorUser.id, creatorUser.login, creatorUser.password)), user.id)

                        if (rightCode.toString() == applyCodeI.code) {
                            respondOk(
                                ApplyCodeO(
                                    true
                                ).writeValueAsString()
                            )
                            volDatabase.joinGroup(code.administratorPermission, code.nodeId, user.id)
                        } else {
                            respondForbidden()
                        }
                    } else {
                        respondForbidden()
                    }
                } else {
                    respondForbidden()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            respondBadRequest()
        }
    }


fun Routing.groupsCreateCode() =
    post("/groups/code/create") {
        try {
            val createCodeI = json.readValue<CreateCodeI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                val permission = volDatabase.isAdministrator(user.id, createCodeI.targetId)

                if (permission) {
                    respondOk(
                        CreateCodeO(
                            tokenManager.createCode(createCodeI.administrator, createCodeI.targetId, token, createCodeI.targetUserId).toString()
                        ).writeValueAsString()
                    )
                } else {
                    respondForbidden()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            respondBadRequest()
        }
    }

