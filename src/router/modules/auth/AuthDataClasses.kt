import com.fasterxml.jackson.annotation.JsonProperty

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

data class UsersCreateI(
    @JsonProperty("login") val login: String,
    @JsonProperty("password") val password: String,
    @JsonProperty("data") val data: UserData
)

data class UsersCreateO(
    @JsonProperty("token") val token: String
)