package io.github.sst.remake.data.bus;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {

    private final Map<Class<?>, List<Subscriber>> subscribers = new ConcurrentHashMap<>();

    public void register(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            Subscribe annotation = method.getAnnotation(Subscribe.class);
            if (annotation == null) continue;

            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1) {
                throw new IllegalStateException(
                        "@Subscribe method must have exactly one parameter: " + method
                );
            }

            Class<?> eventType = params[0];
            method.setAccessible(true);

            Subscriber subscriber = new Subscriber(
                    listener,
                    method,
                    annotation.priority()
            );

            subscribers
                    .computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                    .add(subscriber);

            // Sort by priority (higher first)
            subscribers.get(eventType)
                    .sort(Comparator.comparingInt(Subscriber::getPriority).reversed());
        }
    }

    public void unregister(Object listener) {
        for (List<Subscriber> list : subscribers.values()) {
            list.removeIf(sub -> sub.listener == listener);
        }
    }

    public void call(Object event) {
        Class<?> eventClass = event.getClass();

        // Dispatch to exact type + superclasses/interfaces
        for (Map.Entry<Class<?>, List<Subscriber>> entry : subscribers.entrySet()) {
            if (!entry.getKey().isAssignableFrom(eventClass)) continue;

            for (Subscriber subscriber : entry.getValue()) {
                try {
                    subscriber.method.invoke(subscriber.listener, event);
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Failed to invoke event handler: " + subscriber.method, e
                    );
                }
            }
        }
    }

    private static class Subscriber {
        public final Object listener;
        public final Method method;
        public final Priority priority;

        public Subscriber(Object listener, Method method, Priority priority) {
            this.listener = listener;
            this.method = method;
            this.priority = priority;
        }

        public int getPriority() {
            return priority.ordinal();
        }
    }
}
