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
import groupsCreate
import groupsFind
import imageLoad
import usersProfileEdit

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val volDatabase = DatabaseModule()
    val tokenManager = TokenManager()
    val json = jacksonObjectMapper()

    routing {
        authTokenCreateByLoginAndPassword(json, tokenManager, volDatabase)

        authTokenCreateByRefreshToken(tokenManager, volDatabase)

        authUsersCreate(json, tokenManager, volDatabase)

        usersProfileGet(json, tokenManager, volDatabase)

        usersFind(json, tokenManager, volDatabase)

        usersProfileEdit(json, tokenManager, volDatabase)

        groupsCreate(json, tokenManager, volDatabase)

//        groupsEdit(json, tokenManager, volDatabase)
//
        groupsFind(json, tokenManager, volDatabase)
//
//        eventsCreate(json, tokenManager, volDatabase)
//
//        eventsEdit(json, tokenManager, volDatabase)
//
//        eventsFind(json, tokenManager, volDatabase)
//
//        eventsFindByUser(json, tokenManager, volDatabase)

        imageUpload()

        imageLoad(json)
    }
}
