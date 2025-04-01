package com.skidders.sigma.handler;

import com.skidders.sigma.util.client.interfaces.ISubscriber;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Handler<T> implements ISubscriber {
    protected List<T> list = new ArrayList<>();

    public void init() {
        bus.register(this);
    }

    public <V extends T> V getByClass(Class<V> clazz) {
        T obj = list.stream().filter(t -> t.getClass().equals(clazz)).findFirst().orElse(null);

        if (obj == null) {
            return null;
        }

        return clazz.cast(obj);
    }
}
