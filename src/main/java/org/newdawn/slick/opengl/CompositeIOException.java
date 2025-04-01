package org.newdawn.slick.opengl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection of IOException that failed image data loading
 *
 * @author kevin
 */
public class CompositeIOException extends IOException {
    /**
     * The list of exceptions causing this one
     */
    private final List<Exception> exceptions = new ArrayList<>();

    /**
     * Create a new composite IO Exception
     */
    public CompositeIOException() {
        super();
    }

    /**
     * Add an exception that caused this exceptino
     *
     * @param e The exception
     */
    public void addException(Exception e) {
        exceptions.add(e);
    }

    /**
     * @see Throwable#getMessage()
     */
    public String getMessage() {
        StringBuilder msg = new StringBuilder("Composite Exception: \n");
        for (Exception exception : exceptions) {
            msg.append("\t").append(exception.getMessage()).append("\n");
        }

        return msg.toString();
    }
}