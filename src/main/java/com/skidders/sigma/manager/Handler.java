package com.skidders.sigma.manager;

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
}
