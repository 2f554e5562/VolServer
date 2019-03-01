package router

import DatabaseModule
import authTokenCreateByLoginAndPassword
import authTokenCreateByRefreshToken
import authUsersCreate
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import groupsCreate
import groupsFind
import imageUpload
import io.ktor.application.Application
import io.ktor.routing.routing
import usersFing
import usersProfileGet
import utils.TokenManager

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val volDatabase = DatabaseModule()
    val tokenManager = TokenManager()
    val json = jacksonObjectMapper()

    volDatabase.connect()

    routing {
        authTokenCreateByLoginAndPassword(json, tokenManager, volDatabase)

        authTokenCreateByRefreshToken(json, tokenManager, volDatabase)

        authUsersCreate(json, tokenManager, volDatabase)

        usersProfileGet(json, tokenManager, volDatabase)

        usersFing(json, tokenManager, volDatabase)

        groupsCreate(json, tokenManager, volDatabase)

        groupsFind(json, tokenManager, volDatabase)

        imageUpload()
    }
}
