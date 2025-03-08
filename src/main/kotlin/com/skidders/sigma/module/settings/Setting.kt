package com.skidders.sigma.module.settings

open class Setting<T> {

    val name: String
    val desc: String
    var value: T

    constructor(name: String, desc: String, value: T) {
        this.name = name
        this.desc = desc
        this.value = value
    }

}
