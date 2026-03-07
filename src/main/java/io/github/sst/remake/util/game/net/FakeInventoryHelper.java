package io.github.sst.remake.util.game.net;

import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;

/**
 * Utility to access the custom OPEN_INVENTORY enum constant added to {@link ClientStatusC2SPacket.Mode}.
 * This constant is injected via {@link io.github.sst.remake.mixin.MixinClientStatusC2SPacketMode}.
 *
 * <p>Some anticheats (primarily on older server versions <= 1.11.1) expect the client to send
 * an OPEN_INVENTORY status packet before performing inventory operations outside the inventory screen.
 * Sending this packet tricks the server into thinking the player has their inventory open.</p>
 */
public final class FakeInventoryHelper {

    private static ClientStatusC2SPacket.Mode OPEN_INVENTORY;

    private FakeInventoryHelper() {}

    /**
     * Gets the OPEN_INVENTORY mode constant (ordinal 2).
     * This is lazily resolved from the enum values to ensure the mixin has already run.
     */
    public static ClientStatusC2SPacket.Mode getOpenInventoryMode() {
        if (OPEN_INVENTORY == null) {
            for (ClientStatusC2SPacket.Mode mode : ClientStatusC2SPacket.Mode.values()) {
                if (mode.name().equals("OPEN_INVENTORY")) {
                    OPEN_INVENTORY = mode;
                    break;
                }
            }
        }
        return OPEN_INVENTORY;
    }

    /**
     * Creates a ClientStatusC2SPacket with the OPEN_INVENTORY mode.
     */
    public static ClientStatusC2SPacket createOpenInventoryPacket() {
        return new ClientStatusC2SPacket(getOpenInventoryMode());
    }
}
