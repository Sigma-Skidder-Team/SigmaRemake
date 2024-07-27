package info.opensigma.system;

import info.opensigma.OpenSigma;
import info.opensigma.util.reflections.ClassUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ElementRepository<T> extends CopyOnWriteArrayList<T> implements IClientInitialize {

    protected final String id;
    protected final Class<T> mainClass;
    protected final boolean reflectClasses, reflectFields;
    protected final Object[] toScan;
    protected final boolean allowInternalRepositories;

    protected final List<Class<? extends T>> foundClasses = new ArrayList<>();

    public ElementRepository(
            final String id,
            final Class<T> mainClass,
            final boolean reflectClasses,
            final boolean reflectFields,
            final Object[] toScan,
            final boolean allowInternalRepositories
    ) {
        this.id = id;
        this.mainClass = mainClass;
        this.reflectClasses = reflectClasses;
        this.reflectFields = reflectFields;
        this.toScan = toScan;
        this.allowInternalRepositories = allowInternalRepositories;
    }

    public ElementRepository(final String id, final Class<T> mainClass) {
        this(id, mainClass, true, false, null, true);
    }

    public ElementRepository(final String id, final Object[] toScan, final Class<T> mainClass) {
        this(id, mainClass, false, true, toScan, true);
    }

    @Override
    public void onMinecraftStartup() {
        if (reflectClasses) {
            OpenSigma.getInstance().reflections.getSubTypesOf(mainClass).forEach(klass -> {
                if (ClassUtils.hasParameterlessPublicConstructor(klass))
                    foundClasses.add(klass);
            });
        }

        if (allowInternalRepositories)
            this.forEach(o -> {
                if (o instanceof ElementRepository<?> repository)
                    repository.onMinecraftStartup();
            });
    }

    @Override
    public void onMinecraftLoad() {
        if (reflectClasses && !foundClasses.isEmpty()) {
            foundClasses.forEach(klass -> {
                try {
                    this.add(klass.getDeclaredConstructor().newInstance());
                } catch (Exception e) {
                    OpenSigma.getInstance().fatal("Failed to initialize stored classes in repository {}", e, this.id);
                }
            });
        }

        try {
            if (reflectFields && toScan != null) {
                for (final Object it : toScan) {
                    final Class<?> klass = it.getClass();

                    for (final Field field : klass.getFields()) {
                        field.setAccessible(true);

                        final Object fieldObject = field.get(it);

                        if (fieldObject == null)
                            continue;

                        if (mainClass.isAssignableFrom(fieldObject.getClass())) {
                            this.add((T) fieldObject);
                        }
                    }
                }
            }
        } catch (final Exception e) {
            OpenSigma.getInstance().fatal("Failed to search for fields in provided objects in repository {}", e, this.id);
        }

        if (allowInternalRepositories)
            this.forEach(o -> {
                if (o instanceof ElementRepository<?> repository)
                    repository.onMinecraftLoad();
            });

        OpenSigma.LOGGER.info("Repository {} loaded {} elements", id, size());
    }

    @SuppressWarnings("unchecked")
    public final <O extends T> O getByClass(final Class<O> klass) {
        return (O) this.stream().filter(o -> o.getClass().equals(klass)).findAny().orElse(null);
    }

    @SuppressWarnings("unchecked")
    public final T getByName(final String name) {
        for (Object o : this) {
            if (o instanceof INameable nameable)
                if (nameable.getName().equals(name))
                    return (T) o;
            else
                if (o.toString().equals(name))
                    return (T) o;
        }

        return null;
    }

}
