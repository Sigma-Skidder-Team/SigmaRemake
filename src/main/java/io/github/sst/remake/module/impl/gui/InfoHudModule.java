package io.github.sst.remake.module.impl.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sst.remake.bus.Subscribe;
import io.github.sst.remake.event.impl.client.RenderClient2DEvent;
import io.github.sst.remake.event.impl.game.ModifyChatYEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.setting.impl.ModeSetting;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import org.lwjgl.opengl.GL11;

public class InfoHudModule extends Module {
    private final ModeSetting cords = new ModeSetting("Cords", "Coordinate display type", 1, "None", "Normal", "Precise");
    private final BooleanSetting showPlayer = new BooleanSetting("Show Player", "Renders a miniature version of your character", true);
    private final BooleanSetting showArmor = new BooleanSetting("Show Armor", "Shows your armor's status", true);
    private final BooleanSetting moveChat = new BooleanSetting("Move chat up", "Moves the chat gui up", true);

    public InfoHudModule() {
        super("Info HUD", "Shows a bunch of usefull stuff", Category.GUI);
    }

    @Subscribe
    public void onRenderChat(ModifyChatYEvent event) {
        if (moveChat.value) {
            event.increment(-40);
        }
    }

    @Subscribe
    public void onRender2D(RenderClient2DEvent ignoredEvent) {
        if (client.player != null && !client.options.hudHidden) {
            if (!(client.currentScreen instanceof GameMenuScreen)) {
                int offset = 14;

                if (showPlayer.value) {
                    int y = client.getWindow().getHeight() - 22;
                    //if (Client.getInstance().musicManager.isPlayingSong()) y -= 105;
                    offset += this.renderPlayerModel(0, y, 114);
                }

                if (showArmor.value) {
                    int y = client.getWindow().getHeight() - 14;
                    //if (Client.getInstance().musicManager.isPlayingSong()) y -= 105;

                    offset += this.renderArmorStatus(offset, y) + 10;
                }

                if (!cords.value.equals("None")) {
                    int y = 42;
                    //if (Client.getInstance().musicManager.isPlayingSong()) y += 105;
                    offset += this.renderCoordinates(offset, y) + 10;
                }
            }
        }
    }

    private String getFormattedCoordinates() {
        return !cords.value.equals("Precise")
                ? Math.round(client.player.getX()) + " " +
                Math.round(client.player.getY()) + " " +
                Math.round(client.player.getZ())
                : (float) Math.round(client.player.getX() * 10.0) / 10.0F + " " +
                (float) Math.round(client.player.getY() * 10.0) / 10.0F + " " +
                (float) Math.round(client.player.getZ() * 10.0) / 10.0F;
    }

    private int renderCoordinates(int x, int yOffset) {
        String direction = "Facing South";
        String coordinates = this.getFormattedCoordinates();
        RenderUtils.drawString(
                FontUtils.HELVETICA_MEDIUM_20,
                (float) x,
                (float) (client.getWindow().getHeight() - yOffset),
                coordinates,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.8F));
        return Math.max(FontUtils.HELVETICA_LIGHT_20.getWidth(direction),
                FontUtils.HELVETICA_MEDIUM_20.getWidth(coordinates));
    }

    private int renderArmorStatus(int x, int y) {
        int armorCount = 0;

        for (int i = 0; i < client.player.inventory.armor.size(); i++) {
            ItemStack armorPiece = client.player.inventory.armor.get(i);
            if (!(armorPiece.getItem() instanceof AirBlockItem)) {
                armorCount++;
                int armorY = y - 32 * armorCount;
                RenderUtils.renderItemStack(armorPiece, x, armorY, 32, 32);
                GL11.glDisable(GL11.GL_LIGHTING);
                float durability = 1.0F - (float) armorPiece.getDamage() / (float) armorPiece.getMaxDamage();
                if (durability != 1.0F) {
                    RenderUtils.drawRect((float) (x + 2), (float) (armorY + 28), 28.0F, 5.0F,
                            ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.5F));
                    RenderUtils.drawRect(
                            (float) (x + 2),
                            (float) (armorY + 28),
                            28.0F * durability,
                            3.0F,
                            ColorHelper.applyAlpha(durability > 0.2 ? ClientColors.DARK_SLATE_GREY.getColor()
                                    : ClientColors.PALE_YELLOW.getColor(), 0.9F));
                }
            }
        }
        return armorCount != 0 ? 32 : -7;
    }

    private int renderPlayerModel(int x, int y, int height) {
        drawEntityOnScreen(x + height / 2, y, height / 2, client.player);
        return height - 24;
    }

    private void drawEntityOnScreen(int posX, int posY, int scale, LivingEntity livingEntity) {
        float fixedYaw = livingEntity.yaw;
        float fixedPitch = livingEntity.pitch;

        RenderSystem.pushMatrix();
        RenderSystem.translatef((float) posX, (float) posY, 1050.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);

        MatrixStack matrixstack = new MatrixStack();
        matrixstack.translate(0.0D, 0.0D, 1000.0D);
        matrixstack.scale((float) scale, (float) scale, (float) scale);

        Quaternion quaternion = Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0F);
        matrixstack.multiply(quaternion);

        float f2 = livingEntity.bodyYaw;
        float f3 = livingEntity.yaw;
        float f4 = livingEntity.pitch;
        float f5 = livingEntity.prevHeadYaw;
        float f6 = livingEntity.headYaw;

        livingEntity.bodyYaw = f2;
        livingEntity.yaw = fixedYaw;
        livingEntity.pitch = fixedPitch;
        livingEntity.headYaw = f6;
        livingEntity.prevHeadYaw = f5;

        EntityRenderDispatcher entityrenderermanager = client.getEntityRenderDispatcher();
        entityrenderermanager.setRotation(new Quaternion(0, 0, 0, 1)); // Set no camera rotation
        entityrenderermanager.setRenderShadows(false);

        VertexConsumerProvider.Immediate irendertypebuffer$impl = client.getBufferBuilders().getEntityVertexConsumers();
        RenderSystem.runAsFancy(() -> entityrenderermanager.render(livingEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixstack, irendertypebuffer$impl, 15728880));

        irendertypebuffer$impl.draw();
        entityrenderermanager.setRenderShadows(true);

        livingEntity.bodyYaw = f2;
        livingEntity.yaw = f3;
        livingEntity.pitch = f4;
        livingEntity.prevHeadYaw = f5;
        livingEntity.headYaw = f6;

        RenderSystem.popMatrix();
    }
}
