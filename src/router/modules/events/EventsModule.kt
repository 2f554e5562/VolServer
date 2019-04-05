@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

import com.fasterxml.jackson.module.kotlin.readValue
import database.EventAlreadyExists
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Routing
import io.ktor.routing.post
import router.json
import router.tokenManager
import router.volDatabase


fun Routing.eventsCreate() =
    post("/events/create") {
        try {
            val eventCreateI = json.readValue<EventCreateI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                val event = volDatabase.createEvent(
                    eventCreateI.data, user.id
                )

                respondCreated(
                    EventCreateO(
                        EventFullData(
                            event.id,
                            event.title,
                            user.id,
                            event.place,
                            event.datetime,
                            event.duration,
                            event.description,
                            event.link,
                            event.joined > 0,
                            event.liked > 0
                        )
                    ).writeValueAsString()
                )
            }
        } catch (e: EventAlreadyExists) {
            respondConflict()
        } catch (e: Exception) {
            e.printStackTrace()
            respondBadRequest()
        }
    }


fun Routing.eventsFind() =
    post("/events/list/find") {
        try {
            val eventsFindI = json.readValue<EventsFindI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                respondOk(
                    EventsFindO(
                        volDatabase.findEventsByParameters(
                            eventsFindI.parameters,
                            eventsFindI.offset,
                            eventsFindI.amount
                        )
                    ).writeValueAsString()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            respondBadRequest()
        }
    }

fun Routing.eventsEdit() =
    post("/events/edit") {
        try {
            val eventsEditI = json.readValue<EventsEditI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->

                val eventData = volDatabase.editEvent(eventsEditI.newData, eventsEditI.id)

                if (eventData != null) {
                    respondOk(
                        EventsEditO(
                            eventData
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


fun Routing.eventsJoin() =
    post("/events/join") {
        try {
            val eventsJoinI = json.readValue<EventsJoinI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                val successful = volDatabase.joinEvent(eventsJoinI.eventId, user.id)

                if (successful) {
                    respondOk(
                        EventsJoinO(
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


fun Routing.eventsLeave() =
    post("/events/leave") {
        try {
            val eventsLeaveI = json.readValue<EventsLeaveI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                val successful = volDatabase.leaveEvent(eventsLeaveI.eventId, user.id)

                if (successful) {
                    respondOk(
                        EventsLeaveO(
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

fun Routing.eventsLike() =
    post("/events/like") {
        try {
            val eventsLikeI = json.readValue<EventsLikeI>(call.receive<ByteArray>())

            checkPermission(tokenManager, volDatabase) { token, user ->
                val successful = volDatabase.likeEvent(eventsLikeI.eventId, user.id, eventsLikeI.state)

                if (successful) {
                    respondOk(
                        EventsLeaveO(
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
