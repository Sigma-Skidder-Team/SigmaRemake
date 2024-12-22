package net.jezevcik.workers

import net.jezevcik.workers.log.WorkerLoggerInterface

/**
 * Runs tasks and reports their progress.
 * The running of the tasks is implemented by subclasses.
 */
public abstract class Worker(
    /**
     * Logs errors in tasks or the worker itself to here.
     */
    public val loggerInterface: WorkerLoggerInterface,

    /**
     * The name of the worker - used for logging.
     */
    public val name: String,

    /**
     * Objects waiting for the worker to finish.
     */
    public val waiting: Array<Any>
) {

    /**
     * The tasks which are to run when the worker is started.
     */
    protected val tasks = mutableListOf<Runnable>()

    /**
     * Whether the worker has finished running all the tasks.
     */
    protected var finished = false

    /**
     * Whether the worker has started running the tasks.
     */
    protected var started = false

    /**
     * Whether any of the tasks have crashed.
     */
    protected var crashed = false

    /**
     * Adds a task to the list of tasks which will run once the worker starts.
     *
     * @param task The task which will be added
     */
    public fun addTask(task: Runnable) {
        tasks.add(task)
    }

    /**
     * Starts running all the tasks.
     */
    public fun start() {
        started = true
        run()
    }

    /**
     * Starts running all the tasks, implemented by different workers.
     */
    protected abstract fun run()

    /**
     * Gets the state the worker is in
     *
     * @return The state the worker is in
     */
    public val state: State
        get() = when {
            !started -> State.NOT_STARTED
            crashed -> State.CRASHED
            finished -> State.FINISHED
            else -> State.RUNNING
        }

    public enum class State {
        FINISHED, CRASHED, NOT_STARTED, RUNNING
    }

}