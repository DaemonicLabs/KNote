package knote.util
import java.util.LinkedHashMap

class MutableKObservableMap<K, V>: KObservableMap<K, V>, LinkedHashMap<K, V>() {
    override val callbacks: MutableList<(oldValue: Map<K, V>, newValue: Map<K, V>) -> Unit> = mutableListOf()

    private fun execCallback(oldValue: Map<K, V>, newValue: Map<K, V>) {
        callbacks.forEach {
            it.invoke(oldValue, newValue)
        }
    }

    override fun clear() {
        val oldValue =  this.clone() as Map<K, V>
        super.clear().also {
            execCallback(oldValue, this)
        }

    }

    override fun put(key: K, value: V): V? {
        val oldValue =  this.clone() as Map<K, V>
        return super.put(key, value).also {
            execCallback(oldValue, this)
        }
    }

    override fun putAll(from: Map<out K, V>) {
        val oldValue =  this.clone() as Map<K, V>
        return super.putAll(from).also {
            execCallback(oldValue, this)
        }
    }

    override fun remove(key: K): V? {
        val oldValue =  this.clone() as Map<K, V>
        return super<LinkedHashMap>.remove(key).also {
            execCallback(oldValue, this)
        }
    }
}