package knote.api

import kotlin.reflect.KProperty

interface PageResult <This, T> {
    operator fun getValue(self: This, property: KProperty<*>): T
}