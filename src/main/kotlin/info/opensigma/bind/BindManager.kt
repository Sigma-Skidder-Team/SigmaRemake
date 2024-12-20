package info.opensigma.bind

import info.opensigma.OpenSigma
import info.opensigma.event.KeyPressEvent
import meteordevelopment.orbit.EventHandler
import java.util.HashMap
import java.util.function.Supplier

class BindManager {
    private val map = HashMap<Supplier<Int>, Runnable>()

    @EventHandler
    private fun onKey(keyPressEvent: KeyPressEvent) {
        if (keyPressEvent.keyAction != 0)
            return

        map.forEach { (key, value) ->
            if (key.get() == keyPressEvent.key)
                value.run()
        }
    }

    fun init() {
        OpenSigma.instance.eventBus.subscribe(this)
    }

    fun add(integer: Supplier<Int>, runnable: Runnable) {
        map[integer] = runnable
    }

    companion object {
        val instance = BindManager()
    }
}