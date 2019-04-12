import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.request.receiveStream
import io.ktor.response.respondBytes
import io.ktor.response.respondFile
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import router.tokenManager
import router.volDatabase
import java.io.File

fun Routing.imageUpload() =
    post("/images") {
        try {
            checkPermission(tokenManager, volDatabase) { _, _ ->
                val uploadedFile = call.receiveStream()

                val file = File(
                    "files/uploaded/images",
                    "${uploadedFile.hashCode().toString().getHashSHA256()}_${System.currentTimeMillis()}"
                )

                file.outputStream().buffered().use { output ->
                    uploadedFile.copyToSuspend(output)
                }

                respondOk(
                    ImageUploadO(
                        file.name.createImageLink()
                    ).writeValueAsString()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            respondBadRequest()
        }
    }

fun Routing.imageLoad() =
    get("/images/{imageName}") {
        try {
            checkPermission(tokenManager, volDatabase) { _, _ ->
                val fileName = call.parameters["imageName"]

                val file = File(
                    "files/uploaded/images",
                    fileName
                )

                if (file.exists()) {
                    call.respondBytes(
                        file.readBytes(),
                        ContentType.Image.Any
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

fun Routing.privacyPolicy() =
    get("/privacyPolicy") {
        try {
            call.respondFile(File("files/privacyPolicy.html"))
        } catch (e: Exception) {
            respondNotFound()
        }

    }
