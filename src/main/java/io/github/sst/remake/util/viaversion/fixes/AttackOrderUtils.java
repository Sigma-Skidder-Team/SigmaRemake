package io.github.sst.remake.util.viaversion.fixes;

import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.viaversion.ViaInstance;
import io.github.sst.remake.util.viaversion.ViaProtocols;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

@SuppressWarnings("DataFlowIssue")
public class AttackOrderUtils implements IMinecraft {
    public static void sendConditionalSwing(HitResult ray, Hand enumHand) {
        if (ray != null && ray.getType() != HitResult.Type.ENTITY) client.player.swingHand(enumHand);
    }

    public static void sendFixedAttack(PlayerEntity player, Entity target, Hand hand) {
        if (ViaInstance.getTargetVersion().olderThanOrEqualTo(ViaProtocols.R1_8_X)) {
            client.player.swingHand(hand);
            client.interactionManager.attackEntity(player, target);
        } else {
            client.interactionManager.attackEntity(player, target);
            client.player.swingHand(hand);
        }
    }
}