package router

import DatabaseModule
import TokenManager
import authTokenCreateByLoginAndPassword
import authTokenCreateByRefreshToken
import authUsersCreate
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import eventsCreate
import eventsEdit
import eventsFind
import eventsJoin
import eventsLike
import eventsLiked
import groupsApplyCode
import groupsCreate
import groupsCreateCode
import groupsEdit
import groupsFind
import groupsJoin
import imageLoad
import imageUpload
import io.ktor.application.Application
import io.ktor.routing.routing
import usersFind
import usersProfileEdit
import usersProfileGet

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

        groupsCreateCode()

        groupsApplyCode()


        eventsCreate()

        eventsEdit()

        eventsFind()

        eventsJoin()

        eventsLike()

        eventsLiked()


        imageUpload()

        imageLoad()
    }
}
