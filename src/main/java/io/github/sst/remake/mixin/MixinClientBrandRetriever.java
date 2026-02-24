package io.github.sst.remake.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientBrandRetriever.class)
public class MixinClientBrandRetriever {
    @ModifyReturnValue(method = "getClientModName", at = @At("RETURN"))
    private static String modifyGetClientModName(String original) {
        return "vanilla";
    }
}