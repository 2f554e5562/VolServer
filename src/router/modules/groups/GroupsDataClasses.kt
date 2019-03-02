import com.fasterxml.jackson.annotation.JsonProperty

data class GroupCreateI(
    @JsonProperty("data") val data: GroupData
)

data class GroupCreateO(
    @JsonProperty("data") val data: GroupData
)

data class GroupsFindI(
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
