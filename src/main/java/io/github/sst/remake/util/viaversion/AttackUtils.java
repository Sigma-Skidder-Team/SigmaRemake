package io.github.sst.remake.util.viaversion;

import io.github.sst.remake.util.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;

@SuppressWarnings("DataFlowIssue")
public class AttackUtils implements IMinecraft {

    public static void attackEntity(Entity target, boolean swing) {
        if (FieldUtils.getProtocol().equals(ViaProtocols.R1_8_X)) {
            if (swing)
                client.player.swingHand(Hand.MAIN_HAND);
            client.interactionManager.attackEntity(client.player, target);
        } else {
            client.interactionManager.attackEntity(client.player, target);
            if (swing)
                client.player.swingHand(Hand.MAIN_HAND);
        }
    }

}
