package io.github.sst.remake.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public interface IMinecraft {

    MinecraftClient client = MinecraftClient.getInstance();


    static void addChatMessage(String text) {
        client.inGameHud.getChatHud().addMessage(Text.of(text));
    }

    static void sendChatMessage(String text) {
        final var nh = client.getNetworkHandler();
        if (nh == null) return;
        nh.sendChatMessage(text);
    }

}
