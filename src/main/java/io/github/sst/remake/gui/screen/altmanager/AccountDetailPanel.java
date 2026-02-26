package io.github.sst.remake.gui.screen.altmanager;

import io.github.sst.remake.data.alt.Account;
import io.github.sst.remake.data.alt.AccountBan;
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
    private Account selectedAccount = null;
    private final List<BanEntry> banEntries = new ArrayList<>();
    private float visibilityFade = 0.0F;

    public AccountDetailPanel(GuiComponent parent, String text, int x, int y, int width, int height) {
        super(parent, text, x, y, width, height, false);
    }

    public void handleSelectedAccount(Account account) {
        this.selectedAccount = account;

        for (BanEntry banEntry : this.banEntries) {
            this.queueChildRemoval(banEntry);
        }
        this.banEntries.clear();

        if (account != null) {
            List<AccountBan> accountBans = new ArrayList<>(account.bans);
            Collections.reverse(accountBans);

            int visibleBanCount = 0;
            int entryHeight = 90;
            int entrySpacing = 14;

            for (AccountBan ban : accountBans) {
                BanEntry entry = new BanEntry(
                        this,
                        accountBans.get(visibleBanCount).address,
                        40,
                        100 + visibleBanCount * (entryHeight + entrySpacing),
                        this.width - 90,
                        entryHeight,
                        ban
                );
                this.addToList(entry);
                this.banEntries.add(entry);
                visibleBanCount++;
            }

            this.setHeight(visibleBanCount * (entryHeight + entrySpacing) + 116);
        }
    }

    @Override
    public void draw(float partialTicks) {
        this.applyTranslationTransforms();

        this.visibilityFade = (float) ((double) this.visibilityFade + (this.isSelfVisible() ? 0.33 : -0.33));
        this.visibilityFade = Math.min(1.0F, Math.max(0.0F, this.visibilityFade));

        if (this.selectedAccount == null) {
            int imageWidth = this.width - 30;
            int imageX = this.x + 5;

            RenderUtils.drawImage(
                    (float) imageX,
                    (float) ((MinecraftClient.getInstance().getWindow().getHeight() - imageWidth * 342 / 460) / 2 - 60),
                    (float) imageWidth,
                    (float) (imageWidth * 342 / 460),
                    Resources.INFORMATION
            );
            return;
        }

        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_36,
                (float) (this.x + (this.width - FontUtils.HELVETICA_LIGHT_36.getWidth(this.selectedAccount.name)) / 2),
                (float) this.y - 20,
                this.selectedAccount.name,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.7F)
        );

        super.draw(partialTicks);
    }
}
