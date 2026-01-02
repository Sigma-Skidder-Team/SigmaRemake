package io.github.sst.remake.manager.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.github.sst.remake.Client;
import io.github.sst.remake.alt.Account;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.util.client.ConfigUtils;
import io.github.sst.remake.util.io.GsonUtils;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AccountManager extends Manager {

    public final List<Account> accounts = new ArrayList<>();
    public Account currentAccount;

    @Override
    public void init() {
        File altsFile = new File(ConfigUtils.ALTS_FILE);
        if (altsFile.exists()) {
            try (FileReader reader = new FileReader(altsFile)) {
                JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
                if (json.has("alts") && json.get("alts").isJsonArray()) {
                    JsonArray alts = json.getAsJsonArray("alts");
                    for (JsonElement altElement : alts) {
                        Account account = Account.fromJson(altElement.toString());
                        if (account != null) {
                            this.accounts.add(account);
                        }
                    }
                }
            } catch (IOException | JsonParseException e) {
                Client.LOGGER.error("Failed to load alts", e);
            }
        }
    }

    public boolean login(Account account) {
        this.currentAccount = account;
        this.currentAccount.updateUsedCount();
        MinecraftClient.getInstance().session = this.currentAccount.toSession();

        return true;
    }

    public boolean has(Account account) {
        return this.accounts.contains(account);
    }

    public void add(Account account) {
        this.accounts.add(account);
    }

    public void remove(Account account) {
        this.accounts.remove(account);
    }

    @Override
    public void shutdown() {
        JsonArray jsonArray = new JsonArray();

        for (Account account : this.accounts) {
            jsonArray.add(new JsonParser().parse(account.toJson()).getAsJsonObject());
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("alts", jsonArray);

        try {
            GsonUtils.save(jsonObject, new File(ConfigUtils.ALTS_FILE));
        } catch (IOException | JsonParseException e) {
            Client.LOGGER.error("Failed to save alts", e);
        }
    }
}
