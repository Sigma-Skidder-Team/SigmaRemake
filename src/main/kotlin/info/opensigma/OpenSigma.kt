package info.opensigma

import info.opensigma.bind.BindManager
import info.opensigma.module.Module
import info.opensigma.system.ElementRepository
import info.opensigma.system.IClientInitialize
import meteordevelopment.orbit.EventBus
import net.fabricmc.api.ModInitializer
import net.jezevcik.workers.Worker
import net.jezevcik.workers.impl.AsynchronousWorker
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.reflections.Reflections
import java.lang.invoke.MethodHandles
import kotlin.system.exitProcess

open class OpenSigma : ModInitializer, IClientInitialize {
    val LOGGER: Logger = LogManager.getLogger()

    companion object {
        val instance: OpenSigma = OpenSigma()
    }

    private val clientStartup = AsynchronousWorker(
        { error, message, e, arguments ->
            if (!error) {
                if (arguments != null)
                    LOGGER.info(message, *arguments)
                else
                    LOGGER.info(message)

                return@AsynchronousWorker
            }

            if (e != null && arguments != null)
                LOGGER.error(message, e, *arguments)
            else if (e != null)
                LOGGER.error(message, e)
            else if (arguments != null)
                LOGGER.error(message, *arguments)
            else
                LOGGER.error(message)
        },
        "Client-Startup",
        this
    )

    private val reflections = Reflections("info.opensigma")
    private val eventBus = EventBus()

    val bindManager = BindManager()
    val modules = ElementRepository<Module>("modules", Module::class.java);

    /**
     * DON'T USE THIS !
     * Use the injected initialization methods instead.
     */
    override fun onInitialize() {}

    /**
     * Ran when Minecraft is still starting up.
     * Resources are usually not going to be usable at this point in time,
     * but is useful for loading objects not depended on Minecraft.
     */
    override fun onMinecraftStartup() {
        clientStartup.addTask {
            eventBus.registerLambdaFactory(
                "info.opensigma",
                { lookupInMethod,
                  klass ->
                    lookupInMethod.invoke(
                        null,
                        klass,
                        MethodHandles.lookup()
                    ) as MethodHandles.Lookup
                })
        }
        clientStartup.addTask(bindManager::init)
        clientStartup.addTask(modules::onMinecraftStartup)
        clientStartup.start()
    }

    /**
     * Ran just before Minecraft displays the title screen.
     * All required resources should be accessible by now.
     */
    override fun onMinecraftLoad() {
        if (clientStartup.state != Worker.State.FINISHED) {
            synchronized(this) {
                try {
                    (this as Object).wait()
                } catch (e: InterruptedException) {
                    fatal("An exception occurred during client startup", e)
                }
            }
        }

        modules.onMinecraftLoad()
    }

    /**
     * Prints the exception and stops the client.
     *
     * @param message  The message explaining the cause of the exception.
     * @param e        The exception that caused the client to malfunction.
     */
    fun fatal(message: String, e: Exception) = fatal(message, e, null)

    /**
     * Prints the exception and stops the client.
     *
     * @param message  The message explaining the cause of the exception.
     * @param e        The exception that caused the client to malfunction.
     * @oaram arguments The values to be filled in placeholders declared in message
     */
    fun fatal(message: String, e: Exception, vararg arguments: Any?) {
        if (arguments.isEmpty()) {
            LOGGER.error(message, e)
        } else {
            LOGGER.error(message, e, *arguments)
        }

        exitProcess(1)
    }
}