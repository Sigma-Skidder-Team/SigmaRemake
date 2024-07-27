package info.opensigma;

import info.opensigma.bind.BindManager;
import info.opensigma.module.Module;
import info.opensigma.system.ElementRepository;
import info.opensigma.system.IClientInitialize;
import meteordevelopment.orbit.EventBus;
import net.fabricmc.api.ModInitializer;
import net.jezevcik.workers.Worker;
import net.jezevcik.workers.impl.AsynchronousWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

import java.lang.invoke.MethodHandles;

public final class OpenSigma implements ModInitializer, IClientInitialize {

    public static final Logger LOGGER = LogManager.getLogger();

	private static OpenSigma instance;

	private final AsynchronousWorker clientStartup = new AsynchronousWorker(((error, message, e, arguments) -> {
		if (!error) {
			if (arguments != null)
				LOGGER.info(message, arguments);
			else
				LOGGER.info(message);

			return;
		}

		if (e != null && arguments != null)
			LOGGER.error(message, e, arguments);
		else if (e != null)
			LOGGER.error(message, e);
		else if (arguments != null)
			LOGGER.error(message, arguments);
		else
			LOGGER.error(message);
	}), "Client-Startup", this);

	public final Reflections reflections = new Reflections("info.opensigma");
	public final EventBus eventBus = new EventBus();

	public final BindManager bindManager = new BindManager();
	public final ElementRepository<Module> modules = new ElementRepository<>("modules", Module.class) {
		@Override
		public void onMinecraftLoad() {
			super.onMinecraftLoad();
			this.forEach(Module::init);
		}
	};

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
		clientStartup.addTask(() -> {
			eventBus.registerLambdaFactory("info.opensigma", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
		});

		clientStartup.addTask(bindManager::init);

		clientStartup.addTask(modules::onMinecraftStartup);

		clientStartup.start();
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

		modules.onMinecraftLoad();
	}

	/**
	 * Prints the exception and stops the client.
	 *
	 * @param message The message explaining the cause of the exception.
	 * @param e       The exception that caused the client to malfunction.
	 */
	public void fatal(final String message, final Exception e) {
		fatal(message, e, null);
	}

	/**
	 * Prints the exception and stops the client.
	 *
	 * @param message 	The message explaining the cause of the exception.
	 * @param e       	The exception that caused the client to malfunction.
	 * @oaram arguments The values to be filled in placeholders declared in message
	 */
	public void fatal(final String message, final Exception e, Object... arguments) {
		if (arguments == null || arguments.length == 0)
			LOGGER.error(message, e);
		else
			LOGGER.error(message, e, arguments);

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