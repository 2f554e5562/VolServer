package utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun Any.writeValueAsString(): String =
    jacksonObjectMapper().writeValueAsString(this)