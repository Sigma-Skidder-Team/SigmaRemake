package io.github.sst.remake.module.impl.render;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.ClientPlayerTickEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

public class FullBrightModule extends Module {
    private float currentGamma = 1.0f;

    public FullBrightModule() {
        super("FullBright", "Helps you see in the dark.", Category.RENDER);
    }

    @Override
    public void onDisable() {
        client.options.gamma = 1.0;
        currentGamma = 1.0f;
    }

    @Subscribe
    public void onTick(ClientPlayerTickEvent event) {
        client.options.gamma = 999.0;

        if (client.world != null) {
            int lightAdjustment = 16;

            BlockPos playerPos = new BlockPos(
                    client.player.getX(),
                    client.player.getY(),
                    client.player.getZ()
            ).up();
            WorldChunk currentChunk = client.world.getChunk(
                    playerPos.getX() >> 4,
                    playerPos.getZ() >> 4
            );

            if (currentChunk != null && playerPos.getY() >= 0 && playerPos.getY() < 256) {
                lightAdjustment -= client.world.getLightLevel(playerPos);
            }

            currentGamma += (lightAdjustment - currentGamma) * 0.2f;

            if (currentGamma >= 1.5F) {
                client.options.gamma = Math.min(Math.max(1.0F, currentGamma), 10.0f);
            }
        }
    }
}