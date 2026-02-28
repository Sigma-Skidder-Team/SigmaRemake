package io.github.sst.remake.manager.impl;

import io.github.sst.remake.data.alt.Account;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.tracker.impl.BanTracker;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public final class AccountManager extends Manager {
    public List<Account> accounts;
    public Account currentAccount;

    @Override
    public void init() {
        accounts = new ArrayList<>();
        new BanTracker().enable();
    }

    public boolean login(Account account) {
        this.currentAccount = account;
        this.currentAccount.updateUsedCount();
        MinecraftClient.getInstance().session = this.currentAccount.toSession();

        return true;
    }

    public boolean has(Account account) {
        return this.accounts.stream().anyMatch(existing -> isSameAccount(existing, account));
    }

    public void add(Account account) {
        if (account == null || has(account)) {
            return;
        }
        this.accounts.add(account);
    }

    public void remove(Account account) {
        this.accounts.remove(account);
    }

    public boolean isSameAccount(Account first, Account second) {
        if (first == second) return true;
        if (first == null || second == null) return false;

        String firstUuid = normalized(first.uuid);
        String secondUuid = normalized(second.uuid);
        boolean firstCracked = firstUuid.equals(normalized(Account.STEVE_UUID));
        boolean secondCracked = secondUuid.equals(normalized(Account.STEVE_UUID));

        if (!firstUuid.isEmpty() && !secondUuid.isEmpty() && !firstCracked && !secondCracked) {
            return firstUuid.equals(secondUuid);
        }

        String firstName = normalized(first.name);
        String secondName = normalized(second.name);
        if (!firstName.isEmpty() && !secondName.isEmpty()) {
            return firstName.equals(secondName);
        }

        String firstToken = normalized(first.token);
        String secondToken = normalized(second.token);
        return !firstToken.isEmpty() && firstToken.equals(secondToken);
    }

    private static String normalized(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
