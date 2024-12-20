package info.opensigma.util.reflections

/**
 * A set of methods used for helping while using reflections.
 */
object ClassUtils {

    /**
     * Checks whether a class has a public constructor without any parameters.
     *
     * @param clazz The clazz whose constructor will be checked.
     * @return      Result.
     */
    fun hasParameterlessPublicConstructor(clazz: Class<*>): Boolean {
        return clazz.constructors.any { it.parameterCount == 0 }
    }

}