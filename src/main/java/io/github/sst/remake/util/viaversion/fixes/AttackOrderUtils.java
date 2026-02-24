package io.github.sst.remake.util.viaversion.fixes;

import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.viaversion.ViaInstance;
import io.github.sst.remake.util.viaversion.ViaProtocols;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

@SuppressWarnings("DataFlowIssue")
public class AttackOrderUtils implements IMinecraft {
    public static void sendConditionalSwing(BlockHitResult ray, Hand enumHand) {
        if (ray != null && ray.getType() != BlockHitResult.Type.ENTITY) client.player.swingHand(enumHand);
    }

    public static void sendFixedAttack(Entity target, Hand hand) {
        if (ViaInstance.getTargetVersion().olderThanOrEqualTo(ViaProtocols.R1_8_X)) {
            client.player.swingHand(hand);
            client.interactionManager.attackEntity(client.player, target);
        } else {
            client.interactionManager.attackEntity(client.player, target);
            client.player.swingHand(hand);
        }
    }
}