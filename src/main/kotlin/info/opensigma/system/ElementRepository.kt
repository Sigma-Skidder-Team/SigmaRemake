package info.opensigma.system

import info.opensigma.OpenSigma
import info.opensigma.util.reflections.ClassUtils
import java.util.concurrent.CopyOnWriteArrayList

open class ElementRepository<T>(
    protected val id: String,
    protected val mainClass: Class<T>,
    protected val reflectClasses: Boolean = true,
    protected val reflectFields: Boolean = false,
    protected val toScan: Array<out Any>? = null,
    protected val allowInternalRepositories: Boolean = true
) : CopyOnWriteArrayList<T>(), IClientInitialize {

    protected val foundClasses = mutableListOf<Class<out T>>()

    override fun onMinecraftStartup() {
        if (reflectClasses) {
            OpenSigma.instance.reflections.getSubTypesOf(mainClass).forEach { klass ->
                if (ClassUtils.hasParameterlessPublicConstructor(klass)) {
                    foundClasses.add(klass)
                }
            }
        }

        if (allowInternalRepositories) {
            this.forEach { o ->
                if (o is ElementRepository<*>) {
                    o.onMinecraftStartup()
                }
            }
        }
    }

    override fun onMinecraftLoad() {
        if (reflectClasses && foundClasses.isNotEmpty()) {
            foundClasses.forEach { klass ->
                try {
                    this.add(klass.getDeclaredConstructor().newInstance())
                } catch (e: Exception) {
                    OpenSigma.instance.fatal("Failed to initialize stored classes in repository {}", e, this.id)
                }
            }
        }

        try {
            if (reflectFields && toScan != null) {
                for (it in toScan) {
                    val klass = it.javaClass

                    for (field in klass.fields) {
                        field.isAccessible = true

                        val fieldObject = field.get(it)

                        if (fieldObject != null && mainClass.isAssignableFrom(fieldObject.javaClass)) {
                            this.add(fieldObject as T)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            OpenSigma.instance.fatal("Failed to search for fields in provided objects in repository {}", e, this.id)
        }

        if (allowInternalRepositories) {
            this.forEach { o ->
                if (o is ElementRepository<*>) {
                    o.onMinecraftLoad()
                }
            }
        }

        OpenSigma.LOGGER.info("Repository {} loaded {} elements", id, size)
    }

    @Suppress("UNCHECKED_CAST")
    fun getByName(name: String): T? {
        for (o in this) {
            if (o is INameable && o.name == name) {
                return o as T
            } else if (o.toString() == name) {
                return o as T
            }
        }
        return null
    }
}