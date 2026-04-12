package io.github.sst.remake.module.impl.combat;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.ClientPlayerTickEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import net.minecraft.item.BowItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@SuppressWarnings("DataFlowIssue")
public class FastBowModule extends Module {
    public FastBowModule() {
        super("FastBow", "Allows you to use your bow faster.", Category.COMBAT);
    }

    @Subscribe
    public void onTick(ClientPlayerTickEvent event) {
        if (client.player.getMainHandStack() == null) return;
        if (!(client.player.getMainHandStack().getItem() instanceof BowItem)) return;
        if (!client.player.isOnGround() || !client.player.isUsingItem()) return;

        for (int i = 0; i < 25; i++) {
            client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
        }

        client.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
    }
}