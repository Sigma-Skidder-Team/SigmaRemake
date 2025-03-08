package com.skidders.sigma.utils.misc;

object TimeUtil {

    fun nanoTime(): Long
    {
        return System.nanoTime()
    }

    fun milliTime(): Long
    {
        return nanoTime() / 1000000L
    }

}
