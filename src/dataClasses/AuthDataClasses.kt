package dataClasses

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

data class CreateUserI(
    @JsonProperty("login") val login: String,
    @JsonProperty("password") val password: String,
    @JsonProperty("data") val data: UserProfile
)

data class CreateUserO(
    @JsonProperty("token") val token: String
)

data class UsersProfileGetI(
    @JsonProperty("token") val token: String
)

data class UsersProfileGetO(
    @JsonProperty("data") val data : UserProfile
)

data class UsersFindI(
    @JsonProperty("token") val token: String,
    @JsonProperty("amount") val amount: Int,
    @JsonProperty("offset") val offset: Int,
    @JsonProperty("parameters") val parameters: UserProfileSearch
)

data class UsersFindO(
    @JsonProperty("users") val users : List<UserProfile>
)

data class UserProfile(
    @JsonProperty("id") val id: Int?,
    @JsonProperty("firstName") val firstName: String,
    @JsonProperty("lastName") val lastName: String,
    @JsonProperty("middleName") val middleName: String,
    @JsonProperty("birthday") val birthday: Long,
    @JsonProperty("about") val about: String?,
    @JsonProperty("phoneNumber") val phoneNumber: String?,
    @JsonProperty("image") val image: String?,
    @JsonProperty("email") val email: String?,
    @JsonProperty("vkLink") val vkLink: String?
)

data class UserProfileSearch(
    @JsonProperty("ids") val ids: ArrayList<Int>? = null,
    @JsonProperty("firstName") val firstName: String? = null,
    @JsonProperty("lastName") val lastName: String? = null,
    @JsonProperty("middleName") val middleName: String? = null,
    @JsonProperty("birthdayMin") val birthdayMin: Long? = null,
    @JsonProperty("birthdayMax") val birthdayMax: Long? = null,
    @JsonProperty("about") val about: String? = null,
    @JsonProperty("phoneNumber") val phoneNumber: String? = null,
    @JsonProperty("email") val email: String? = null,
    @JsonProperty("vkLink") val vkLink: String? = null
)