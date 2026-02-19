package io.github.sst.remake.gui.screen.altmanager;

import io.github.sst.remake.alt.Account;
import io.github.sst.remake.alt.AccountBan;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccountDetailPanel extends Widget {
    private Account currentAccount = null;
    private final List<BanEntry> banEntries = new ArrayList<>();
    private float visibilityFade = 0.0F;

    public AccountDetailPanel(GuiComponent var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6, false);
    }

    public void handleSelectedAccount(Account account) {
        this.currentAccount = account;

        for (BanEntry ban : this.banEntries) {
            this.queueChildRemoval(ban);
        }

        if (account != null) {
            List<AccountBan> accountBans = new ArrayList<>(account.bans);
            Collections.reverse(accountBans);

            int index = 0;
            int var14 = 90;
            int var7 = 14;

            for (AccountBan var9 : accountBans) {
                if (var9.getServer() != null && var9.getServer().getIcon() != null) {
                    BanEntry var10 = new BanEntry(
                            this, accountBans.get(index).address, 40, 100 + index * (var14 + var7), this.width - 90, var14, var9
                    );
                    this.addToList(var10);
                    this.banEntries.add(var10);
                    index++;
                }
            }

            this.setHeight(index * (var14 + var7) + 116);
        }
    }

    @Override
    public void draw(float partialTicks) {
        this.applyTranslationTransforms();
        this.visibilityFade = (float) ((double) this.visibilityFade + (this.isSelfVisible() ? 0.33 : -0.33));
        this.visibilityFade = Math.min(1.0F, Math.max(0.0F, this.visibilityFade));

        if (this.currentAccount == null) {
            int var4 = this.width - 30;
            int var5 = this.x + 5;
            RenderUtils.drawImage(
                    (float) var5,
                    (float) ((MinecraftClient.getInstance().getWindow().getHeight() - var4 * 342 / 460) / 2 - 60),
                    (float) var4,
                    (float) (var4 * 342 / 460),
                    Resources.INFORMATION
            );
            return;
        }

        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_36,
                (float) (this.x + (this.width - FontUtils.HELVETICA_LIGHT_36.getWidth(this.currentAccount.name)) / 2),
                (float) this.y - 20,
                this.currentAccount.name,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.7F)
        );
        super.draw(partialTicks);
    }
}
