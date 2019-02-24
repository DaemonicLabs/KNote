package knote.util

class MutableKObservableList<E>: ArrayList<E>(), KObservableList<E> {
    override val callbacks: MutableList<(List<E>, List<E>) -> Unit> = mutableListOf()

    fun execCallback(oldValue: List<E>, newValue: List<E>) {
        callbacks.forEach {
            it.invoke(oldValue, newValue)
        }
    }


    override fun add(element: E): Boolean {
        val oldValue =  this.clone() as List<E>
        return super.add(element).also {
            execCallback(oldValue, this)
        }
    }

    override fun add(index: Int, element: E) {
        val oldValue =  this.clone() as List<E>
        super.add(index, element).also {
            execCallback(oldValue, this)
        }
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        val oldValue =  this.clone() as List<E>
        return super.addAll(index, elements).also {
            execCallback(oldValue, this)
        }
    }

    override fun addAll(elements: Collection<E>): Boolean {
        val oldValue =  this.clone() as List<E>
        return super.addAll(elements).also {
            execCallback(oldValue, this)
        }
    }

    override fun clear() {
        val oldValue =  this.clone() as List<E>
        return super.clear().also {
            execCallback(oldValue, this)
        }
    }

    override fun remove(element: E): Boolean {
        val oldValue =  this.clone() as List<E>
        return super.remove(element).also {
            execCallback(oldValue, this)
        }
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        val oldValue =  this.clone() as List<E>
        return super.removeAll(elements).also {
            execCallback(oldValue, this)
        }
    }

    override fun removeAt(index: Int): E {
        val oldValue =  this.clone() as List<E>
        return super.removeAt(index).also {
            execCallback(oldValue, this)
        }
    }

    override fun set(index: Int, element: E): E {
        val oldValue =  this.clone() as List<E>
        return super.set(index, element).also {
            execCallback(oldValue, this)
        }
    }
}