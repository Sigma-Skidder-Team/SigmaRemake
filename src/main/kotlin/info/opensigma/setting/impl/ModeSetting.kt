package info.opensigma.setting.impl;

import info.opensigma.setting.Setting;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.system.CallbackI;

public class ModeSetting extends Setting<String> {

    public final String[] values;

    private String value;

    public ModeSetting(String name, String description, String[] values, String value) {
        super(name, description);
        this.values = values;
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(final String value) {
        if (!ArrayUtils.contains(values, value))
            return;

        this.value = value;
    }
}
