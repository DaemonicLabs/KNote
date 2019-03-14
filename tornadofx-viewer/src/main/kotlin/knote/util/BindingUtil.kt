package knote.util

import javafx.beans.WeakListener
import javafx.collections.MapChangeListener
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import mu.KLogging

import java.lang.ref.WeakReference

object BindingUtil : KLogging() {
    fun <K, V, F> mapContent(
        mapped: ObservableList<F>, source: ObservableMap<out K, out V>,
        mapper: (K, V) -> F
    ) {
        map(mapped, source, mapper)
    }

    private fun <K, V, F> map(
        mapped: ObservableList<F>, source: ObservableMap<out K, out V>,
        mapper: (K, V) -> F
    ): Any {
        val contentMapping = MapContentMapping(mapped, source, mapper)
//        mapped.setAll(source.map { (k, v) -> mapper.invoke(k, v) })
        source.removeListener(contentMapping)
        source.addListener(contentMapping)
        return contentMapping
    }

    private class MapContentMapping<K, V, F>(
        mapped: MutableList<F>,
        source: Map<out K, V>,
        private val mapper: (K, V) -> F
    ) :
        MapChangeListener<K, V>, WeakListener {
        private val mappedRef: WeakReference<MutableList<F>> = WeakReference(mapped)
        private val indexMap: MutableMap<K, Int> = mutableMapOf()

        init {
            source.forEach { (k, v) ->
                val value = mapper.invoke(k, v)
                indexMap[k] = mapped.size
                mapped.add(value)
            }
        }

        override fun onChanged(change: MapChangeListener.Change<out K, out V>) {
            val mapped = mappedRef.get()
            if (mapped == null) {
                change.map.removeListener(this)
            } else {
                if(change.wasAdded()) {
                    val value = mapper.invoke(change.key, change.valueAdded)
                    indexMap[change.key] = mapped.size
                    mapped.add(value)
                }
                if (change.wasRemoved()) {
                    indexMap[change.key]?.let { key ->
                        mapped.removeAt(key)
                    } ?: run {
                        logger.error("did not remove '${change.key}'")
                    }
                    indexMap.remove(change.key)
                }
            }
        }

        override fun wasGarbageCollected(): Boolean {
            return mappedRef.get() == null
        }

        override fun hashCode(): Int {
            val list = mappedRef.get()
            return list?.hashCode() ?: 0
        }

        override fun equals(obj: Any?): Boolean {
            if (this === obj) {
                return true
            }

            val mapped1 = mappedRef.get() ?: return false

            if (obj is MapContentMapping<*, *, *>) {
                val mapped2 = obj.mappedRef.get()
                return mapped1 === mapped2
            }
            return false
        }
    }
}