package io.github.sst.remake.manager.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.sst.remake.Client;
import io.github.sst.remake.alt.Account;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.util.io.FileUtils;
import io.github.sst.remake.util.io.audio.SoundUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AccountManager extends Manager {

    public final List<Account> accounts = new ArrayList<>();

    public Account currentAccount;

    public boolean has(Account account) {
        return this.accounts.contains(account);
    }

    public void add(Account account) {
        this.accounts.add(account);
        SoundUtils.play("connect");
    }

    public void remove(Account account) {
        this.accounts.remove(account);
    }

    public boolean login(Account account) {
        account.updateUsedCount();

        this.currentAccount = account;
        return true;
    }

    @Override
    public void init() {
        add(new Account("Steve", "0", Account.STEVE_UUID));
    }

    @Override
    public void shutdown() {
        JsonArray jsonArray = new JsonArray();

        for (Account account : this.accounts) {
            jsonArray.add(
                    com.google.gson.JsonParser.parseString(account.toJson()).getAsJsonObject()
            );
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("alts", jsonArray);

        try {
            FileUtils.save(jsonObject, new File(Client.INSTANCE.configManager.file + "/alts.json"));
        } catch (IOException | JsonParseException e) {
            Client.LOGGER.error(e.getMessage());
        }
    }
}
