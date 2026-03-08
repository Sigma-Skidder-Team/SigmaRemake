package io.github.sst.remake.util.game.player;

import io.github.sst.remake.util.IMinecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.util.Formatting;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class PlayerUtils implements IMinecraft {
    /**
     * Gets the team color for a player, or white (0xFFFFFF) if not on a team.
     */
    public static int getTeamColor(PlayerEntity player) {
        AbstractTeam team = player.getScoreboardTeam();
        if (team != null) {
            Formatting formatting = team.getColor();
            if (formatting.getColorValue() != null) {
                return formatting.getColorValue();
            }
        }
        return 0xFFFFFF;
    }

    /**
     * Resolves a UUID to a player name using the Mojang sessionserver API.
     */
    public static String resolvePlayerName(UUID uuid) {
        try {
            String uuidStr = uuid.toString().replace("-", "");
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuidStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() == 200) {
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                StringBuilder sb = new StringBuilder();
                int ch;
                while ((ch = reader.read()) != -1) {
                    sb.append((char) ch);
                }
                reader.close();

                // Simple JSON parsing — extract "name" field
                String json = sb.toString();
                int nameIndex = json.indexOf("\"name\"");
                if (nameIndex != -1) {
                    int colonIndex = json.indexOf(":", nameIndex);
                    int quoteStart = json.indexOf("\"", colonIndex + 1);
                    int quoteEnd = json.indexOf("\"", quoteStart + 1);
                    if (quoteStart != -1 && quoteEnd != -1) {
                        return json.substring(quoteStart + 1, quoteEnd);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
