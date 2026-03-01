package io.github.sst.remake.manager.impl;

import io.github.sst.remake.Client;
import io.github.sst.remake.data.alt.Account;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.tracker.impl.BanTracker;
import io.github.sst.remake.util.http.cookie.CookieLoginUtils;
import io.github.sst.remake.util.http.ms.MicrosoftLoginUtils;
import io.github.sst.remake.util.http.token.TokenLoginUtils;
import io.github.sst.remake.util.http.token.TokenVerifyUtils;
import io.github.sst.remake.util.system.io.FileUtils;
import io.github.sst.remake.util.system.io.audio.SoundUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public void processCrackedLogin(String username) {
        if (username == null || username.isEmpty()) {
            return;
        }

        Account account = new Account(username, "0", Account.STEVE_UUID);
        if (!has(account)) {
            add(account);
            Client.INSTANCE.configManager.saveAlts();
        }
    }

    public boolean processCookieLogin() {
        File file = FileUtils.openTxtFile();
        if (file == null) {
            return false;
        }

        try {
            CookieLoginUtils.LoginData session = CookieLoginUtils.loginWithCookie(file);
            if (session == null) {
                SoundUtils.play("error");
                return false;
            }

            Account account = new Account(session.username, session.playerID, session.token);
            if (!has(account)) {
                add(account);
                Client.INSTANCE.configManager.saveAlts();
            }
            return true;
        } catch (Exception e) {
            SoundUtils.play("error");
            return false;
        }
    }

    public boolean processTokenLogin(String token) {
        try {
            TokenVerifyUtils.AuthResult result = TokenVerifyUtils.authenticate(token);
            if (result instanceof TokenVerifyUtils.AuthResult.Failure) {
                SoundUtils.play("error");
                return false;
            }

            Session session = TokenLoginUtils.setSession(token);
            if (session == null) {
                SoundUtils.play("error");
                return false;
            }

            Account account = new Account(session.getUsername(), token, session.getUuid());

            if (!has(account)) {
                add(account);
                Client.INSTANCE.configManager.saveAlts();
            }

            return true;
        } catch (Exception e) {
            SoundUtils.play("error");
            return false;
        }
    }

    public void processWebLogin(Runnable onComplete) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        MicrosoftLoginUtils.acquireMSAuthCode(executor)
                .thenComposeAsync(code -> MicrosoftLoginUtils.acquireMSAccessToken(code, executor), executor)
                .thenComposeAsync(access -> MicrosoftLoginUtils.acquireXboxAccessToken(access, executor), executor)
                .thenComposeAsync(xbox -> MicrosoftLoginUtils.acquireXboxXstsToken(xbox, executor), executor)
                .thenComposeAsync(xsts -> MicrosoftLoginUtils.acquireMCAccessToken(xsts.get("Token"), xsts.get("uhs"), executor), executor)
                .thenComposeAsync(mc -> MicrosoftLoginUtils.login(mc, executor), executor)
                .thenAccept(session -> {
                    try {
                        Account account = new Account(session.getUsername(), session.getAccessToken(), session.getUuid());
                        if (!has(account)) {
                            add(account);
                            Client.INSTANCE.configManager.saveAlts();
                        }
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    } finally {
                        executor.shutdown();
                    }
                })
                .exceptionally(err -> {
                    try {
                        Client.LOGGER.error("Auth failed", err);
                        SoundUtils.play("error");
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    } finally {
                        executor.shutdown();
                    }
                    return null;
                });
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
