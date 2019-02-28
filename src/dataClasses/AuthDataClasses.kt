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

data class UsersCreateI(
    @JsonProperty("login") val login: String,
    @JsonProperty("password") val password: String,
    @JsonProperty("data") val data: UserData
)

data class UsersCreateO(
    @JsonProperty("token") val token: String
)

data class UsersProfileGetI(
    @JsonProperty("token") val token: String
)

data class UsersProfileGetO(
    @JsonProperty("data") val data: UserData
)

data class UsersFindI(
    @JsonProperty("token") val token: String,
    @JsonProperty("amount") val amount: Int,
    @JsonProperty("offset") val offset: Int,
    @JsonProperty("parameters") val parameters: UserDataSearch
)

data class UsersFindO(
    @JsonProperty("users") val users: List<UserData>
)

data class GroupCreateI(
    @JsonProperty("token") val token: String,
    @JsonProperty("data") val data: GroupData
)

data class GroupCreateO(
    @JsonProperty("data") val data: GroupData
)

data class GroupsFindI(
    @JsonProperty("token") val token: String,
    @JsonProperty("offset") val offset: Int,
    @JsonProperty("amount") val amount: Int,
    @JsonProperty("parameters") val parameters: GroupDataSearch
)

data class GroupsFindO(
    @JsonProperty("groups") val groups: List<GroupData>
)


data class GroupData(
    @JsonProperty("title") val title: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("can_post") val canPost: Boolean,
    @JsonProperty("color") val color: String?,
    @JsonProperty("image") val image: String?,
    @JsonProperty("vk_link") val vkLink: String?,
    @JsonProperty("creator_id") val creatorId: Int?
)

data class GroupDataSearch(
    @JsonProperty("ids") val ids: List<Int>? = null,
    @JsonProperty("title") val title: String? = null,
    @JsonProperty("description") val description: String? = null,
    @JsonProperty("can_post") val canPost: Boolean? = null,
    @JsonProperty("vk_link") val vkLink: String? = null,
    @JsonProperty("creator_ids") val creatorIds: List<Int>? = null
)

data class UserData(
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

class UserDataSearch(
    @JsonProperty("ids") val ids: List<Int>? = null,
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