package net.jezevcik.workers.impl;

import net.jezevcik.workers.Worker;
import net.jezevcik.workers.log.WorkerLoggerInterface;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Runs all tasks in a different thread.
 */
public class AsynchronousWorker extends Worker {

    /**
     * The list of created threads.
     */
    private final List<Thread> threads = new ArrayList<>();

    public AsynchronousWorker(final WorkerLoggerInterface loggerInterface, final String name, final Object... waiting) {
        super(loggerInterface, name, waiting);
    }

    @Override
    public synchronized void run() {
        tasks.forEach(runnable -> {
            final Thread thread = new Thread(() -> {
                try {
                    runnable.run();
                } catch (Exception e) {
                    this.loggerInterface.accept(false, "Worker {} has crashed", e, this.name);
                    crashed = true;
                }
            });

            thread.start();

            threads.add(thread);
        });

        new Thread(() -> {
            while (!finished || crashed) {
                final Iterator<Thread> iterator = threads.iterator();

                if (!iterator.hasNext())
                    break;

                while (iterator.hasNext()) {
                    if (!iterator.next().isAlive())
                        iterator.remove();
                }
            }

            finished = true;

            this.loggerInterface.accept(false, "Worker {} finished!", null, this.name);

            for (Object o : waiting) {
                synchronized (o) {
                    o.notify();
                }
            }
        }).start();
    }

}
