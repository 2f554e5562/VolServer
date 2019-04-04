import kotlin.reflect.KProperty

class NotNullable<T : Any> {
    private var value: T? = null

    @Throws(IllegalStateException::class)
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException("Property ${property.name} not initialized.")
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

class Nullable<T : Any> {
    private var value: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        this.value = value
    }
}