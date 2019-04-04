infix fun String.startsLike(string: String): String {
    return "$this =~ '$string.+'"
}

infix fun String.endsLike(string: String): String {
    return "$this =~ '.+$string'"
}

infix fun String.like(string: String): String {
    return "$this =~ '.*$string.*'"
}

infix fun String.inList(list: List<Any>): String {
    return "$this IN $list"
}

infix fun String.equals(boolean: Boolean): String {
    return "$this = $boolean"
}

infix fun String.greaterOrEquals(other: Long): String {
    return "$this >= $other"
}

infix fun String.greater(other: Long): String {
    return "$this > $other"
}

infix fun String.lessOrEquals(other: Long): String {
    return "$this <= $other"
}

infix fun String.less(other: Long): String {
    return "$this < $other"
}
