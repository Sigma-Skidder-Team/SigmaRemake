package info.opensigma.setting;

public abstract class Setting<T> implements INameable {

    public final String name, description;

    public Setting(final String name, final String description) {
        this.name = name;
        this.description = description;
    }

    public abstract T getValue();

    public abstract void setValue(final T value);

    @Override
    public String getName() {
        return name;
    }

}
