package info.opensigma.system;

import info.opensigma.OpenSigma;
import info.opensigma.util.reflections.ClassUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ElementRepository<T> extends CopyOnWriteArrayList<T> implements IClientInitialize {

    private final String id;
    private final Class<T> mainClass;
    private final boolean reflectClasses, reflectFields;
    private final Object[] toScan;

    private final List<Class<? extends T>> foundClasses = new ArrayList<>();

    public ElementRepository(String id, Class<T> mainClass, boolean reflectClasses, boolean reflectFields, Object[] toScan) {
        this.id = id;
        this.mainClass = mainClass;
        this.reflectClasses = reflectClasses;
        this.reflectFields = reflectFields;
        this.toScan = toScan;
    }

    public ElementRepository(String id, Class<T> mainClass) {
        this(id, mainClass, true, false, null);
    }

    public ElementRepository(String id, Object[] toScan) {
        this(id, null, false, true, toScan);
    }

    @Override
    public void onMinecraftStartup() {
        if (reflectClasses) {
            OpenSigma.getInstance().reflections.getSubTypesOf(mainClass).forEach(klass -> {
                if (ClassUtils.hasParameterlessPublicConstructor(klass))
                    foundClasses.add(klass);
            });
        }
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
                        final Object fieldObject = field.get(it);

                        if (fieldObject.getClass().isInstance(mainClass) && !fieldObject.getClass().equals(mainClass)) {
                            this.add((T) fieldObject);
                        }
                    }
                }
            }
        } catch (final Exception e) {
            OpenSigma.getInstance().fatal("Failed to search for fields in provided objects in repository {}", e, this.id);
        }

        OpenSigma.LOGGER.info("Repository {} loaded {} elements", id, size());
    }

}
