package io.github.sst.remake.data.alt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.sst.remake.util.http.SkinUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;
import org.newdawn.slick.opengl.texture.Texture;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Account {
    public static final String STEVE_UUID = "c06f8906-4c8a-4911-9c29-ea1dbd1aab82";
    private static final Gson GSON = new GsonBuilder().create();

    public final String name;
    public String uuid;
    public final String token;

    public long lastUsed;
    public long dateAdded;
    public int useCount;

    public final List<AccountBan> bans = new ArrayList<>();

    private transient Thread updateThread;
    private transient Thread headUpdateThread;
    public transient BufferedImage skin;
    public transient Texture head;

    public Account(String name, String token, String uuid) {
        this.name = name;
        this.dateAdded = System.currentTimeMillis();
        this.lastUsed = 0L;
        this.useCount = 0;
        this.token = token;
        this.uuid = uuid;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public static Account fromJson(String json) {
        return GSON.fromJson(json, Account.class);
    }

    public void updateUsedCount() {
        this.useCount++;
    }

    public String getUUID() {
        return this.uuid;
    }

    public String getFormattedUUID() {
        return this.uuid.replaceAll("-", "");
    }

    public void removeBanByIp(String serverIP) {
        this.bans.removeIf(accountBan -> accountBan.address.equals(serverIP));
    }

    public Texture setHeadTexture() {
        if (this.head == null && this.headUpdateThread == null) {
            this.headUpdateThread = new Thread(() -> {
                byte[] data = SkinUtils.getHeadBytes(getFormattedUUID(), 75);
                if (data == null) {
                    this.headUpdateThread = null;
                    return;
                }
                MinecraftClient.getInstance().execute(() -> {
                    if (this.head == null) {
                        this.head = SkinUtils.loadHeadTexture(data);
                    }
                    this.headUpdateThread = null;
                });
            }, "AltHeadLoader-" + this.name);
            this.headUpdateThread.setDaemon(true);
            this.headUpdateThread.start();
        }

        return this.head != null ? this.head : Resources.STEVE_HEAD;
    }

    public void updateSkin() {
        if (!this.getUUID().contains(STEVE_UUID) && this.updateThread == null) {
            this.updateThread = new Thread(() -> this.skin = SkinUtils.getSkin(getFormattedUUID()));
            this.updateThread.start();
        }
    }

    public AccountBan getBanInfo(String address) {
        for (AccountBan ban : this.bans) {
            if (ban.address.equals(address)) {
                return ban;
            }
        }

        return null;
    }

    public Session toSession() {
        return new Session(this.name, this.uuid, this.token, Session.AccountType.LEGACY.name());
    }
}
