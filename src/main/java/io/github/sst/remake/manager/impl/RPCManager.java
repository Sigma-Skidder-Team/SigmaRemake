package io.github.sst.remake.manager.impl;

import dev.firstdark.rpc.DiscordRpc;
import dev.firstdark.rpc.enums.ErrorCode;
import dev.firstdark.rpc.exceptions.UnsupportedOsType;
import dev.firstdark.rpc.handlers.RPCEventHandler;
import dev.firstdark.rpc.models.DiscordRichPresence;
import dev.firstdark.rpc.models.User;
import io.github.sst.remake.Client;
import io.github.sst.remake.manager.Manager;

import static io.github.sst.remake.Client.LOGGER;

public class RPCManager extends Manager {
    private static final long TIMESTAMP = System.currentTimeMillis() / 1000L;
    private static final String STATE = "Playing Minecraft";
    private static final String DETAILS = "Jello for Fabric";
    private static final String LARGE_IMAGE_KEY = "jello";
    private static final String LARGE_IMAGE_DETAILS = "Sigma Remake " + Client.VERSION;

    private DiscordRpc rpc;

    @Override
    public void init() {
        this.rpc = new DiscordRpc();

        RPCEventHandler handler = new RPCEventHandler() {
            @Override
            public void ready(User user) {
                LOGGER.info("Initialised RPC for {}", user.getUsername());

                rpc.updatePresence(DiscordRichPresence.builder()
                        .startTimestamp(TIMESTAMP)
                        .largeImageKey(LARGE_IMAGE_KEY)
                        .largeImageText(LARGE_IMAGE_DETAILS)
                        .details(DETAILS)
                        .state(STATE).build());
            }

            @Override
            public void disconnected(ErrorCode errorCode, String message) {
                LOGGER.info("Disconnected RPC {} ({})", message, errorCode.name());
            }

            @Override
            public void errored(ErrorCode errorCode, String message) {
                LOGGER.info("RPC caught an error {} ({})", message, errorCode.name());
            }
        };

        try {
            rpc.init("693493612754763907", handler, false);
        } catch (UnsupportedOsType e) {
            LOGGER.error("Discord RPC failed to initialize", e);
        }
    }

    @Override
    public void shutdown() {
        if (this.rpc != null) {
            this.rpc.shutdown();
            this.rpc = null;
        }
    }
}