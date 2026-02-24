package io.github.sst.remake.util.viaversion;

import io.github.sst.remake.Client;
import io.github.sst.remake.util.viaversion.version.ClientSideVersionUtils;

public class ViaInstance {
    public static boolean VIAVERSION_EXISTS = viaFabricExists() && viaVersionExists();

    public static void init() {
        ClientSideVersionUtils.init();
    }

    public static ViaProtocols getTargetVersion() {
        return ClientSideVersionUtils.getProtocol();
    }

    public static void setTargetVersion(ViaProtocols protocol) {
        ClientSideVersionUtils.setClientSideVersion(protocol.protocol);
    }

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