package io.github.sst.remake.util.viaversion;

import io.github.sst.remake.Client;

public class ViaInstance {

    public static boolean VIAVERSION_EXISTS = viaFabricExists() && viaVersionExists();

    private static boolean viaFabricExists() {
        try {
            Class.forName("com.viaversion.fabric.mc1165.ViaFabric");
            Client.LOGGER.info("ViaFabric is available");
            return true;
        } catch (ClassNotFoundException e) {
            Client.LOGGER.info("ViaFabric is missing");
            return false;
        }
    }

    private static boolean viaVersionExists() {
        try {
            Class.forName("com.viaversion.viaversion.ViaListener");
            Client.LOGGER.info("ViaVersion is available");
            return true;
        } catch (ClassNotFoundException e) {
            Client.LOGGER.info("ViaVersion is missing");
            return false;
        }
    }

}
