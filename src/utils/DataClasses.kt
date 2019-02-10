package utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

data class ErrorMessage(
    val message: String
)

fun Any.writeValueAsString() =
    jacksonObjectMapper().writeValueAsString(this)