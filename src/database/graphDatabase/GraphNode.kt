abstract class GraphNode {
    var id by NotNullable<Long>()
}

class Filter(
    private val clazz: Class<*>
) {
    val filters = mutableListOf<String>()

    fun add(filter: (String) -> String) {
        filters.add(filter(clazz.name.decapitalize()))
    }
}

infix fun String.startsLike(string: String): String {
    return "$this =~ '$string.+'"
}

infix fun String.endsLike(string: String): String {
    return "$this =~ '.+$string'"
}

infix fun String.like(string: String): String {
    return "$this =~ '.+$string.+'"
}

infix fun String.inList(list: List<Any>): String {
    return "$this IN $list"
}

infix fun String.equals(boolean: Boolean): String {
    return "$this = $boolean"
}
