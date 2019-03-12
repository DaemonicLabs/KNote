package knote.util

class MutableKObservableList<E> : ArrayList<E>(), KObservableList<E> {
    override val callbacks: MutableList<(List<E>) -> Unit> = mutableListOf()

    fun execCallback(newValue: List<E>) {
        callbacks.forEach {
            it.invoke(newValue)
        }
    }

    override fun add(element: E): Boolean {
        return super.add(element).also {
            execCallback(this)
        }
    }

    override fun add(index: Int, element: E) {
        super.add(index, element).also {
            execCallback(this)
        }
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        return super.addAll(index, elements).also {
            execCallback(this)
        }
    }

    override fun addAll(elements: Collection<E>): Boolean {
        return super.addAll(elements).also {
            execCallback(this)
        }
    }

    override fun clear() {
        return super.clear().also {
            execCallback(this)
        }
    }

    override fun remove(element: E): Boolean {
        return super.remove(element).also {
            execCallback(this)
        }
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        return super.removeAll(elements).also {
            execCallback(this)
        }
    }

    override fun removeAt(index: Int): E {
        return super.removeAt(index).also {
            execCallback(this)
        }
    }

    override operator fun set(index: Int, element: E): E {
        return super.set(index, element).also {
            execCallback(this)
        }
    }
}