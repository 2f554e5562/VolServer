package dataClasses

import com.fasterxml.jackson.annotation.JsonProperty

data class CreateUserI(
    @JsonProperty("login") val login: String,
    @JsonProperty("password") val password: String,
    @JsonProperty("firstName") val firstName: String,
    @JsonProperty("lastName") val lastName: String,
    @JsonProperty("middleName") val middleName: String,
    @JsonProperty("about") val about: String?,
    @JsonProperty("phoneNumber") val phoneNumber: String?,
    @JsonProperty("image") val image: String?,
    @JsonProperty("email") val email: String?,
    @JsonProperty("vkLink") val vkLink: String?
)

data class CreateUserO(
    @JsonProperty("token") val token: String
)

data class CreateTokenByRefreshTokenI(
    @JsonProperty("refreshToken") val refreshToken: String
)

data class CreateTokenByRefreshTokenO(
    @JsonProperty("token") val string: String
)

data class CreateTokenByLoginI(
    @JsonProperty("login") val login: String,
    @JsonProperty("password") val password: String
)

data class CreateTokenByLoginO(
    @JsonProperty("token") val token: String
)