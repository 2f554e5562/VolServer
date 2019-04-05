import com.fasterxml.jackson.annotation.JsonProperty

data class GroupCreateI(
    @JsonProperty("data") val data: GroupCreateData
)

data class GroupCreateO(
    @JsonProperty("data") val data: GroupFullData
)

data class GroupsFindI(
    @JsonProperty("offset") val offset: Long,
    @JsonProperty("amount") val amount: Long,
    @JsonProperty("parameters") val parameters: GroupDataSearch
)

data class GroupsFindO(
    @JsonProperty("groups") val groups: List<GroupFullData>
)

data class GroupsEditI(
    @JsonProperty("id") val id: Long,
    @JsonProperty("new_data") val newData: GroupDataEdit
)

data class GroupsEditO(
    @JsonProperty("new_data") val newData: GroupFullData
)

data class GroupsJoinI(
    @JsonProperty("group_id") val groupId: Long
)

data class GroupsJoinO(
    @JsonProperty("successful") val successful: Boolean
)

data class GroupsLeaveI(
    @JsonProperty("group_id") val groupId: Long
)

data class GroupsLeaveO(
    @JsonProperty("successful") val successful: Boolean
)


data class GroupCreateData(
    @JsonProperty("title") val title: String,
    @JsonProperty("image") val image: String?,
    @JsonProperty("description") val description: String?,
    @JsonProperty("link") val link: String?,
    @JsonProperty("color") val color: String?
)

data class GroupFullData(
    @JsonProperty("id") val id: Long,
    @JsonProperty("title") val title: String,
    @JsonProperty("description") val description: String?,
    @JsonProperty("color") val color: String?,
    @JsonProperty("image") val image: String?,
    @JsonProperty("link") val link: String?,
    @JsonProperty("creator_id") val creatorId: Long,
    @JsonProperty("joined") val joined: Boolean
)

data class GroupDataSearch(
    @JsonProperty("ids") val ids: List<Long>? = null,
    @JsonProperty("title") val title: String? = null,
    @JsonProperty("description") val description: String? = null,
    @JsonProperty("can_post") val canPost: Boolean? = null,
    @JsonProperty("link") val link: String? = null,
    @JsonProperty("creator_ids") val creatorIds: List<Long>? = null
)

data class GroupDataEdit(
    @JsonProperty("title") val title: String? = null,
    @JsonProperty("description") val description: String? = null,
    @JsonProperty("color") val color: String? = null,
    @JsonProperty("image") val image: String? = null,
    @JsonProperty("link") val link: String? = null
)
