package io.github.sst.remake.mixin;

import io.github.sst.remake.Client;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.viaversion.ViaInstance;
import io.github.sst.remake.util.viaversion.ViaProtocols;
import io.github.sst.remake.util.viaversion.version.ClientSideVersionUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.DoubleOption;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(MultiplayerScreen.class)
public abstract class MixinMultiplayerScreen extends Screen {
    @Unique
    private ClickableWidget versionSelectorWidget;

    protected MixinMultiplayerScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void injectInit(CallbackInfo ci) {
        if (!ViaInstance.VIAVERSION_EXISTS) return;

        DoubleOption versionSelector = new DoubleOption(
                "jello.portaloption",
                0,
                ViaProtocols.values().length - 1,
                1,
                (options) -> (double) ClientSideVersionUtils.getProtocol().ordinal(),
                Client.INSTANCE.viaManager::onVersionChange,
                (options, option) -> {
                    int index = (int) option.get(options);
                    return new LiteralText(ViaProtocols.getByIndex(index).name);
                }
        );
        versionSelectorWidget = addButton(versionSelector.createButton(client.options, width / 2 + 40, 7, 114));
    }

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;drawCenteredText(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V"))
    private void modifyRender(Args args) {
        args.set(3, this.width / 2 - 107);
        args.set(4, 13);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void injectRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!ViaInstance.VIAVERSION_EXISTS) return;

        versionSelectorWidget.render(matrices, mouseX, mouseY, delta);
        drawStringWithShadow(matrices, textRenderer, "Jello Portal:", width / 2 - 30, 13, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F));
    }
}