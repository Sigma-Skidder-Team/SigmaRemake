package io.github.sst.remake.manager.impl;

import io.github.sst.remake.Client;
import io.github.sst.remake.alt.Account;
import io.github.sst.remake.manager.Manager;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class AccountManager extends Manager {

    public final List<Account> accounts = new ArrayList<>();
    public Account currentAccount;

    @Override
    public void init() {
        Client.INSTANCE.configManager.loadAlts();
    }

    @Override
    public void shutdown() {
        Client.INSTANCE.configManager.saveAlts();
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
}
