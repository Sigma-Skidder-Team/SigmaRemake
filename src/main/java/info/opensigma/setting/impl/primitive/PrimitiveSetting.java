package info.opensigma.setting.impl.primitive;

import info.opensigma.setting.Setting;

import java.util.function.Predicate;

/**
 * A simple setting that just sets one and gets one single value without any additional information.
 *
 * @param <T> The type of the setting.
 */
public class PrimitiveSetting<T> extends Setting<T> {

    /**
     * The core value being manipulated.
     */
    private T value;

    /**
     * The purpose of the setting's type is to indicate to ClickGUIs how to render this setting.
     */
    public final PrimitiveSettingType type;

    /**
     * Verifies if a new value for the setting is possible.
     * Is used mostly by extenders of this setting.
     * Is null if no predicate is set.
     */
    private final Predicate<T> verifier;
    
    public PrimitiveSetting(final String name, final String description, final T value, final PrimitiveSettingType type, final Predicate<T> verifier) {
        super(name, description);

        this.value = value;
        this.type = type;
        this.verifier = verifier;
    }

    public PrimitiveSetting(final String name, final String description, final T value, final PrimitiveSettingType type) {
        this(name, description, value, type, null);
    }

    public PrimitiveSetting(final String name, final String description, final T value) {
        super(name, description);

        if (value == null) {
            type = null;
        } else if (value.getClass() == Boolean.class) {
            type = PrimitiveSettingType.BOOLEAN;
        } else if (value.getClass() == Integer.class) {
            type = PrimitiveSettingType.COLOR;
        } else {
            type = null;
        }

        this.value = value;
        this.verifier = null;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        if (this.verifier.test(value))
            this.value = value;
    }

}
