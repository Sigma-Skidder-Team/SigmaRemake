package io.github.sst.remake.gui.element.impl.alts;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sst.remake.alt.Account;
import io.github.sst.remake.util.world.MockPlayerEntity;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.panel.AnimatedIconPanel;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderHelper;
import io.github.sst.remake.util.render.font.FontAlignment;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.ProfilerSystem;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.lwjgl.opengl.GL11;

import java.util.UUID;
import java.util.function.IntSupplier;

public class EntityElement extends AnimatedIconPanel implements IMinecraft {
    public static ColorHelper field20821 = new ColorHelper(
            ClientColors.DEEP_TEAL.getColor(),
            ClientColors.DEEP_TEAL.getColor(),
            ClientColors.DEEP_TEAL.getColor(),
            ClientColors.DEEP_TEAL.getColor(),
            FontAlignment.LEFT,
            FontAlignment.CENTER
    );

    public String skin;
    private static ClientWorld clientWorld;
    public Account account;
    private MockPlayerEntity entity;

    public EntityElement(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, String skinName) {
        super(var1, var2, var3, var4, var5, var6, field20821, false);
        this.skin = skinName;
    }

    public Profiler getProfiler() {
        Class7991 var3 = new Class7991(this);
        return new ProfilerSystem(() -> 0L, var3, false);
    }

    @Override
    public void draw(float partialTicks) {
        if (this.account != null) {
            GL11.glEnable(2929);
            RenderHelper.enableStandardItemLighting();
            RenderSystem.disableDepthTest();
            RenderSystem.pushMatrix();
            RenderSystem.translatef((float) (this.x + this.width / 2), (float) (this.y - this.height / 4), -200.0F);
            GL11.glColor3f(1.0F, 1.0F, 1.0F);
            RenderSystem.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
            RenderSystem.rotatef(180.0F, 0.0F, 0.0F, 1.0F);

            float var4 = (float) (client.getWindow().getHeight() - this.getMouseY() - client.getWindow().getHeight() / 2);
            float var5 = (float) (client.getWindow().getWidth() - this.getMouseX() - client.getWindow().getWidth() / 2);
            float var6 = (float) Math.atan(var4 / (float) (client.getWindow().getHeight() / 2)) * 20.0F;
            float var7 = (float) Math.atan(var5 / (float) (client.getWindow().getWidth() / 2)) * 20.0F;

            RenderSystem.rotatef(-var6, 1.0F, 0.0F, 0.0F);
            RenderSystem.rotatef(-var7, 0.0F, 1.0F, 0.0F);

            UUID uuid = UUID.fromString(this.account.getUUID());

            if (clientWorld == null) {
                ClientWorld.Properties properties = new ClientWorld.Properties(Difficulty.NORMAL, false, false);
                clientWorld = new ClientWorld(client.getNetworkHandler(), properties, World.OVERWORLD, DimensionType.OVERWORLD, 1, this::getProfiler, null, false, 0L);
            }

            GameProfile prof = new GameProfile(uuid, this.account.name);
            if (this.entity == null || !this.entity.getDisplayName().getString().equals(this.account.name)) {
                this.entity = new MockPlayerEntity(clientWorld, new GameProfile(uuid, this.account.name));

                this.entity.cachedScoreboardEntry =
                        new PlayerListEntry(
                                new PlayerListS2CPacket().new Entry(
                                        prof,
                                        0,
                                        GameMode.CREATIVE,
                                        this.entity.getDisplayName()
                                )
                        );
            }

            this.entity.setUuid(uuid);
            float var10 = (float) (System.currentTimeMillis() % 1750L) / 278.52115F;
            var10 = (float) Math.sin(var10);
            RenderSystem.pushMatrix();
            client.getEntityRenderDispatcher().setRenderShadows(false);

            RenderHelper.setupGui3DDiffuseLighting();
            RenderSystem.enableLighting();
            RenderSystem.enableDepthTest();
            GL11.glLightModelfv(2899, new float[]{0.7F, 0.7F, 0.7F, 1.0F});

            if (client.getEntityRenderDispatcher().camera == null) {
                client.getEntityRenderDispatcher().camera = new Camera();
            }

            this.entity.limbDistance = var10 * 0.5F;
            InventoryScreen.drawEntity(0, 390, 160, 0.0F, 0.0F, this.entity);
            client.getEntityRenderDispatcher().setRenderShadows(true);
            RenderSystem.popMatrix();
            RenderSystem.popMatrix();
            RenderHelper.disableStandardItemLighting();
            RenderSystem.disableRescaleNormal();
            RenderSystem.activeTexture(33985);
            RenderSystem.disableTexture();
            RenderSystem.activeTexture(33984);
            GL11.glDisable(2929);
        }
    }

    public void handleSelectedAccount(Account var1) {
        this.account = var1;
    }

    public static class Class7991 implements IntSupplier {
        private int field34347;
        private int field34348;
        public final EntityElement field34349;

        public Class7991(EntityElement face) {
            this.field34349 = face;
            this.field34347 = 0;
            this.field34348 = 1;
        }

        @Override
        public int getAsInt() {
            int var3 = this.field34347 + this.field34348;
            this.field34347 = this.field34348;
            this.field34348 = var3;
            return this.field34347;
        }
    }
}
