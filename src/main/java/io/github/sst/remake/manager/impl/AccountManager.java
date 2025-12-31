package io.github.sst.remake.manager.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.github.sst.remake.Client;
import io.github.sst.remake.alt.Account;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.util.client.ConfigUtils;
import io.github.sst.remake.util.io.FileUtils;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AccountManager extends Manager {

    public final List<Account> accounts = new ArrayList<>();
    public Account currentAccount;

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
            jsonArray.add(JsonParser.parseString(account.toJson()).getAsJsonObject());
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("alts", jsonArray);

        try {
            FileUtils.save(jsonObject, new File(ConfigUtils.ALTS_FILE));
        } catch (IOException | JsonParseException e) {
            Client.LOGGER.error(e.getMessage());
        }
    }
}
