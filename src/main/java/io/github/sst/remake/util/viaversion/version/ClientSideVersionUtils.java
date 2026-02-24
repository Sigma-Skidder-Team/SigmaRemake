package io.github.sst.remake.util.viaversion.version;

import io.github.sst.remake.Client;
import io.github.sst.remake.util.viaversion.ClassUtils;
import io.github.sst.remake.util.viaversion.ViaProtocols;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
public class ClientSideVersionUtils {

    private static Object configInst;

    private static Method getClientSideVersionMethod;
    private static Method setClientSideVersionMethod;

    private static Method setClientSideEnabledMethod;
    private static Method setHideButtonMethod;

    public static void init() {}

    static {
        if (ClassUtils.VIA_FABRIC_EXISTS) {
            try {
                ensureConfigLoaded();

                if (configInst != null) {
                    if (setClientSideEnabledMethod != null) {
                        setClientSideEnabledMethod.invoke(configInst, true);
                    }

                    if (setHideButtonMethod != null) {
                        setHideButtonMethod.invoke(configInst, true);
                    }
                }
            } catch (Exception e) {
                Client.LOGGER.error("Failed to apply ViaFabric config defaults", e);
            }
        }
    }

    public static ViaProtocols getProtocol() {
        return ViaProtocols.getByProtocol(getClientSideVersion());
    }

    public static int getClientSideVersion() {
        if (!ClassUtils.VIA_FABRIC_EXISTS) return -1;

        try {
            ensureConfigLoaded();

            if (configInst == null || getClientSideVersionMethod == null) return -1;

            return (int) getClientSideVersionMethod.invoke(configInst);
        } catch (Exception e) {
            Client.LOGGER.error("Failed to grab version", e);
            return -1;
        }
    }

    public static void setClientSideVersion(int version) {
        if (!ClassUtils.VIA_FABRIC_EXISTS) return;

        try {
            ensureConfigLoaded();

            if (configInst == null || setClientSideVersionMethod == null) return;

            setClientSideVersionMethod.invoke(configInst, version);
        } catch (Exception e) {
            Client.LOGGER.error("Failed to set client side version", e);
        }
    }

    private static void ensureConfigLoaded() throws Exception {
        if (configInst != null
                && getClientSideVersionMethod != null
                && setClientSideVersionMethod != null
                && setClientSideEnabledMethod != null
                && setHideButtonMethod != null) {
            return;
        }

        Class viaFabricClass = Class.forName("com.viaversion.fabric.mc1165.ViaFabric");
        Field vfConfigField = viaFabricClass.getField("config");

        configInst = vfConfigField.get(null);
        if (configInst == null) return;

        Class clazz = configInst.getClass();

        if (getClientSideVersionMethod == null) {
            getClientSideVersionMethod = clazz.getMethod("getClientSideVersion");
        }

        if (setClientSideVersionMethod == null) {
            setClientSideVersionMethod = clazz.getMethod("setClientSideVersion", int.class);
        }

        if (setClientSideEnabledMethod == null) {
            setClientSideEnabledMethod = clazz.getMethod("setClientSideEnabled", boolean.class);
        }

        if (setHideButtonMethod == null) {
            setHideButtonMethod = clazz.getMethod("setHideButton", boolean.class);
        }
    }
}