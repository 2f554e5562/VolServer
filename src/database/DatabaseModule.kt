import database.GroupAlreadyExists
import database.UserAlreadyExists
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.GraphDatabase

class DatabaseModule {
    private val serverUrl = "194.1.239.114"

    private fun connectGraph(uri: String, user: String, password: String) =
        GraphDatabase.driver(uri, AuthTokens.basic(user, password))

    private val driver = connectGraph("bolt://$serverUrl:7687", "vol_server", "2f4e623830")

    fun connectRelational() {
        Database.connect(
            url = "jdbc:h2:tcp://$serverUrl:9092/./root/vol_server/h2db",
            driver = "org.h2.Driver",
            user = "vol_server",
            password = "2f4e623830"
        )

        transaction {
            SchemaUtils.create(UsersTable, EventsTable, GroupsTable)
        }
    }

    private fun NodeItem.findRelationNodes(relation: String): List<Int> {
        return driver.session().readTransaction { transaction ->
            val relationQuery =
                if (relation == "any")
                    "[$relation]"
                else
                    "[$relation: ${relation.capitalize()}]"

            val query = "MATCH ($nodeName: ${nodeName.capitalize()} { ${nodeParameters.map { "${it.key}:${it.value}" }.joinToString(", ")} }) - $relationQuery -> (otherNode) RETURN otherNode"
            println(query)

            return@readTransaction transaction.run(query).list { record ->
                return@list record["otherNode"]["id"].asInt()
            }
        }
    }

    private fun NodeItem.createNewGraphNode() {
        driver.session().readTransaction { transaction ->
            val query =
                "CREATE ($nodeName: ${nodeName.capitalize()} { ${nodeParameters.map { "${it.key}:${it.value}" }.joinToString(
                    ", "
                )} })"
            println(query)

            return@readTransaction transaction.run(query)
        }
    }

    private fun NodeItem.mergeGraphNodes(relation: String, mergeWith: NodeItem) {
        driver.session().readTransaction { transaction ->
            val query =
                "MATCH ($nodeName: ${nodeName.capitalize()} { ${nodeParameters.map { "${it.key}:${it.value}" }.joinToString(
                    ", "
                )} }), (${mergeWith.nodeName}: ${mergeWith.nodeName.capitalize()} { ${mergeWith.nodeParameters.map { "${it.key}:${it.value}" }.joinToString(
                    ", "
                )} }) MERGE (($nodeName) - [$relation: ${relation.capitalize()}] -> (${mergeWith.nodeName}))"
            println(query)

            return@readTransaction transaction.run(query)
        }
    }

    private fun NodeItem.deleteGraphNode() {
        driver.session().readTransaction { transaction ->
            val query =
                "MATCH ($nodeName: ${nodeName.capitalize()} { ${nodeParameters.map { "${it.key}:${it.value}" }.joinToString(
                    ", "
                )} }) DETACH DELETE $nodeName"
            println(query)

            return@readTransaction transaction.run(query)
        }
    }

    private fun NodeItem.deleteRelation(relation: String, otherNode: NodeItem) {
        driver.session().readTransaction { transaction ->
            val query =
                "MATCH ($nodeName: ${nodeName.capitalize()} { ${nodeParameters.map { "${it.key}:${it.value}" }.joinToString(
                    ", "
                )} }) - [$relation: ${relation.capitalize()}] -> (${otherNode.nodeName}: ${otherNode.nodeName.capitalize()} { ${otherNode.nodeParameters.map { "${it.key}:${it.value}" }.joinToString(
                    ", "
                )} }) DELETE $relation"
            println(query)
            return@readTransaction transaction.run(query)
        }
    }


    fun createUser(user: UsersCreateI): UserRow = transaction {
        if (UserRow.find { UsersTable.login eq user.login.trimAllSpaces() }.count() == 0) {
            val newUser = UserRow.new {
                login = user.login.trimAllSpaces()
                password = user.password.trimAllSpaces()
                firstName = user.data.firstName.trimAllSpaces()
                lastName = user.data.lastName.trimAllSpaces()
                middleName = user.data.middleName.trimAllSpaces()
                birthday = user.data.birthday

                if (user.data.about != null)
                    about = user.data.about.trimAllSpaces()

                if (user.data.phoneNumber != null)
                    phoneNumber = user.data.phoneNumber.trimAllSpaces()

                if (user.data.image != null)
                    image = user.data.image.trimAllSpaces()

                if (user.data.email != null)
                    email = user.data.email.trimAllSpaces()

                if (user.data.link != null)
                    link = user.data.link.trimAllSpaces()
            }

            val newUserNode = NodeItem(
                "user",
                mapOf("id" to newUser.id.value.toString())
            )

            newUserNode.createNewGraphNode()

            return@transaction newUser
        } else {
            throw UserAlreadyExists()
        }
    }

    fun findByLoginAndPassword(login: String, password: String): UserRow? = transaction {
        UserRow.find { (UsersTable.login eq login) and (UsersTable.password eq password.getHashSHA256()) }.firstOrNull()
    }

    fun findUserById(id: Int): UserRow? = transaction {
        UserRow.findById(id)
    }

    fun editUser(user: UserDataEdit, userId: Int): UserFullData? = transaction {
        val userData = UserRow.findById(userId)

        userData?.apply {
            user.firstName?.let { firstName = it.trimAllSpaces() }
            user.lastName?.let { lastName = it.trimAllSpaces() }
            user.middleName?.let { middleName = it.trimAllSpaces() }
            user.about?.let { about = it.trimAllSpaces() }
            user.phoneNumber?.let { phoneNumber = it.trimAllSpaces() }
            user.image?.let { image = it.trimAllSpaces() }
            user.email?.let { email = it.trimAllSpaces() }
            user.link?.let { link = it.trimAllSpaces() }
        }?.let {
            UserFullData(
                it.id.value,
                it.firstName,
                it.lastName,
                it.middleName,
                it.birthday,
                it.about,
                it.phoneNumber,
                it.image,
                it.email,
                it.link
            )
        }
    }

    fun findUsersByParameters(parameters: UserDataSearch, offset: Int, amount: Int) = transaction {
        val query = UsersTable.selectAll()

        parameters.apply {
            ids?.let { ids ->
                query.andWhere { UsersTable.id inList ids }
            }

            firstName?.let { firstName ->
                query.andWhere { UsersTable.firstName like "%$firstName%" }
            }

            lastName?.let { lastName ->
                query.andWhere { UsersTable.lastName like "%$lastName%" }
            }

            middleName?.let { middleName ->
                query.andWhere { UsersTable.middleName like "%$middleName%" }
            }

            birthdayMin?.let { birthdayMin ->
                query.andWhere { UsersTable.birthday greaterEq birthdayMin }
            }

            birthdayMax?.let { birthdayMax ->
                query.andWhere { UsersTable.birthday lessEq birthdayMax }
            }

            about?.let { about ->
                query.andWhere { UsersTable.about like "%$about%" }
            }

            phoneNumber?.let { phoneNumber ->
                query.andWhere { UsersTable.phoneNumber like "%$phoneNumber%" }
            }

            email?.let { email ->
                query.andWhere { UsersTable.email like "%$email%" }
            }

            link?.let { vkLink ->
                query.andWhere { UsersTable.link like "%$vkLink%" }
            }
        }

        query.limit(amount, offset).map {
            UserFullData(
                it[UsersTable.id].value,
                it[UsersTable.firstName],
                it[UsersTable.lastName],
                it[UsersTable.middleName],
                it[UsersTable.birthday],
                it[UsersTable.about],
                it[UsersTable.phoneNumber],
                it[UsersTable.image],
                it[UsersTable.email],
                it[UsersTable.link]
            )
        }
    }


    fun createGroup(group: GroupData, userId: Int): GroupRow = transaction {
        if (GroupRow.find { GroupsTable.title eq group.title.trimAllSpaces() }.count() == 0) {
            val newGroup = GroupRow.new {
                title = group.title.trimAllSpaces()
                creatorId = userId
                canPost = group.canPost

                group.description?.let { description = it.trimAllSpaces() }

                group.color?.let { color = it.trimAllSpaces() }

                group.image?.let { image = it.trimAllSpaces() }

                group.link?.let { link = it.trimAllSpaces() }
            }

            val newGroupNode = NodeItem(
                "group",
                mapOf("id" to newGroup.id.value.toString())
            )

            val creatorNode = NodeItem(
                "user",
                mapOf("id" to userId.toString())
            )

            newGroupNode.createNewGraphNode()
            creatorNode.mergeGraphNodes("creatorOf", newGroupNode)

            return@transaction newGroup
        } else {
            throw GroupAlreadyExists()
        }
    }

    fun editGroup(group: GroupDataEdit, groupId: Int): GroupFullData? = transaction {
        val groupData = GroupRow.findById(groupId)

        groupData?.apply {
            group.title?.let { title = it.trimAllSpaces() }
            group.description?.let { description = it.trimAllSpaces() }
            group.color?.let { color = it.trimAllSpaces() }
            group.image?.let { image = it.trimAllSpaces() }
            group.link?.let { link = it.trimAllSpaces() }
        }?.let {
            GroupFullData(
                it.id.value,
                it.title,
                it.description,
                it.canPost,
                it.color,
                it.image,
                it.link,
                it.creatorId
            )
        }
    }

    fun findGroupsByParameters(parameters: GroupDataSearch, offset: Int, amount: Int) = transaction {
        val query = GroupsTable.selectAll()

        parameters.apply {
            ids?.let { ids ->
                query.andWhere { GroupsTable.id inList ids }
            }
            title?.let { title ->
                query.andWhere { GroupsTable.title like "%$title%" }
            }
            description?.let { description ->
                query.andWhere { GroupsTable.description like "%$description%" }
            }
            canPost?.let { canPost ->
                query.andWhere { GroupsTable.canPost eq canPost }
            }
            link?.let { link ->
                query.andWhere { GroupsTable.link like "%$link%" }
            }
            creatorIds?.let { creatorIds ->
                query.andWhere { GroupsTable.creatorId inList creatorIds }
            }
        }

        query.limit(amount, offset).map {
            GroupFullData(
                it[GroupsTable.id].value,
                it[GroupsTable.title],
                it[GroupsTable.description],
                it[GroupsTable.canPost],
                it[GroupsTable.color],
                it[GroupsTable.image],
                it[GroupsTable.link],
                it[GroupsTable.creatorId]
            )
        }
    }


    fun createEvent(event: EventData, userId: Int): EventRow = transaction {
        val newEvent = EventRow.new {
            title = event.title.trimAllSpaces()
            authorId = userId

            event.place?.let { place = it.trimAllSpaces() }
            event.datetime?.let { datetime = it }
            event.duration?.let { duration = it }
            event.description?.let { description = it.trimAllSpaces() }
            event.link?.let { link = it.trimAllSpaces() }
        }

        val newEventNode = NodeItem(
            "event",
            mapOf("id" to newEvent.id.value.toString())
        )

        val creatorNode = NodeItem(
            "user",
            mapOf("id" to userId.toString())
        )

        newEventNode.createNewGraphNode()
        creatorNode.mergeGraphNodes("creatorOf", newEventNode)

        return@transaction newEvent
    }

    fun editEvent(event: EventsDataEdit, eventId: Int): EventFullData? = transaction {
        val eventData = EventRow.findById(eventId)

        eventData?.apply {
            event.title?.let { title = it.trimAllSpaces() }
            event.description?.let { description = it.trimAllSpaces() }
            event.datetime?.let { datetime = it }
            event.duration?.let { duration = it }
            event.place?.let { place = it.trimAllSpaces() }
            event.link?.let { link = it.trimAllSpaces() }
        }?.let {
            EventFullData(
                it.id.value,
                it.title,
                it.authorId,
                it.place,
                it.datetime,
                it.duration,
                it.description,
                it.link
            )
        }
    }

    fun findEventsByParameters(parameters: EventDataSearch, offset: Int, amount: Int) = transaction {
        val query = EventsTable.selectAll()

        parameters.apply {
            ids?.let { ids ->
                query.andWhere { EventsTable.id inList ids }
            }

            title?.let { title ->
                query.andWhere { EventsTable.title like "%$title%" }
            }

            authorIds?.let { authorIds ->
                query.andWhere { EventsTable.authorId inList authorIds }
            }

            place?.let { place ->
                query.andWhere { EventsTable.place like "%$place%" }
            }

            datetimeMin?.let { datetimeMin ->
                query.andWhere { EventsTable.datetime greaterEq datetimeMin }
            }

            datetimeMax?.let { datetimeMax ->
                query.andWhere { EventsTable.datetime lessEq datetimeMax }
            }

            durationMin?.let { durationMin ->
                query.andWhere { EventsTable.duration greaterEq durationMin }
            }

            durationMax?.let { durationMax ->
                query.andWhere { EventsTable.duration lessEq durationMax }
            }

            description?.let { description ->
                query.andWhere { GroupsTable.description like "%$description%" }
            }
        }

        query.limit(amount, offset).map {
            EventFullData(
                it[EventsTable.id].value,
                it[EventsTable.title],
                it[EventsTable.authorId],
                it[EventsTable.place],
                it[EventsTable.datetime],
                it[EventsTable.duration],
                it[EventsTable.description],
                it[EventsTable.link]
            )
        }
    }

    fun findEventsByUser(userId: Int, offset: Int, amount: Int, relation: String): List<EventFullData> =
        transaction {
            val userNode = NodeItem(
                "user",
                mapOf("id" to userId.toString())
            )

            val relationNodes = userNode.findRelationNodes(relation)

            return@transaction findEventsByParameters(
                EventDataSearch(relationNodes),
                offset,
                amount
            )
        }
}

data class NodeItem(
    val nodeName: String,
    val nodeParameters: Map<String, String>
)