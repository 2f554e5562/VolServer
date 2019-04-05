package router

import DatabaseModule
import authTokenCreateByLoginAndPassword
import authTokenCreateByRefreshToken
import authUsersCreate
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import imageUpload
import io.ktor.application.Application
import io.ktor.routing.routing
import usersFind
import usersProfileGet
import TokenManager
import eventsCreate
import eventsEdit
import eventsFind
import eventsJoin
import eventsLeave
import eventsLike
import groupsCreate
import groupsEdit
import groupsFind
import groupsJoin
import groupsLeave
import imageLoad
import usersProfileEdit

val volDatabase = DatabaseModule()
val tokenManager = TokenManager()
val json = jacksonObjectMapper()

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    routing {
        authTokenCreateByLoginAndPassword()

        authTokenCreateByRefreshToken()

        authUsersCreate()

        usersProfileGet()

        usersFind()

        usersProfileEdit()

        groupsCreate()

        groupsEdit()

        groupsFind()

        groupsJoin()

        groupsLeave()

        eventsCreate()

        eventsEdit()

        eventsFind()

        eventsJoin()

        eventsLeave()

        eventsLike()

        imageUpload()

        imageLoad()
    }
}
