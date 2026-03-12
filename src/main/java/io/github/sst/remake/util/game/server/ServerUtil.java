package io.github.sst.remake.util.game.server;

import io.github.sst.remake.util.IMinecraft;

import java.net.SocketAddress;

public final class ServerUtil implements IMinecraft {
    private ServerUtil() {}

    public static boolean isHypixel() {
        try {
            if (client.getNetworkHandler() == null) return false;
            SocketAddress addr = client.getNetworkHandler().getConnection().getAddress();
            if (addr == null) return false;
            String s = addr.toString().toLowerCase();
            return s.contains("hypixel") || s.contains("mc.hypixel.net");
        } catch (Throwable t) {
            return false;
        }
    }
}
