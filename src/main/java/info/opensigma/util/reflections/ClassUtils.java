package info.opensigma.util.reflections;

import java.lang.reflect.Constructor;

/**
 * A set of methods used for helping while using reflections.
 */
public class ClassUtils {

    /**
     * Checks whether a class has a public constructor without any parameters.
     *
     * @param clazz The clazz whose constructor will be checked.
     * @return      Result.
     */
    public static boolean hasParameterlessPublicConstructor(Class<?> clazz) {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (constructor.getParameterCount() == 0) {
                return true;
            }
        }

        return false;
    }

}