package knote.util

import kotlin.reflect.KProperty

class MutableKObservableObject<This, T>(
    value: T
) : KObservableObject<This, T> {
    override var value: T = value
        set(value) {
            field = value
            callbacks.forEach { it.invoke(value) }
        }

    operator fun setValue(self: This, property: KProperty<*>, t: T) {
        value = t
    }

    override val callbacks: MutableList<(newValue: T) -> Unit> = mutableListOf()
}