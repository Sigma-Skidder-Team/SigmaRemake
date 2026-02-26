package io.github.sst.remake.tracker.impl;

import io.github.sst.remake.Client;
import io.github.sst.remake.data.alt.Account;
import io.github.sst.remake.data.alt.AccountBan;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.net.ReceivePacketEvent;
import io.github.sst.remake.tracker.Tracker;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.java.StringUtils;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public final class BanTracker extends Tracker implements IMinecraft {
    @Subscribe
    public void onPacket(ReceivePacketEvent event) {
        if (client.getCurrentServerEntry() == null) return;

        if (event.packet instanceof GameMessageS2CPacket) {
            if (checkBanMessage((GameMessageS2CPacket) event.packet)) {
                registerBan(System.currentTimeMillis()); //now
            }

            return;
        }

        if (event.packet instanceof DisconnectS2CPacket) {
            String reason = ((DisconnectS2CPacket) event.packet).getReason().getString();
            if (isBanMessage(reason)) {
                long until = parseBanUntil(reason);
                if (until == 0L) {
                    until = Long.MAX_VALUE;
                }
                registerBan(until);
            }

            return;
        }

        if (event.packet instanceof LoginDisconnectS2CPacket) {
            String reason = ((LoginDisconnectS2CPacket) event.packet).getReason().getString();
            if (isBanMessage(reason)) {
                long until = parseBanUntil(reason);
                if (until == 0L) {
                    until = Long.MAX_VALUE;
                }
                registerBan(until);
            }

            return;
        }

        if (event.packet instanceof LoginSuccessS2CPacket) {
            registerBan(System.currentTimeMillis()); //now
        }
    }

    private boolean checkBanMessage(GameMessageS2CPacket chat) {
        String raw = chat.getMessage().getString();
        if (raw == null) return false;

        return isBanMessage(raw);
    }

    @SuppressWarnings("DataFlowIssue")
    private void registerBan(long untilMs) {
        Account currentAccount = Client.INSTANCE.accountManager.currentAccount;
        if (currentAccount == null) return;

        String address = client.getCurrentServerEntry().address;

        AccountBan existingBan = currentAccount.getBanInfo(address);
        AccountBan newBan = new AccountBan(address, new Date(untilMs));
        if (existingBan == null) {
            existingBan = newBan;
            currentAccount.bans.add(newBan);
        } else {
            currentAccount.bans.remove(existingBan);
            currentAccount.bans.add(newBan);
        }

        Client.INSTANCE.configManager.saveAlts();
    }

    private long parseBanUntil(String message) {
        message = message.toLowerCase();

        if (message.contains("security") && message.contains("alert")) return Long.MAX_VALUE - 1;
        if (message.contains("permanent")
                || message.contains("your account has been suspended from")
                || message.contains("tu cuenta ha sido suspendida. al reconectarte, tendr")
                || message.contains("gebannt")) return Long.MAX_VALUE;
        if (message.contains("compromised")) return Long.MAX_VALUE - 1;

        long days = TimeUnit.DAYS.toMillis(StringUtils.extractDays(message));
        long hours = TimeUnit.HOURS.toMillis(StringUtils.extractHours(message));
        long minutes = TimeUnit.MINUTES.toMillis(StringUtils.extractMinutes(message));
        long seconds = TimeUnit.SECONDS.toMillis(StringUtils.extractSeconds(message));

        if (message.contains("vous avez été banni")
                && days == 0 && hours == 0 && minutes == 0 && seconds == 0) {
            return Long.MAX_VALUE;
        }

        if (days == 0 && hours == 0 && minutes == 0 && seconds == 0) {
            return 0L;
        }

        return System.currentTimeMillis() + days + hours + minutes + seconds;
    }

    private void clearBanForCurrentServer() {
        Account currentAccount = Client.INSTANCE.accountManager.currentAccount;
        if (currentAccount == null) return;
        if (client.getCurrentServerEntry() == null) return;

        String address = client.getCurrentServerEntry().address;

        AccountBan existingBan = currentAccount.getBanInfo(address);
        if (existingBan != null) {
            currentAccount.bans.remove(existingBan);
            Client.INSTANCE.configManager.saveAlts();
        }
    }

    private String normalize(String s) {
        String out = s.trim().toLowerCase();
        out = out.replaceAll("\\s+", " ");
        out = out.replaceAll("[.]+$", ""); // remove trailing periods (one or more)
        return out;
    }

    private boolean isBanMessage(String raw) {
        if (raw == null) return false;

        String msg = normalize(raw);

        List<String> commonBanMessages = Arrays.asList(
                normalize("You are permanently banned from MinemenClub."),
                normalize("Your connection to the server leu-practice has been prevented due to you being associated to a blacklisted player."),
                normalize("You are blacklisted from MinemenClub."),
                normalize("You are banned from this server")
        );

        if (commonBanMessages.contains(msg)) {
            return true;
        }

        boolean keyword =
                msg.contains(normalize("you are banned from this server"))
                        || msg.contains(normalize("banned"))
                        || msg.contains(normalize("blacklisted"))
                        || msg.contains(normalize("suspended"))
                        || msg.contains(normalize("security alert"))
                        || msg.contains(normalize("compromised"))
                        || msg.contains(normalize("vous avez été banni"))
                        || msg.contains(normalize("gebannt"));

        return keyword;
    }
}
