package io.github.sst.remake.alt;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;

import java.util.Date;

public class AccountBan {
    public final String address;
    public final Date date;

    public AccountBan(String address, Date date) {
        this.address = address;
        this.date = date;
    }

    public ServerInfo getServer() {
        ServerList list = new ServerList(MinecraftClient.getInstance());
        list.loadFile();
        int count = list.size();

        for (int i = 0; i < count; i++) {
            ServerInfo info = list.get(i);
            if (info.address.equals(this.address)) {
                return info;
            }
        }

        return null;
    }
}
