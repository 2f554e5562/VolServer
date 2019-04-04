import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest

fun String.getHashSHA256(): String {
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(this.toByteArray())
    return digest.fold("") { str, it ->
        str + "%02x".format(it)
    }
}

fun Any.writeValueAsString(): String =
    jacksonObjectMapper().writeValueAsString(this)

fun String.trimAllSpaces(): String =
    Regex("\\s+").replace(this, " ").trim()

suspend fun InputStream.copyToSuspend(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    yieldSize: Int = 4 * 1024 * 1024,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Long {
    return withContext(dispatcher) {
        val buffer = ByteArray(bufferSize)
        var bytesCopied = 0L
        var bytesAfterYield = 0L
        while (true) {
            val bytes = read(buffer).takeIf { it >= 0 } ?: break
            out.write(buffer, 0, bytes)
            if (bytesAfterYield >= yieldSize) {
                yield()
                bytesAfterYield %= yieldSize
            }
            bytesCopied += bytes
            bytesAfterYield += bytes
        }
        return@withContext bytesCopied
    }
}

suspend inline fun PipelineContext<Unit, ApplicationCall>.checkPermission(
    tokenManager: TokenManager,
    volDatabase: DatabaseModule,
    block: (token: Token, user: UserData) -> Unit
) {
    val token = tokenManager.parseToken(call.request.headers["Access-Token"] ?: throw IllegalStateException())

    val user = volDatabase.findUserById(token.userId)

    if (user == null) {
        respondUnauthorized()
    } else {
        val userFullData = UserData(
            user.id,
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

        val userToken = tokenManager.createToken(
            AuthUserData(
                user.id,
                user.login,
                user.password
            )
        )

        if (token.toString() == userToken.toString()) {
            block(token, userFullData)
        } else {
            respondUnauthorized()
        }
    }
}