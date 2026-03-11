package io.github.sst.remake.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestions;
import io.github.sst.remake.Client;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestor.class)
public abstract class MixinCommandSuggestor {
    @Shadow
    private ParseResults<CommandSource> parse;

    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    private CommandSuggestor.SuggestionWindow window;

    @Shadow
    private boolean completingSuggestions;

    @Shadow
    @Final
    private List<OrderedText> messages;

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Final
    private TextFieldWidget textField;

    @Invoker("show")
    protected abstract void invokeShow();

    @Inject(method = "refresh", at = @At("HEAD"), cancellable = true)
    private void injectRefresh(CallbackInfo ci) {
        String input = this.textField.getText();
        if (!Client.INSTANCE.commandManager.isClientCommand(input)) {
            return;
        }

        if (this.client.player == null || this.client.player.networkHandler == null) {
            return;
        }

        if (this.parse != null && !this.parse.getReader().getString().equals(input)) {
            this.parse = null;
        }

        if (!this.completingSuggestions) {
            this.textField.setSuggestion(null);
            this.window = null;
        }

        this.messages.clear();

        if (this.parse == null) {
            this.parse = Client.INSTANCE.commandManager.getDispatcher()
                    .parse(input, this.client.player.networkHandler.getCommandSource());
        }

        int cursor = this.textField.getCursor();
        if (cursor >= 1 && (this.window == null || !this.completingSuggestions)) {
            this.pendingSuggestions = Client.INSTANCE.commandManager.getDispatcher()
                    .getCompletionSuggestions(this.parse, cursor);
            this.pendingSuggestions.thenRun(() -> {
                if (this.pendingSuggestions != null && this.pendingSuggestions.isDone()) {
                    this.invokeShow();
                }
            });
        }

        ci.cancel();
    }
}