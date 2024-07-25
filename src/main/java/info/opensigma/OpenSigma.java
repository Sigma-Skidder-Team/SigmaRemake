package info.opensigma;

import net.fabricmc.api.ModInitializer;

import net.jezevcik.workers.Worker;
import net.jezevcik.workers.impl.AsynchronousWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Async;
import org.lwjgl.system.CallbackI;

public final class OpenSigma implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("OpenSigma");

	private static OpenSigma instance;

	private final AsynchronousWorker clientStartup = new AsynchronousWorker(((message, e, arguments) -> {
		if (e != null && arguments != null)
			LOGGER.error(message, e, arguments);
		else if (e != null)
			LOGGER.error(message, e);
		else if (arguments != null)
			LOGGER.error(message, arguments);
		else
			LOGGER.error(message);
	}), "Client-Startup", this);

	/**
	 * DON'T USE THIS !
	 * Use the injected initialization methods instead.
	 */
	@Override
	public void onInitialize() { }

	/**
	 * Ran when Minecraft is still starting up.
	 * Resources are usually not going to be usable at this point in time,
	 * but is useful for loading objects not depended on Minecraft.
	 */
	public void onMinecraftStartup() {

	}

	/**
	 * Ran just before Minecraft displays the title screen.
	 * All required resources should be accessible by now.
	 */
	public void onMinecraftLoad() {
		if (clientStartup.getState() != Worker.State.FINISHED) {
			synchronized (this) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					fatal("An exception occurred during client startup", e);
				}
			}
		}

	}

	/**
	 * Prints the exception and stops the client.
	 * @param message The message explaining the cause of the exception.
	 * @param e       The exception that caused the client to malfunction.
	 */
	public void fatal(final String message, final Exception e) {
		LOGGER.fatal(message, e);
		System.exit(1);
	}

	/**
	 * Don't mess with this method, it may look a bit
	 * weird, but that's to prevent concurrency issues.
	 * Is an implementation of the Singleton design pattern.
	 *
	 * @return An instance of the OpenSigma class.
	 */
	public static OpenSigma getInstance() {
		OpenSigma result = instance;

		if (result != null) {
			return result;
		}

		synchronized(OpenSigma.class) {
			if (instance == null) {
				instance = new OpenSigma();
			}

			return instance;
		}
	}
}