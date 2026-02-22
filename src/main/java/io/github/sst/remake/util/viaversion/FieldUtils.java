package io.github.sst.remake.util.viaversion;

import io.github.sst.remake.Client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FieldUtils {
    private static Object configInst;
    private static Method getClientSideVersionMethod;

    public static ViaProtocols getProtocol() {
        return ViaProtocols.getByProtocol(getClientSideVersion());
    }

    public static int getClientSideVersion() {
        if (!ClassUtils.VIA_FABRIC_EXISTS) return -1;

        if (getClientSideVersionMethod != null) {
            try {
                return (int) getClientSideVersionMethod.invoke(configInst);
            } catch (Exception e) {
                Client.LOGGER.error("Failed to grab cached version", e);
            }
        }

        try {
            Class<?> viaFabricClass = Class.forName("com.viaversion.fabric.mc1165.ViaFabric");
            Field vfConfigField = viaFabricClass.getField("config");
            configInst = vfConfigField.get(null);

            if (configInst == null) {
                return -1;
            }

            getClientSideVersionMethod = configInst.getClass().getMethod("getClientSideVersion");
            return (int) getClientSideVersionMethod.invoke(configInst);
        } catch (Exception e) {
            Client.LOGGER.error("Failed to grab version", e);
            return -1;
        }
    }
}
