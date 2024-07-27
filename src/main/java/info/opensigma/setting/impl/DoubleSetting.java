package info.opensigma.setting.impl;

import info.opensigma.setting.impl.primitive.PrimitiveSetting;

public class DoubleSetting extends PrimitiveSetting<Double> {

    public final double min, max;

    public DoubleSetting(String name, String description, Double value, final double min, final double max) {
        super(name, description, value, null, v -> v > min && v < max);
        this.min = min;
        this.max = max;
    }
}
