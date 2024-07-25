package net.jezevcik.workers;

import net.jezevcik.workers.log.WorkerLoggerInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs tasks and reports their progress.
 * The running of the tasks is implemented by subclasses.
 */
public abstract class Worker {

    /**
     * Logs errors in tasks or the worker itself to here.
     */
    protected final WorkerLoggerInterface loggerInterface;

    /**
     * The tasks which are to run when the worker is started.
     */
    protected final List<Runnable> tasks = new ArrayList<>();
    /**
     * Objects waiting for the worker to finish.
     */
    protected final Object[] waiting;

    /**
     * The name of the worker - used for logging.
     */
    public final String name;

    /**
     * Whether the worker has finished running all the tasks.
     */
    protected volatile boolean finished = false,
    /**
     * Whether the worker has started running the tasks.
     */
    started = false,
    /**
     * Whether any of the tasks have crashed.
     */
    crashed = false;

    public Worker(final WorkerLoggerInterface loggerInterface, final String name, final Object... waiting) {
        this.name = name;
        this.waiting = waiting;
        this.loggerInterface = loggerInterface;
    }

    /**
     * Adds a task to the list of tasks which will run once the worker starts.
     *
     * @param task The task which will be added
     */
    public void addTask(Runnable task) {
        tasks.add(task);
    }

    /**
     * Starts running all the tasks.
     */
    public void start() {
        started = true;
        run();
    }

    /**
     * Starts running all the tasks, implemented by different workers.
     */
    protected abstract void run();

    /**
     * Gets the state the worker is in
     *
     * @return The state the worker is in
     */
    public synchronized State getState() {
        if (!started)
            return State.NOT_STARTED;

        if (crashed)
            return State.CRASHED;

        return finished ?
                State.FINISHED : State.RUNNING;
    }

    public enum State {
        FINISHED, CRASHED, NOT_STARTED, RUNNING;
    }

}
