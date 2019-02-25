package knote.logging

import ch.qos.logback.core.rolling.TriggeringPolicyBase

import java.io.File

class RollOncePerSessionTriggeringPolicy<E> : TriggeringPolicyBase<E>() {

    override fun isTriggeringEvent(activeFile: File, event: E): Boolean {
        // roll the first time when the event gets called
        if (doRolling) {
            doRolling = false
            return true
        }
        return false
    }

    companion object {
        private var doRolling = true
    }
}