package knote.util

interface MapLike<R, S> {
    operator fun get(key: R): S
}