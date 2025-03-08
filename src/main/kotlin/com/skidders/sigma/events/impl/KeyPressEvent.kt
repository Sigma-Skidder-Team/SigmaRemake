package com.skidders.sigma.events.impl;

import com.skidders.sigma.events.Event

data class KeyPressEvent(val key: Int, val modifiers: Int, val action: Int) : Event() {
}
