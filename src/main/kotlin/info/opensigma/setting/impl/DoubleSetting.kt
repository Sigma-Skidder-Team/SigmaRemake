package info.opensigma.setting.impl

import info.opensigma.setting.impl.primitive.PrimitiveSetting

class DoubleSetting(
    name: String,
    description: String,
    value: Double,
    private val min: Double,
    private val max: Double
) : PrimitiveSetting<Double>(name, description, value, null, { it in min..max })