package io.github.sst.remake.gui.element.impl.alts;

import io.github.sst.remake.alt.Account;
import io.github.sst.remake.alt.AccountBan;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.panel.AnimatedIconPanel;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccountElement extends AnimatedIconPanel {
    private Account currentAccount = null;
    private final List<BanElement> bans = new ArrayList<>();
    private float field20815 = 0.0F;

    public AccountElement(CustomGuiScreen parent, String iconName, int x, int y, int width, int height) {
        super(parent, iconName, x, y, width, height, false);
    }

    public void handleSelectedAccount(Account account) {
        this.currentAccount = account;

        for (BanElement ban : this.bans) {
            this.method13234(ban);
        }

        if (account != null) {
            List<AccountBan> accountBans = new ArrayList<>(account.bans);
            Collections.reverse(accountBans);

            int index = 0;
            int height = 90;
            int hPadding = 14;

            for (AccountBan ban : accountBans) {
                if (ban.getServer() != null && ban.getServer().getFavicon() != null) {
                    BanElement el = new BanElement(
                            this, accountBans.get(index).address,
                            40, 100 + index * (height + hPadding),
                            this.width - 90, height,
                            ban
                    );
                    this.addToList(el);
                    this.bans.add(el);
                    index++;
                }
            }

            this.setHeight(index * (height + hPadding) + 116);
        }
    }

    @Override
    public void draw(float partialTicks) {
        this.method13225();
        this.field20815 = (float) ((double) this.field20815 + (this.isSelfVisible() ? 0.33 : -0.33));
        this.field20815 = Math.min(1.0F, Math.max(0.0F, this.field20815));

        if (this.currentAccount == null) {
            int var4 = this.width - 30;
            int var5 = this.x + 5;
            RenderUtils.drawImage(
                    (float) var5,
                    (float) ((MinecraftClient.getInstance().getWindow().getHeight() - var4 * 342 / 460) / 2 - 60),
                    (float) var4,
                    (float) (var4 * 342 / 460),
                    Resources.imgPNG
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
