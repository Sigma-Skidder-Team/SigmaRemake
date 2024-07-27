package info.opensigma.bind;

import info.opensigma.OpenSigma;
import info.opensigma.event.KeyPressEvent;
import meteordevelopment.orbit.EventHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class BindManager {

    private final Map<Supplier<Integer>, Runnable> map = new HashMap<>();

    @EventHandler
    private void onKey(KeyPressEvent keyPressEvent) {
        if (keyPressEvent.keyAction != 0)
            return;

        this.map.forEach((key, value) -> {
            if (key.get() == keyPressEvent.key)
                value.run();
        });
    }

    public void init() {
        OpenSigma.getInstance().eventBus.subscribe(this);
    }

    public void add(final Supplier<Integer> integer, final Runnable runnable) {
        this.map.put(integer, runnable);
    }

}
