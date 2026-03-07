package io.github.sst.remake.mixin;

import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;

@Mixin(ClientStatusC2SPacket.Mode.class)
public class MixinClientStatusC2SPacketMode {

    @SuppressWarnings("InvokerTarget")
    @Invoker("<init>")
    private static ClientStatusC2SPacket.Mode createMode(String name, int ordinal) {
        throw new AssertionError();
    }

    @Shadow
    @Final
    @Mutable
    private static ClientStatusC2SPacket.Mode[] field_12776;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void addOpenInventory(CallbackInfo ci) {
        ArrayList<ClientStatusC2SPacket.Mode> modes = new ArrayList<>(Arrays.asList(field_12776));

        ClientStatusC2SPacket.Mode openInventory = createMode("OPEN_INVENTORY", modes.size());
        modes.add(openInventory);

        field_12776 = modes.toArray(new ClientStatusC2SPacket.Mode[0]);
    }
}
