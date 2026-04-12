package io.github.sst.remake.module.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.net.ReceivePacketEvent;
import io.github.sst.remake.event.impl.game.net.SendPacketEvent;
import io.github.sst.remake.event.impl.game.player.MotionEvent;
import io.github.sst.remake.event.impl.game.render.Render3DEvent;
import io.github.sst.remake.event.impl.game.render.RenderNameTagEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.module.impl.render.nametags.FurnaceTracker;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.tracker.impl.BotTracker;
import io.github.sst.remake.util.game.player.PlayerUtils;
import io.github.sst.remake.util.game.world.EntityUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.client.gui.screen.ingame.FurnaceScreen;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerPropertyUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.newdawn.slick.opengl.font.TrueTypeFont;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

@SuppressWarnings("unused")
public class NametagsModule extends Module {
    private static final int BACKGROUND_COLOR = ColorHelper.applyAlpha(
            ColorHelper.blendColors(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor(), 75.0F), 0.5F);

    private final BooleanSetting magnify = new BooleanSetting("Magnify", "Scales nametags to keep them readable", true);
    private final BooleanSetting furnaces = new BooleanSetting("Furnaces", "Shows furnaces info once open", true);
    private final BooleanSetting mobOwners = new BooleanSetting("Mob Owners", "Shows mob owners", true);

    private final HashMap<BlockPos, FurnaceTracker> furnaceTrackers = new HashMap<>();
    private final HashMap<UUID, String> mobOwnerNames = new HashMap<>();
    private final List<Entity> entities = new ArrayList<>();
    private BlockPos currentBlockPos;

    public NametagsModule() {
        super(Category.RENDER, "Nametags", "Highlight player names above them.");
    }

    @Subscribe
    public void onRenderNameTag(RenderNameTagEvent event) {
        if (event.entity instanceof PlayerEntity) {
            event.cancelled = true;
        }
    }

    @Subscribe
    public void onSendPacket(SendPacketEvent event) {
        if (client.world == null) return;

        if (event.packet instanceof PlayerInteractBlockC2SPacket) {
            PlayerInteractBlockC2SPacket packet = (PlayerInteractBlockC2SPacket) event.packet;
            BlockPos pos = packet.getBlockHitResult().getBlockPos();
            if (client.world.getBlockState(pos).getBlock() instanceof FurnaceBlock) {
                this.currentBlockPos = pos;
            }
        }

        if (event.packet instanceof ClickSlotC2SPacket) {
            ClickSlotC2SPacket clickPacket = (ClickSlotC2SPacket) event.packet;
            FurnaceTracker tracker = this.getFurnaceTrackerByWindowId(clickPacket.getSyncId());
            if (tracker == null) {
                return;
            }

            if (client.currentScreen instanceof FurnaceScreen) {
                FurnaceScreen furnaceScreen = (FurnaceScreen) client.currentScreen;
                tracker.inputStack = furnaceScreen.getScreenHandler().getSlot(0).getStack().copy();
                tracker.fuelStack = furnaceScreen.getScreenHandler().getSlot(1).getStack().copy();
                tracker.outputStack = furnaceScreen.getScreenHandler().getSlot(2).getStack().copy();
            }
        }
    }

    @Subscribe
    @SuppressWarnings("DataFlowIssue")
    public void onMotion(MotionEvent event) {
        if (!event.isPre()) return;

        if (!furnaces.value) {
            this.furnaceTrackers.clear();
        } else {
            Iterator<Entry<BlockPos, FurnaceTracker>> iterator = this.furnaceTrackers.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<BlockPos, FurnaceTracker> entry = iterator.next();

                // Remove furnace tracker if the block at its position is no longer a furnace
                if (!(client.world.getBlockState(entry.getKey()).getBlock() instanceof FurnaceBlock)) {
                    iterator.remove();
                    continue;
                }

                // Update the smelting progress for the furnace
                entry.getValue().updateSmelting();
            }
        }

        this.entities.clear();

        // Collect all players in the world, sorted by distance
        List<PlayerEntity> players = new ArrayList<>(client.world.getPlayers());
        players.sort(Comparator.comparingDouble(p -> p.squaredDistanceTo(client.player)));

        for (Entity entity : players) {
            if (entity != client.player
                    && !entity.isInvisible()) {
                this.entities.add(entity);
            }
        }
    }

    @Subscribe
    public void onReceivePacket(ReceivePacketEvent event) {
        if (event.packet instanceof OpenScreenS2CPacket) {
            OpenScreenS2CPacket openPacket = (OpenScreenS2CPacket) event.packet;
            if (openPacket.getScreenHandlerType() != ScreenHandlerType.FURNACE) {
                return;
            }

            this.furnaceTrackers.put(this.currentBlockPos, new FurnaceTracker(openPacket.getSyncId()));
        }

        if (event.packet instanceof ScreenHandlerSlotUpdateS2CPacket) {
            ScreenHandlerSlotUpdateS2CPacket slotPacket = (ScreenHandlerSlotUpdateS2CPacket) event.packet;
            FurnaceTracker tracker = this.getFurnaceTrackerByWindowId(slotPacket.getSyncId());
            if (tracker == null) {
                return;
            }

            if (slotPacket.getSlot() == 0) {
                tracker.inputStack = slotPacket.getItemStack().copy();
            } else if (slotPacket.getSlot() == 1) {
                tracker.fuelStack = slotPacket.getItemStack().copy();
            } else if (slotPacket.getSlot() == 2) {
                tracker.outputStack = slotPacket.getItemStack().copy();
            }
        }

        if (event.packet instanceof ScreenHandlerPropertyUpdateS2CPacket) {
            ScreenHandlerPropertyUpdateS2CPacket propPacket = (ScreenHandlerPropertyUpdateS2CPacket) event.packet;
            FurnaceTracker tracker = this.getFurnaceTrackerByWindowId(propPacket.getSyncId());
            if (tracker == null) {
                return;
            }

            switch (propPacket.getPropertyId()) {
                case 0:
                    tracker.smeltDelay = propPacket.getValue();
                    break;
                case 1:
                    tracker.cooldown = propPacket.getValue();
                    break;
                case 2:
                    tracker.smeltTime = (float) propPacket.getValue();
                    break;
                case 3:
                    tracker.smeltProgress = (float) propPacket.getValue();
                    break;
            }
        }
    }

    @Subscribe
    @SuppressWarnings("deprecation")
    public void on3D(Render3DEvent event) {
        if (client.world == null) return;

        GL13.glMultiTexCoord2f(33986, 240.0F, 240.0F);
        for (Entity entity : this.entities) {
            if (entity instanceof PlayerEntity && BotTracker.isBot((PlayerEntity) entity)) continue;

            float scale = 1.0F;
            if (magnify.value) {
                scale = (float) Math.max(1.0, Math.sqrt(entity.squaredDistanceTo(client.player) / 30.0));
            }

            Vec3d pos = EntityUtils.getEntityPosition(entity);
            this.drawNametag(
                    pos.x,
                    pos.y + (double) entity.getHeight(),
                    pos.z,
                    entity,
                    scale,
                    null);
            entity.setCustomNameVisible(false);
        }

        for (Entry<BlockPos, FurnaceTracker> entry : this.furnaceTrackers.entrySet()) {
            float scale = 1.0F;
            if (magnify.value) {
                scale = (float) Math.max(0.8F,
                        Math.sqrt(EntityUtils.calculateDistanceSquared(entry.getKey()) / 30.0));
            }

            this.drawFurnaceNametag(entry.getKey(), entry.getValue(), scale);
        }

        if (this.mobOwners.value) {
            for (Entity entity : client.world.getEntities()) {
                if (entity instanceof TameableEntity || entity instanceof HorseBaseEntity) {
                    UUID uuid = (entity instanceof TameableEntity)
                            ? ((TameableEntity) entity).getOwnerUuid()
                            : ((HorseBaseEntity) entity).getOwnerUuid();
                    if (uuid != null) {
                        if (!this.mobOwnerNames.containsKey(uuid)) {
                            this.mobOwnerNames.put(uuid, null);

                            new Thread(() -> {
                                try {
                                    String name = PlayerUtils.resolvePlayerName(uuid);
                                    if (name != null) {
                                        this.mobOwnerNames.put(uuid, name);
                                    }
                                } catch (Exception ignored) {
                                }
                            }).start();
                        }

                        if (this.mobOwnerNames.get(uuid) != null) {
                            float scale = 1.0F;
                            if (magnify.value) {
                                scale = (float) Math.max(1.0,
                                        Math.sqrt(entity.squaredDistanceTo(client.player) / 30.0));
                            }

                            Vec3d pos = EntityUtils.getEntityPosition(entity);
                            this.drawNametag(
                                    pos.x,
                                    pos.y + (double) entity.getHeight(),
                                    pos.z,
                                    entity,
                                    scale,
                                    this.mobOwnerNames.get(uuid));
                            entity.setCustomNameVisible(false);
                        }
                    }
                }
            }
        }

        GL11.glDisable(GL11.GL_LIGHTING);
        GL13.glMultiTexCoord2f(33986, 240.0F, 240.0F);
    }

    private FurnaceTracker getFurnaceTrackerByWindowId(int windowId) {
        for (Entry<BlockPos, FurnaceTracker> entry : this.furnaceTrackers.entrySet()) {
            if (entry.getValue().windowId == windowId) {
                return entry.getValue();
            }
        }
        return null;
    }

    private void drawFurnaceNametag(BlockPos furnacePos, FurnaceTracker furnace, float partialTicks) {
        TrueTypeFont font = FontUtils.HELVETICA_LIGHT_25;
        Camera camera = client.gameRenderer.getCamera();

        float renderX = (float) ((double) furnacePos.getX() - camera.getPos().x + 0.5);
        float renderY = (float) ((double) furnacePos.getY() - camera.getPos().y + 1.0);
        float renderZ = (float) ((double) furnacePos.getZ() - camera.getPos().z + 0.5);

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);

        float smeltingProgress = furnace.smeltProgress != 0 ? Math.min(furnace.smeltTime / furnace.smeltProgress, 1.0F) : 0;
        float cooldownProgress = furnace.cooldown != 0 ? Math.min((float) furnace.smeltDelay / (float) furnace.cooldown, 1.0F) : 0;
        int padding = 14;

        GL11.glPushMatrix();
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
        GL11.glTranslated(renderX, renderY + 0.6F - 0.33333334F * (1.0F - partialTicks), renderZ);
        GL11.glRotatef(camera.getYaw(), 0.0F, -1.0F, 0.0F);
        GL11.glRotatef(camera.getPitch(), 1.0F, 0.0F, 0.0F);
        GL11.glPushMatrix();

        float scale = 0.008F;
        GL11.glScalef(-scale * partialTicks, -scale * partialTicks, -scale * partialTicks);

        int nameplateWidth;
        ItemStack outputItem = furnace.refreshOutput();
        if (outputItem != null) {
            nameplateWidth = Math.max(FontUtils.HELVETICA_LIGHT_20.getWidth(outputItem.getName().getString()), 50);
        } else {
            nameplateWidth = 37;
        }

        int boxWidth = 51 + nameplateWidth + padding * 2;
        int boxHeight = 85 + padding * 2;

        GL11.glTranslated(-boxWidth / 2d, -boxHeight / 2d, 0.0);

        RenderUtils.drawRect(0.0F, 0.0F, (float) boxWidth, (float) boxHeight, BACKGROUND_COLOR);
        RenderUtils.drawRoundedRect(0.0F, 0.0F, (float) boxWidth, (float) boxHeight, 20.0F, 0.5F);

        RenderUtils.drawString(font, padding, (float) (padding - 5), "Furnace", ClientColors.LIGHT_GREYISH_BLUE.getColor());
        if (outputItem == null) {
            RenderUtils.drawString(
                    FontUtils.HELVETICA_LIGHT_20, (float) (padding + 15), (float) (padding + 40), "Empty",
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.6F));
        }

        ItemStack itemStack = furnace.refreshOutput();
        if (itemStack != null) {
            RenderUtils.renderItemStack(itemStack, padding, padding + 27, 45, 45);
            RenderUtils.drawString(FontUtils.HELVETICA_LIGHT_20, (float) (padding + 51), 40.0F,
                    itemStack.getName().getString(), ClientColors.LIGHT_GREYISH_BLUE.getColor());
            RenderUtils.drawString(FontUtils.HELVETICA_LIGHT_14, (float) (padding + 51), 62.0F,
                    "Count: " + itemStack.getCount(), ClientColors.LIGHT_GREYISH_BLUE.getColor());
        }

        // Cooldown bar: x1=0, y1=boxHeight-12, x2=clamped, y2=boxHeight-6 → width=x2, height=6
        RenderUtils.drawRect(0.0F, (float) boxHeight - 12.0F, Math.min((float) boxWidth * cooldownProgress, (float) boxWidth),
                6.0F, ColorHelper.applyAlpha(-106750, 0.3F));
        // Smelting bar: x1=0, y1=boxHeight-6, x2=clamped, y2=boxHeight → width=x2, height=6
        RenderUtils.drawRect(
                0.0F, (float) boxHeight - 6.0F, Math.min((float) boxWidth * smeltingProgress, (float) boxWidth), 6.0F,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.75F));

        GL11.glPopMatrix();
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawNametag(double x, double y, double z, Entity entity, float scale, String overrideName) {
        TrueTypeFont font = FontUtils.HELVETICA_LIGHT_25;
        String name = overrideName == null ? entity.getName().getString().replaceAll("§.", "") : overrideName;

        if (name.isEmpty()) {
            return;
        }

        Camera camera = client.gameRenderer.getCamera();
        float renderX = (float) (x - camera.getPos().x);
        float renderY = (float) (y - camera.getPos().y);
        float renderZ = (float) (z - camera.getPos().z);

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);

        String healthStr = (float) Math.round(((LivingEntity) entity).getHealth() * 10.0F) / 10.0F + "";
        float healthPercent = Math.min(((LivingEntity) entity).getHealth() / ((LivingEntity) entity).getMaxHealth(), 1.0F);

        GL11.glPushMatrix();
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
        GL11.glTranslated(renderX, renderY + 0.6F - 0.33333334F * (1.0F - scale), renderZ);
        GL11.glRotatef(camera.getYaw(), 0.0F, -1.0F, 0.0F);
        GL11.glRotatef(camera.getPitch(), 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.009F * scale, -0.009F * scale, -0.009F * scale);

        // Team color for health bar
        int teamColor = ColorHelper.applyAlpha(
                !(entity instanceof PlayerEntity)
                        ? ClientColors.LIGHT_GREYISH_BLUE.getColor()
                        : new Color(PlayerUtils.getTeamColor((PlayerEntity) entity)).getRGB(),
                0.5F);

        int halfWidth = font.getWidth(name) / 2;

        RenderUtils.drawRoundedRect((float) (-halfWidth - 10), -25.0F, (float) (halfWidth * 2 + 20),
                (float) (font.getHeight() + 27), 20.0F, 0.5F);

        // Rebase drawRect uses (x1, y1, x2, y2) but Remake uses (x, y, width, height)
        // Background: x1=-halfWidth-10, y1=-25, x2=halfWidth+10, y2=fontHeight+2
        RenderUtils.drawRect((float) (-halfWidth - 10), -25.0F, (float) (halfWidth * 2 + 20),
                (float) (font.getHeight() + 27), BACKGROUND_COLOR);

        // Health bar: x1=-halfWidth-10, y1=fontHeight-1-hurtTime/3, x2=clamped, y2=fontHeight+2
        float healthBarX1 = (float) (-halfWidth - 10);
        float healthBarY1 = (float) (font.getHeight() - 1) - (float) ((LivingEntity) entity).hurtTime / 3.0F;
        float healthBarX2 = Math.min((float) (halfWidth * 2 + 20) * (healthPercent - 0.5F), (float) (halfWidth + 10));
        RenderUtils.drawRect(healthBarX1, healthBarY1,
                healthBarX2 - healthBarX1,
                (float) (font.getHeight() + 2) - healthBarY1, teamColor);

        GL11.glTranslated(-font.getWidth(name) / 2d, 0.0, 0.0);
        int healthLabelWidth = FontUtils.HELVETICA_LIGHT_14.getWidth("Health: 20.0");
        String healthPrefix = "Health: ";
        int nameWidth = font.getWidth(name);
        if (healthLabelWidth > nameWidth) {
            healthPrefix = "H: ";
        }

        RenderUtils.drawString(font, 0.0F, -20.0F, name, ClientColors.LIGHT_GREYISH_BLUE.getColor());
        RenderUtils.drawString(FontUtils.HELVETICA_LIGHT_14, 0.0F, 10.0F, healthPrefix + healthStr,
                ClientColors.LIGHT_GREYISH_BLUE.getColor());

        GL11.glPopMatrix();

        // Reset GL state
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
    }
}