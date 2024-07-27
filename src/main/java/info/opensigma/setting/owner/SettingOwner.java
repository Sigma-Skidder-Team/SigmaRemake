package info.opensigma.setting.owner;

import info.opensigma.setting.Setting;
import info.opensigma.system.ElementRepository;
import info.opensigma.system.INameable;
import net.jezevcik.argon.utils.objects.NullUtils;

public class SettingOwner extends ElementRepository<Setting> {

    public SettingOwner(final INameable owner) {
        super(String.format("%s-settings", owner.getName()), new Object[] { owner }, Setting.class);
    }

    public final boolean getBooleanValue(final String name) {
        return (boolean) NullUtils.requireNotNull(getByName(name).getValue(), String.format("Setting %s does not exist!", name));
    }

    public final int getIntValue(final String name) {
        return (int) NullUtils.requireNotNull(getByName(name).getValue(), String.format("Setting %s does not exist!", name));
    }

    public final float getFloatValue(final String name) {
        return (float) NullUtils.requireNotNull(getByName(name).getValue(), String.format("Setting %s does not exist!", name));
    }

    public final double getDoubleValue(final String name) {
        return (double) NullUtils.requireNotNull(getByName(name).getValue(), String.format("Setting %s does not exist!", name));
    }

    public final long getLongValue(final String name) {
        return (long) NullUtils.requireNotNull(getByName(name).getValue(), String.format("Setting %s does not exist!", name));
    }

    public final short getShortValue(final String name) {
        return (short) NullUtils.requireNotNull(getByName(name).getValue(), String.format("Setting %s does not exist!", name));
    }

    public final byte getByteValue(final String name) {
        return (byte) NullUtils.requireNotNull(getByName(name).getValue(), String.format("Setting %s does not exist!", name));
    }

    public final char getCharValue(final String name) {
        return (char) NullUtils.requireNotNull(getByName(name).getValue(), String.format("Setting %s does not exist!", name));
    }

    public final String getStringValue(final String name) {
        return (String) NullUtils.requireNotNull(getByName(name).getValue(), String.format("Setting %s does not exist!", name));
    }

    public final Object getObjectValue(final String name) {
        return NullUtils.requireNotNull(getByName(name).getValue(), String.format("Setting %s does not exist!", name));
    }

}
