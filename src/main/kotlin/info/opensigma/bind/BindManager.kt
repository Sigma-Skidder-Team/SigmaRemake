package info.opensigma.bind

import info.opensigma.OpenSigma
import java.util.HashMap
import java.util.function.Supplier

class BindManager {
    private val map = HashMap<Supplier<Int>, Runnable>()

    fun init() {
        OpenSigma.instance.eventBus.subscribe(this)
    }

    fun add(integer: Supplier<Int>, runnable: Runnable) {
        map[integer] = runnable
    }

    companion object
}