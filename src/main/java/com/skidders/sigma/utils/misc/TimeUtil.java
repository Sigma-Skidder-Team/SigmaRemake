package com.skidders.sigma.utils.misc;

import java.util.function.LongSupplier;

public class TimeUtil {

    public static LongSupplier nanoTimeSupplier = System::nanoTime;

    public static long nanoTime()
    {
        return nanoTimeSupplier.getAsLong();
    }

    public static long milliTime()
    {
        return nanoTime() / 1000000L;
    }

}
