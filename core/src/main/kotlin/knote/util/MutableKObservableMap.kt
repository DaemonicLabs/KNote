package knote.util

import java.util.LinkedHashMap

class MutableKObservableMap<K, V> : KObservableMap<K, V>, LinkedHashMap<K, V>() {
    override val callbacks: MutableList<(newValue: Map<K, V>) -> Unit> = mutableListOf()

    private fun execCallback(newValue: Map<K, V>) {
        callbacks.forEach {
            it.invoke(newValue)
        }
    }

    override fun clear() {
        super.clear().also {
            execCallback(this)
        }
    }

    override fun put(key: K, value: V): V? {
        return super.put(key, value).also {
            execCallback(this)
        }
    }

    override fun putAll(from: Map<out K, V>) {
        return super.putAll(from).also {
            execCallback(this)
        }
    }

    override fun remove(key: K): V? {
        return super<LinkedHashMap>.remove(key).also {
            execCallback(this)
        }
    }
}