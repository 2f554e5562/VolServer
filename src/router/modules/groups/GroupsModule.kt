import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import database.GroupAlreadyExists
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Routing
import io.ktor.routing.post
import java.lang.IllegalStateException


fun Routing.groupsCreate(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/groups/create") {
        try {
            val groupCreateI = json.readValue<GroupCreateI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                val group = volDatabase.createGroup(
                    groupCreateI.data, user.id
                )

                respondCreated(
                    GroupCreateO(
                        GroupFullData(
                            group.id,
                            group.title,
                            group.description,
                            group.color,
                            group.image,
                            group.link,
                            group.creatorId,
                            group.joined
                        )
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


fun Routing.groupsFind(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/groups/list/find") {
        try {
            val groupFindI = json.readValue<GroupsFindI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                respondOk(
                    GroupsFindO(
                        volDatabase.findGroupsByParameters(groupFindI.parameters, groupFindI.offset, groupFindI.amount)
                    ).writeValueAsString()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            respondBadRequest()
        }
    }


fun Routing.groupsEdit(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
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


fun Routing.groupsJoin(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/groups/join") {
        try {
            val groupsEditI = json.readValue<GroupsJoinI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                val successful = true

                volDatabase.joinGroup(groupsEditI.groupId, user.id)

                if (successful) {
                    respondOk(
                        GroupsJoinO(
                            "Successful"
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


fun Routing.groupsLeave(
    json: ObjectMapper,
    tokenManager: TokenManager,
    volDatabase: DatabaseModule
) =
    post("/groups/leave") {
        try {
            val groupsEditI = json.readValue<GroupsLeaveI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                val successful = true

                volDatabase.leaveGroup(groupsEditI.groupId, user.id)

                if (successful) {
                    respondOk(
                        GroupsLeaveO(
                            "Successful"
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
