package io.github.sst.remake.util.system.io.audio;

import io.github.sst.remake.Client;
import io.github.sst.remake.util.render.image.Resources;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SoundUtils {
    private static final String fileType = ".mp3";
    private static final List<String> VALID_SOUNDS = new ArrayList<>(
            Arrays.asList("activate", "deactivate", "click", "error", "pop", "connect", "switch", "clicksound")
    );

    public static void play(String url) {
        if (!VALID_SOUNDS.contains(url)) {
            Client.LOGGER.warn("Invalid audio file attempted to be played: {}", url);
        } else {
            try {
                InputStream audioStream = Resources.readInputStream("audio/" + url + fileType);

                AdvancedPlayer player = new AdvancedPlayer(audioStream);
                new Thread(() -> {
                    try {
                        player.play();
                    } catch (JavaLayerException e) {
                        Client.LOGGER.error("Error playing audio file: {}", url, e);
                    }
                }).start();
            } catch (JavaLayerException e) {
                Client.LOGGER.error("Unsupported audio file: {}", url, e);
            }
        }
    }
}