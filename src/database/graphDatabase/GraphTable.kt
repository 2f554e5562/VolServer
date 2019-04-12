import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.Record
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaType

abstract class GraphTable(
    val tableName: String,
    val driver: Driver
) {
    init {
        driver.session().run("MATCH (n) WHERE (ID(n) = -1) RETURN n;")
        println("Database ${this::class.java.name} - CONNECTED")
    }

    inline fun <reified T : GraphNode> newNode(block: T.() -> Unit): T {
        val clazz = T::class.constructors.first().call().apply {
            block()
        }

        val clazzName = clazz::class.java.name

        return driver.session().readTransaction { transaction ->
            val returnItems = mutableListOf<String>()

            returnItems.add("(${clazzName.decapitalize()})")

            val query =
                "CREATE (${clazzName.decapitalize()}: ${clazzName.capitalize()} { ${clazz::class.memberProperties.map { field ->
                    field.isAccessible = true

                    val fieldValue = try {
                        field.getter.call(clazz)
                    } catch (e: InvocationTargetException) {
                        return@map null
                    }

                    return@map if (fieldValue != null)
                        when (field.returnType.javaType) {
                            String::class.java -> {
                                "${field.name}: \"$fieldValue\""
                            }

                            else -> "${field.name}: $fieldValue"
                        }
                    else
                        null
                }.filterNotNull().distinct().joinToString(", ")} }) RETURN ${returnItems.distinct().joinToString(", ")}"

            println(query.trimAllSpaces())

            return@readTransaction transaction.run(query).list { record ->
                record.parseToNode<T>()
            }.first()
        }
    }

    inline fun <reified T : GraphNode> findNode(
        byNode: Long? = null,
        limit: Long = 20,
        offset: Long = 0,
        block: T.(Filter) -> Unit
    ): List<T> {
        val filter = Filter(T::class.java)

        val clazz = T::class.constructors.first().call().apply {
            block(filter)
        }
        val byNodeQuery = if (byNode != null)
            "a"
        else
            ""

        val clazzName = clazz::class.java.name

        return driver.session().readTransaction { transaction ->
            val params = mutableListOf<String>()
            val where = mutableListOf<String>()
            val returnItems = mutableListOf<String>()

            if (byNode != null)
                where.add("ID(a) = $byNode")
            returnItems.add("(${clazzName.decapitalize()})")

            clazz::class.memberProperties.forEach { field ->
                field.isAccessible = true

                val fieldValue = try {
                    field.getter.call(clazz)
                } catch (e: InvocationTargetException) {
                    return@forEach
                }

                when (field.name) {
                    "id" -> {
                        where.add("ID(${clazzName.decapitalize()}) = $fieldValue")
                    }
                    else -> {
                        if (fieldValue != null)
                            when (field.returnType.javaType) {
                                String::class.java ->
                                    params.add("${field.name}: \"$fieldValue\"")

                                else ->
                                    params.add("${field.name}: $fieldValue")
                            }
                    }
                }
            }

            where.addAll(filter.filters)

            clazz::class.memberProperties.forEach { property ->
                val observeRelationship = property.findAnnotation<ObserveRelationship>()
                val observeAllRelationship = property.findAnnotation<ObserveAllRelationship>()

                observeRelationship?.relationName?.forEach { relationName ->
                    returnItems.add("(($byNodeQuery) - [:${relationName.capitalize()}] -> (${clazzName.decapitalize()})) as ${relationName.decapitalize()}")
                }

                observeAllRelationship?.relationName?.forEach { relationName ->
                    returnItems.add("(() - [:${relationName.capitalize()}] -> (${clazzName.decapitalize()})) as ${relationName.decapitalize()}All")
                }
            }

            val matchQuery =
                "MATCH (${clazzName.decapitalize()}: ${clazzName.capitalize()} { ${params.distinct().joinToString(
                    ", "
                )} })${if (byNode != null) ", ($byNodeQuery)" else ""}"

            val query = "$matchQuery ${if (!where.isEmpty()) {
                "WHERE (${where.distinct().joinToString(" AND ")})"
            } else {
                ""
            }} RETURN ${returnItems.distinct().joinToString(", ")} ${if (offset > 0) "SKIP $offset" else ""} ${if (limit > 0) "LIMIT $limit" else ""}"

            println(query.trimAllSpaces())

            return@readTransaction transaction.run(query).list { record ->
                record.parseToNode<T>()
            }
        }
    }

    inline fun <reified R : GraphNode, reified T : GraphNode> findNodeRelatedWith(
        relatedNodeId: Long,
        limit: Long = 20,
        offset: Long = 0,
        block: T.(Filter) -> Unit
    ): List<T> {
        val filter = Filter(T::class.java)

        val clazz = T::class.constructors.first().call().apply {
            block(filter)
        }

        val clazzR = R::class.constructors.first().call()

        val clazzName = clazz::class.java.name
        val clazzRName = clazzR::class.java.name

        return driver.session().readTransaction { transaction ->
            val params = mutableListOf<String>()
            val where = mutableListOf<String>()
            val returnItems = mutableListOf<String>()

            returnItems.add("(${clazzName.decapitalize()})")

            clazz::class.memberProperties.forEach { field ->
                field.isAccessible = true

                val fieldValue = try {
                    field.getter.call(clazz)
                } catch (e: InvocationTargetException) {
                    return@forEach
                }

                when (field.name) {
                    "id" -> {
                        where.add("ID(${clazzName.decapitalize()}) = $fieldValue")
                    }
                    else -> {
                        if (fieldValue != null)
                            params.add("${field.name}: \"$fieldValue\"")
                    }
                }
            }

            where.add("ID(${clazzRName.decapitalize()}) = $relatedNodeId")

            where.addAll(filter.filters)

            clazz::class.memberProperties.forEach { property ->
                val observeRelationship = property.findAnnotation<ObserveRelationship>()
                val observeAllRelationship = property.findAnnotation<ObserveAllRelationship>()

                observeRelationship?.relationName?.forEach { relationName ->
                    returnItems.add("((${clazzRName.decapitalize()}) - [:${relationName.capitalize()}] -> (${clazzName.decapitalize()})) as ${relationName.decapitalize()}")
                }
                observeAllRelationship?.relationName?.forEach { relationName ->
                    returnItems.add("(() - [:${relationName.capitalize()}] -> (${clazzName.decapitalize()})) as ${relationName.decapitalize()}All")
                }
            }

            val matchQuery =
                "(${clazzName.decapitalize()}: ${clazzName.capitalize()} { ${params.distinct().joinToString(
                    ", "
                )} }), (${clazzRName.decapitalize()}: ${clazzRName.capitalize()})"

            val query = "MATCH $matchQuery ${if (!where.isEmpty()) {
                "WHERE (${where.distinct().joinToString(" AND ")})"
            } else {
                ""
            }} RETURN ${returnItems.distinct().joinToString(", ")} ${if (offset > 0) "SKIP $offset" else ""} ${if (limit > 0) "LIMIT $limit" else ""}"

            println(query.trimAllSpaces())

            return@readTransaction transaction.run(query).list { record ->
                record.parseToNode<T>()
            }
        }
    }

    inline fun <reified T : GraphNode> editNode(nodeId: Long, block: T.(Filter) -> Unit): T {
        val filter = Filter(T::class.java)

        val clazz = T::class.constructors.first().call().apply {
            block(filter)
        }

        val clazzName = clazz::class.java.name

        return driver.session().readTransaction { transaction ->
            val where = mutableListOf<String>()

            where.add("ID(${clazzName.decapitalize()}) = $nodeId")

            where.addAll(filter.filters)

            val setQuery = clazz::class.memberProperties.map { field ->
                field.isAccessible = true

                try {
                    val fieldValue = field.getter.call(clazz)

                    return@map if (fieldValue != null)
                        when (field.returnType.javaType) {
                            String::class.java -> {
                                "${clazzName.decapitalize()}.${field.name} = \"$fieldValue\""
                            }

                            else -> "${clazzName.decapitalize()}.${field.name} = $fieldValue"
                        }
                    else
                        null
                } catch (e: InvocationTargetException) {
                    return@map null
                }
            }.filterNotNull().distinct().joinToString(", ")

            val query =
                "MATCH (${clazzName.decapitalize()}: ${clazzName.capitalize()}) WHERE (${if (where.isNotEmpty()) where.distinct().joinToString(
                    " AND "
                ) else ""}) ${if (setQuery.isNotBlank()) "SET $setQuery" else ""} RETURN (${clazzName.decapitalize()})"

            println(query.trimAllSpaces())

            return@readTransaction transaction.run(query).list { record ->
                record.parseToNode<T>()
            }.first()
        }
    }

    inline fun <reified T : GraphRelationship> newRelationship(
        firstNodeId: Long,
        secondNodeId: Long
    ): GraphRelationship? {
        return driver.session().readTransaction { transaction ->
            val clazz = T::class.constructors.first().call()

            val clazzName = clazz::class.java.name

            val query =
                "MATCH (a${clazz.startNode?.let { ": ${it.name.capitalize()}" }}), (b${clazz.endNode?.let { ": ${it.name.capitalize()}" }}) WHERE (ID(a) = $firstNodeId AND ID(b) = $secondNodeId) MERGE (a) - [${clazzName.decapitalize()}: ${clazzName.capitalize()}] -> (b) RETURN (${clazzName.decapitalize()});"
            println(query.trimAllSpaces())

            return@readTransaction transaction.run(query).list { record ->
                record.parseToRelationship<T>()
            }.firstOrNull()
        }
    }

    inline fun <reified T : GraphRelationship> deleteRelationship(firstNodeId: Long, secondNodeId: Long): Long {
        return driver.session().readTransaction { transaction ->
            val clazz = T::class.constructors.first().call()

            val clazzName = clazz::class.java.name

            val query =
                "MATCH (a${clazz.startNode?.let { ": ${it.name.capitalize()}" }}) - [${clazzName.decapitalize()}: ${clazzName.capitalize()}] -> (b${clazz.endNode?.let { ": ${it.name.capitalize()}" }}) WHERE (ID(a) = $firstNodeId AND ID(b) = $secondNodeId) DELETE ${clazzName.decapitalize()} RETURN ${clazzName.decapitalize()};"
            println(query.trimAllSpaces())

            return@readTransaction transaction.run(query).list().size.toLong()
        }
    }

    inline fun <reified T : GraphNode, reified R : GraphRelationship> findRelationshipNode(
        nodeId: Long,
        limit: Long = 20,
        offset: Long = 0,
        block: T.(Filter) -> Unit
    ): List<T> {
        val filter = Filter(T::class.java)

        val relationshipName = R::class.java.name

        val clazz = T::class.constructors.first().call().apply {
            block(filter)
        }

        val clazzName = clazz::class.java.name

        return driver.session().readTransaction { transaction ->
            val params = mutableListOf<String>()
            val where = mutableListOf<String>()

            clazz::class.memberProperties.forEach { field ->
                field.isAccessible = true

                val fieldValue = try {
                    field.getter.call(clazz)
                } catch (e: InvocationTargetException) {
                    return@forEach
                }

                when (field.name) {
                    "id" -> {
                        where.add("ID(${clazzName.decapitalize()}) = $fieldValue")
                    }
                    else -> {
                        if (fieldValue != null)
                            params.add("${field.name}: \"$fieldValue\"")
                    }
                }
            }

            where.addAll(filter.filters)

            where.add("ID(a) = $nodeId")

            val matchQuery =
                "(a) - [${relationshipName.decapitalize()}: ${relationshipName.capitalize()}] - (${clazzName.decapitalize()}: ${clazzName.capitalize()})"

            val query = "MATCH $matchQuery WHERE (${if (!where.isEmpty()) {
                where.distinct().joinToString(" AND ")
            } else {
                ""
            }}) RETURN (${clazzName.decapitalize()}) ${if (offset > 0) "SKIP $offset" else ""} ${if (limit > 0) "LIMIT $limit" else ""}"

            println(query.trimAllSpaces())

            return@readTransaction transaction.run(query).list { record ->
                record.parseToNode<T>()
            }
        }
    }

    inline fun <reified T : GraphNode> Record.parseToNode(): T {
        val record = this

        val node = record[T::class.java.name.decapitalize()].asNode()

        return T::class.constructors.first().call().apply {
            this::class.memberProperties.forEach { property ->
                property as KMutableProperty<*>

                val observeRelationship = property.findAnnotation<ObserveRelationship>()
                val observeAllRelationship = property.findAnnotation<ObserveAllRelationship>()

                if (observeRelationship != null) {
                    property.setter.call(this, observeRelationship.relationName.map {
                        return@map if (record.containsKey(it.decapitalize()))
                            record[it].size()
                        else
                            0
                    }.sum())
                }

                if (observeAllRelationship != null) {
                    property.setter.call(this, observeAllRelationship.relationName.map {
                        return@map if (record.containsKey("${it.decapitalize()}All")) {
                            record["${it}All"].size()
                        } else
                            0
                    }.sum())
                }

                when (property.name) {
                    "id" -> {
                        property.setter.call(this, node.id().toInt())
                    }

                    else -> {
                        if (node[property.name].isNull) return@forEach

                        when (property.returnType.javaType) {
                            String::class.java -> property.setter.call(this, node[property.name].asString())
                            String::class -> property.setter.call(this, node[property.name].asString())

                            Int::class.java -> property.setter.call(this, node[property.name].asInt())
                            Int::class -> property.setter.call(this, node[property.name].asInt())

                            Long::class.java -> property.setter.call(this, node[property.name].asLong())
                            java.lang.Long::class.java -> property.setter.call(this, node[property.name].asLong())

                            else -> {
                                throw IllegalStateException("Illegal parse type (${property.returnType.javaType}).")
                            }
                        }
                    }
                }
            }
        }
    }

    inline fun <reified T : GraphRelationship> Record.parseToRelationship(): T {
        val relationship = this[T::class.java.name.decapitalize()].asRelationship()

        return T::class.constructors.first().call().apply {
            id = relationship.id()
            startNodeId = relationship.startNodeId()
            endNodeId = relationship.endNodeId()
        }
    }
}