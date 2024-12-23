package com.skidders.sigma.module.settings;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Setting<T> {

    public final String name, desc;
    public T value;

}
