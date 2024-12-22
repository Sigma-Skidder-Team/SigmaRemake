package net.jezevcik.workers.log

interface WorkerLoggerInterface {
    fun accept(error: Boolean, message: String, e: Exception?, vararg arguments: Any)
}