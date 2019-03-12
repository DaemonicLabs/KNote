package knote.util

import javafx.beans.WeakListener
import javafx.collections.ListChangeListener
import javafx.collections.MapChangeListener
import javafx.collections.ObservableList
import javafx.collections.ObservableMap

import java.lang.ref.WeakReference

object BindingUtil {

    fun <E, F> mapContent(
        mapped: ObservableList<F>, source: ObservableList<out E>,
        mapper: (E) -> F
    ) {
        map(mapped, source, mapper)
    }

    fun <K, V, F> mapContent(
        mapped: ObservableList<F>, source: ObservableMap<out K, out V>,
        mapper: (K, V) -> F
    ) {
        map(mapped, source, mapper)
    }

    private fun <E, F> map(
        mapped: ObservableList<F>, source: ObservableList<out E>,
        mapper: (E) -> F
    ): Any {
        val contentMapping = ListContentMapping(mapped, mapper)
        mapped.setAll(source.map { o -> mapper.invoke(o) })
//        source.removeListener(contentMapping)
//        source.addListener(contentMapping)
        return contentMapping
    }
    private fun <K, V, F> map(
        mapped: ObservableList<F>, source: ObservableMap<out K, out V>,
        mapper: (K, V) -> F
    ): Any {
        val contentMapping = MapContentMapping(mapped, mapper)
        mapped.setAll(source.map { (k, v) -> mapper.invoke(k, v) })
        source.removeListener(contentMapping)
        source.addListener(contentMapping)
        return contentMapping
    }

    private class ListContentMapping<E, F>(mapped: MutableList<F>, private val mapper: (E) -> F) :
        ListChangeListener<E>, WeakListener {
        private val mappedRef: WeakReference<MutableList<F>> = WeakReference(mapped)

        override fun onChanged(change: ListChangeListener.Change<out E>) {
            val mapped = mappedRef.get()
            if (mapped == null) {
                change.list.removeListener(this)
            } else {
                while (change.next()) {
                    if (change.wasPermutated()) {
                        mapped.subList(change.from, change.to).clear()
                        mapped.addAll(change.from, change.list.subList(change.from, change.to)
                            .map { o -> mapper.invoke(o) }
                        )
                    } else {
                        if (change.wasRemoved()) {
                            mapped.subList(change.from, change.from + change.removedSize).clear()
                        }
                        if (change.wasAdded()) {
                            mapped.addAll(change.from, change.addedSubList
                                .map { o -> mapper.invoke(o) }
                            )
                        }
                    }
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

            if (obj is ListContentMapping<*, *>) {
                val mapped2 = obj.mappedRef.get()
                return mapped1 === mapped2
            }
            return false
        }
    }

    private class MapContentMapping<K, V, F>(mapped: MutableList<F>, private val mapper: (K, V) -> F) :
        MapChangeListener<K, V>, WeakListener {
        private val mappedRef: WeakReference<MutableList<F>> = WeakReference(mapped)
        private val indexMap: MutableMap<K, Int> = mutableMapOf()

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
                    mapped.removeAt(indexMap[change.key]!!)
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