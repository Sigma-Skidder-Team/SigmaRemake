package net.jezevcik.workers.log;

public interface WorkerLoggerInterface {

    void accept(final String message, final Exception e, final Object[] arguments);

}
