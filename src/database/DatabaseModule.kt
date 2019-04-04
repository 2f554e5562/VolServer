import database.GroupAlreadyExists
import database.UserAlreadyExists
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseModule {
    fun connectRelational() {

    }

//    private fun GraphNode.find(vararg byFields: String): List<GraphNode> {
//        return driver.session().readTransaction { transaction ->
//            val query =
//                "MATCH (${title.decapitalize()}: ${title.capitalize()}) WHERE (${nodeParameters.filter { it.key in byFields }.map { "${title.decapitalize()}.${it.key} = \"${it.value}\"" }.joinToString(
//                    " AND "
//                )}) RETURN ${title.decapitalize()}"
//            println(query)
//
//            val result = transaction.run(query)
//
//            return@readTransaction result.list { record ->
//                val node = record[title].asNode()
//
//                println(node.asMap { it.asString() }.toMutableMap())
//
//                GraphNode(
//                    node.labels().first(),
//                    node.asMap { it.asString() }.toMutableMap()
//                )
//            }
//        }
//    }
//
//    private fun GraphNode.findRelationNodes(relation: String): List<Int> {
//        return driver.session().readTransaction { transaction ->
//            val relationQuery =
//                if (relation == "any")
//                    "[$relation]"
//                else
//                    "[$relation: ${relation.capitalize()}]"
//
//            val query =
//                "MATCH ($title: ${title.capitalize()} { ${nodeParameters.map { "${it.key}:${it.value}" }.joinToString(
//                    ", "
//                )} }) - $relationQuery -> (otherNode) RETURN otherNode"
//            println(query)
//
//            val result = transaction.run(query)
//
//            return@readTransaction result.list { record ->
//                return@list record["otherNode"]["id"].asInt()
//            }
//        }
//    }
//
//    private fun GraphNode.mergeGraphNodes(relation: String, mergeWith: GraphNode) {
//        driver.session().readTransaction { transaction ->
//            val query =
//                "MATCH ($title: ${title.capitalize()} { ${nodeParameters.map { "${it.key}:${it.value}" }.joinToString(
//                    ", "
//                )} }), (${mergeWith.title}: ${mergeWith.title.capitalize()} { ${mergeWith.nodeParameters.map { "${it.key}:${it.value}" }.joinToString(
//                    ", "
//                )} }) MERGE (($title) - [$relation: ${relation.capitalize()}] -> (${mergeWith.title}))"
//            println(query)
//
//            return@readTransaction transaction.run(query)
//        }
//    }
//
//    private fun GraphNode.deleteGraphNode() {
//        driver.session().readTransaction { transaction ->
//            val query =
//                "MATCH ($title: ${title.capitalize()} { ${nodeParameters.map { "${it.key}:${it.value}" }.joinToString(
//                    ", "
//                )} }) DETACH DELETE $title"
//            println(query)
//
//            return@readTransaction transaction.run(query)
//        }
//    }
//
//    private fun GraphNode.deleteRelation(relation: String, otherNode: GraphNode) {
//        driver.session().readTransaction { transaction ->
//            val query =
//                "MATCH ($title: ${title.capitalize()} { ${nodeParameters.map { "${it.key}:${it.value}" }.joinToString(
//                    ", "
//                )} }) - [$relation: ${relation.capitalize()}] -> (${otherNode.title}: ${otherNode.title.capitalize()} { ${otherNode.nodeParameters.map { "${it.key}:${it.value}" }.joinToString(
//                    ", "
//                )} }) DELETE $relation"
//            println(query)
//            return@readTransaction transaction.run(query)
//        }
//    }


    fun createUser(user: UsersCreateI): UserNode {
        val users = VolGraphDatabase.find<UserNode> {
            login = user.login
            password = user.password
        }

        if (users.isEmpty())
            return VolGraphDatabase.new<UserNode> {
                login = user.login.trimAllSpaces()
                password = user.password.trimAllSpaces()
                firstName = user.data.firstName.trimAllSpaces()
                lastName = user.data.lastName.trimAllSpaces()
                middleName = user.data.middleName.trimAllSpaces()
                birthday = user.data.birthday
                about = user.data.about?.trimAllSpaces()
                phoneNumber = user.data.phoneNumber?.trimAllSpaces()
                image = user.data.image?.trimAllSpaces() ?: "default"
                email = user.data.email?.trimAllSpaces()
                link = user.data.link?.trimAllSpaces()
            }
        else {
            throw UserAlreadyExists()
        }
    }

    fun findByLoginAndPassword(login: String, password: String) =
        VolGraphDatabase.find<UserNode> {
            this.login = login
            this.password = password
        }.firstOrNull()

    fun findUserById(id: Int) =
        VolGraphDatabase.find<UserNode> {
            this.id = id
        }.firstOrNull()

    fun editUser(user: UserDataEdit, userId: Int): UserData {
        val editedUser = VolGraphDatabase.edit<UserNode>(userId) {
            user.firstName?.let { firstName = it }
            user.lastName?.let { lastName = it }
            user.middleName?.let { middleName = it }
            user.birthday?.let { birthday = it }

            about = user.about
            phoneNumber = user.phoneNumber
            image = user.image
            email = user.email
            link = user.link
        }.first()

        return editedUser.let {
            UserData(
                it.id,
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
            UserData(
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

    fun createGroup(group: GroupData, userId: Int) =
        if (VolGraphDatabase.find<GroupNode> { title = group.title }.isNotEmpty()) {
            val newGroupNode = VolGraphDatabase.new<GroupNode> {
                title = group.title.trimAllSpaces()
                description = group.description?.trimAllSpaces()
                color = group.color.trimAllSpaces()
                image = group.image.trimAllSpaces()
                link = group.link?.trimAllSpaces()
            }

            val creatorNode = VolGraphDatabase.find<UserNode> {
                id = userId
            }.first()

            VolGraphDatabase.newRelation<CreatorRelation>(creatorNode, newGroupNode)

            newGroupNode
        } else throw GroupAlreadyExists()

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


    fun createEvent(event: EventData, userId: Int) =
        if (VolGraphDatabase.find<EventNode> { title = event.title }.isNotEmpty()) {
            val newEventNode = VolGraphDatabase.new<EventNode> {
                title = event.title.trimAllSpaces()
                place = event.place?.trimAllSpaces()
                datetime = event.datetime.toString()
                duration = event.duration.toString()
                description = event.description?.trimAllSpaces()
                link = event.link?.trimAllSpaces()
            }

            val creatorNode = VolGraphDatabase.find<UserNode> {
                id = userId
            }.first()

            VolGraphDatabase.newRelation<CreatorRelation>(creatorNode, newEventNode)

            newEventNode
        } else throw GroupAlreadyExists()

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
}
