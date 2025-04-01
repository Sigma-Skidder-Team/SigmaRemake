package com.skidders.sigma.util.client.events;

import java.lang.reflect.Method;

public class Handler {
    public Object listener;
    public Method method;
    public Priority priority;

    public Handler(Object listener, Method method, Priority priority) {
        this.listener = listener;
        this.method = method;
        this.priority = priority;
    }
}