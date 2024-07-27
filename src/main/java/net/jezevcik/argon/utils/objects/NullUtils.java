package net.jezevcik.argon.utils.objects;

/**
 * A set of methods used for interacting with objects that are or may be null.
 */
public class NullUtils {

    /**
     * If the first object is null, the second will be returned.
     *
     * @param object The first object. Will be returned if it is not null.
     * @param ifElse The second object. Will be returned if the first one is null.
     * @return       The first object if it is not null, otherwise the second one.
     * @param <T>    The type of both objects.
     */
    public static <T> T nullOrElse(final T object, final T ifElse) {
        return object == null ? ifElse : object;
    }

    /**
     * Throws a null pointer exception if the provided object is null.
     *
     * @param object The object which will be checked.
     * @return       The provided object, if it is not null.
     * @param <T>    The type of the provided object.
     */
    public static <T> T requireNotNull(final T object) {
        if (object == null)
            throw new NullPointerException();

        return object;
    }

    /**
     * Throws a null pointer exception with a provided message if the provided object is null.
     *
     * @param object    The object which will be checked.
     * @param exception The message to be returned if the object is null.
     * @return          The provided object, if it is not null.
     * @param <T>       The type of the provided object.
     */
    public static <T> T requireNotNull(final T object, final String exception) {
        if (object == null)
            throw new NullPointerException(exception);

        return object;
    }

}
