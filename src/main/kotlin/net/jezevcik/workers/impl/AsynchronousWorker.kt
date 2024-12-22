package net.jezevcik.workers.impl

import net.jezevcik.workers.log.WorkerLoggerInterface
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Runs all tasks in a different thread.
 */
class AsynchronousWorker(loggerInterface: WorkerLoggerInterface, name: String, vararg waiting: Any)
    : Worker(loggerInterface, name, *waiting) {

    /**
     * The list of created threads.
     */
    private val threads = CopyOnWriteArrayList<Thread>()

    override fun run() {
        tasks.forEach { runnable ->
            val thread = Thread {
                try {
                    runnable.run()
                } catch (e: Exception) {
                    loggerInterface.accept(false, "Worker {} has crashed", e, name)
                    crashed = true
                }
            }
            thread.start()
            threads.add(thread)
        }

        Thread {
            while (!finished || crashed) {
                val iterator = threads.iterator()
                if (!iterator.hasNext()) break

                while (iterator.hasNext()) {
                    if (!iterator.next().isAlive) iterator.remove()
                }
            }

            finished = true
            loggerInterface.accept(false, "Worker {} finished!", null, name)

            waiting.forEach { synchronized(it) { it.notify() } }
        }.start()
    }
}