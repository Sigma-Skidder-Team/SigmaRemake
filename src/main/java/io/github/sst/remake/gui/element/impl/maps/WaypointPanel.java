package io.github.sst.remake.gui.element.impl.maps;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.Element;
import io.github.sst.remake.gui.element.impl.Button;
import io.github.sst.remake.gui.element.impl.TextButton;
import io.github.sst.remake.gui.element.impl.TextField;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.EasingFunctions;
import io.github.sst.remake.util.math.anim.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WaypointPanel extends Element {
    private final List<Button> field20723 = new ArrayList<Button>();
    private final Date field20724;
    private boolean field20725 = false;
    private Date field20726;
    public Vec3i field20727;
    public TextField field20728;
    public TextField field20729;
    public BadgeSelect field20730;
    private final List<Class9073> field20731 = new ArrayList<>();

    public WaypointPanel(CustomGuiScreen var1, String var2, int var3, int var4, Vec3i var5) {
        super(var1, var2, var3 - 107, var4 + 10, 214, 170, ColorHelper.DEFAULT_COLOR, "", false);
        this.field20727 = var5;
        if (this.y + this.height <= MinecraftClient.getInstance().getWindow().getHeight()) {
            this.y += 10;
        } else {
            this.y = this.y - (this.height + 27);
            this.field20725 = true;
        }

        this.field20724 = new Date();
        this.setReAddChildren(true);
        this.setListening(false);
        TextButton var8;
        this.addToList(
                var8 = new TextButton(
                        this,
                        "addButton",
                        this.width - 66,
                        this.height - 60,
                        FontUtils.HELVETICA_LIGHT_25.getWidth("Add"),
                        50,
                        ColorHelper.DEFAULT_COLOR,
                        "Add",
                        FontUtils.HELVETICA_LIGHT_25
                )
        );
        var8.onClick((var1x, var2x) -> this.method13132(this.field20729.getText(), this.method13130(), this.field20730.field21296));
        this.addToList(this.field20729 = new TextField(this, "Name", 20, 7, this.width - 40, 60, TextField.field20741, "My waypoint", "My waypoint"));
        this.field20729.startFocus();
        this.field20729.setRoundedThingy(false);
        this.addToList(this.field20730 = new BadgeSelect(this, "badgeSelect", 0, 86));
        this.addToList(
                this.field20728 = new TextField(
                        this,
                        "Coords",
                        20,
                        this.height - 44,
                        this.width - 100,
                        20,
                        TextField.field20741,
                        var5.getX() + " " + var5.getZ(),
                        var5.getX() + " " + var5.getZ()
                )
        );
        this.field20728.setRoundedThingy(false);
        this.field20728.setFont(FontUtils.HELVETICA_LIGHT_18);
    }

    public Vec3i method13130() {
        if (this.field20728.getText() != null && this.field20728.getText().contains(" ")) {
            String[] var3 = this.field20728.getText().split(" ");
            if (var3.length == 2 && var3[0].matches("-?\\d+") && var3[1].matches("-?\\d+")) {
                int var4 = Integer.valueOf(var3[0]);
                int var5 = Integer.valueOf(var3[1]);
                return new Vec3i(var4, 0, var5);
            }
        }

        return this.field20727;
    }

    @Override
    public void draw(float partialTicks) {
        partialTicks = AnimationUtils.calculateProgressWithReverse(this.field20724, this.field20726, 250.0F, 120.0F);
        float var4 = EasingFunctions.easeOutBack(partialTicks, 0.0F, 1.0F, 1.0F);
        this.setScale(0.8F + var4 * 0.2F, 0.8F + var4 * 0.2F);
        this.setTranslateX((int) ((float) this.width * 0.2F * (1.0F - var4)) * (!this.field20725 ? 1 : -1));
        super.applyScaleTransforms();
        int var5 = 10;
        int var6 = ColorHelper.applyAlpha(-723724, QuadraticEasing.easeOutQuad(partialTicks, 0.0F, 1.0F, 1.0F));
        RenderUtils.drawRoundedRect(
                (float) (this.x + var5 / 2),
                (float) (this.y + var5 / 2),
                (float) (this.width - var5),
                (float) (this.height - var5),
                35.0F,
                partialTicks
        );
        RenderUtils.drawRoundedRect(
                (float) (this.x + var5 / 2),
                (float) (this.y + var5 / 2),
                (float) (this.x - var5 / 2 + this.width),
                (float) (this.y - var5 / 2 + this.height),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks * 0.25F)
        );
        RenderUtils.drawRoundedRect((float) this.x, (float) this.y, (float) this.width, (float) this.height, (float) var5, var6);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) this.x, (float) this.y, 0.0F);
        GL11.glRotatef(!this.field20725 ? -90.0F : 90.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-this.x), (float) (-this.y), 0.0F);
        RenderUtils.drawImage(
                (float) (this.x + (!this.field20725 ? 0 : this.height)),
                (float) this.y + (float) ((this.width - 47) / 2) * (!this.field20725 ? 1.0F : -1.58F),
                18.0F,
                47.0F,
                Resources.selectPNG,
                var6
        );
        GL11.glPopMatrix();
        RenderUtils.drawRoundedRect(
                (float) (this.x + 25),
                (float) (this.y + 68),
                (float) (this.x + this.width - 25),
                (float) (this.y + 69),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.05F * partialTicks)
        );
        super.draw(partialTicks);
    }

    public final void method13131(Class9073 var1) {
        this.field20731.add(var1);
    }

    public final void method13132(String var1, Vec3i var2, int var3) {
        for (Class9073 var7 : this.field20731) {
            var7.method33814(this, var1, var2, var3);
        }
    }

    public interface Class9073 {
        void method33814(WaypointPanel var1, String var2, Vec3i var3, int var4);
    }
}
