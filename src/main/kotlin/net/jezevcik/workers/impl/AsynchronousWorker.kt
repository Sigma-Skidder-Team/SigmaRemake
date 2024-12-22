package net.jezevcik.workers.impl

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class AsynchronousWorker(
    private val loggerInterface: (Boolean, String, Exception?, Array<out Any>?) -> Unit,
    private val name: String,
    private vararg val waiting: Any
) {
    private val tasks = mutableListOf<suspend () -> Unit>()
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var job: Job

    fun addTask(task: suspend () -> Unit) {
        tasks.add(task)
    }

    fun start() {
        job = scope.launch {
            try {
                val time = measureTimeMillis {
                    tasks.forEach { task ->
                        launch { task() }
                    }
                }
                loggerInterface(false, "Worker $name finished in $time ms!", null, emptyArray())
            } catch (e: Exception) {
                loggerInterface(true, "Worker $name has crashed", e, emptyArray())
            } finally {
                waiting.forEach {
                    synchronized(it) { (it as Object).notify() }
                }
            }
        }
    }

    suspend fun awaitCompletion() {
        job.join()
    }
}