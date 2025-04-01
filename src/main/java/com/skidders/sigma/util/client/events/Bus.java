package com.skidders.sigma.util.client.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Bus {
    private final Map<Class<?>, List<Handler>> eventHandlers = new ConcurrentHashMap<>();

    public void register(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Listen.class) && method.getParameterCount() == 1) {
                Class<?> eventType = method.getParameterTypes()[0];
                method.setAccessible(true);
                Listen listenAnnotation = method.getAnnotation(Listen.class);
                Priority priority = listenAnnotation.priority();
                eventHandlers
                        .computeIfAbsent(eventType, k -> new ArrayList<>())
                        .add(new Handler(listener, method, priority));
                sortHandlers(eventType);
            }
        }
    }

    public void unregister(Object listener) {
        for (List<Handler> handlers : eventHandlers.values()) {
            handlers.removeIf(handler -> handler.listener == listener);
        }
    }

    public void post(Object event) {
        List<Handler> handlers = eventHandlers.get(event.getClass());
        if (handlers != null) {
            for (Handler handler : handlers) {
                try {
                    handler.method.invoke(handler.listener, event);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(String.format(
                            "Failed to call event handler method '%s' in listener '%s' for event class '%s'",
                            handler.method.getName(),
                            handler.listener.getClass().getName(),
                            event.getClass().getName()
                    ), e);
                }
            }
        }
    }

    private void sortHandlers(Class<?> eventType) {
        List<Handler> handlers = eventHandlers.get(eventType);
        if (handlers != null) {
            handlers.sort(Comparator.comparingInt(handler -> handler.priority.ordinal()));
        }
    }
}