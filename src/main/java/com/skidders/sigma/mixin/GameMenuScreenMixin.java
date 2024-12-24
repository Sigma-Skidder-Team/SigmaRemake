package com.skidders.sigma.mixin;

import com.skidders.sigma.screens.InGameOptionsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("TAIL"))
    private void initWidgets(CallbackInfo ci) {
        this.addButton(new ButtonWidget(
                this.width / 2 - 102,
                this.height - 60,
                204,
                20,
                Text.of("Jello for Sigma Options"),
                button -> MinecraftClient.getInstance().openScreen(new InGameOptionsScreen("Jello Options"))
        ));
    }

}
