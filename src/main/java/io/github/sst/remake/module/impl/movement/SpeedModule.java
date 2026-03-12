package io.github.sst.remake.module.impl.movement;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.ClientPlayerTickEvent;
import io.github.sst.remake.event.impl.game.net.ReceivePacketEvent;
import io.github.sst.remake.Client;
import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.module.impl.movement.speed.VanillaSpeed;
import io.github.sst.remake.module.impl.movement.speed.NCPSpeed;
import io.github.sst.remake.module.impl.movement.speed.VerusSpeed;
import io.github.sst.remake.module.impl.movement.speed.AACSpeed;
import io.github.sst.remake.module.impl.movement.speed.HypixelSpeed;
import io.github.sst.remake.module.impl.movement.speed.HypixelNewSpeed;
import io.github.sst.remake.module.impl.movement.speed.UpdatedNCPSpeed;
import io.github.sst.remake.module.impl.movement.speed.TestSpeed;
import io.github.sst.remake.module.impl.movement.speed.SlowHopSpeed;
import io.github.sst.remake.module.impl.movement.speed.OldAACSpeed;
import io.github.sst.remake.module.impl.movement.speed.MineplexSpeed;
import io.github.sst.remake.module.impl.movement.speed.MinemenSpeed;
import io.github.sst.remake.module.impl.movement.speed.LowHopSpeed;
import io.github.sst.remake.module.impl.movement.speed.LegitSpeed;
import io.github.sst.remake.module.impl.movement.speed.InvadedSpeed;
import io.github.sst.remake.module.impl.movement.speed.GommeSpeed;
import io.github.sst.remake.module.impl.movement.speed.CubecraftSpeed;
import io.github.sst.remake.module.impl.movement.speed.BoostSpeed;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.setting.impl.SubModuleSetting;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

// Import Speed mode implementations here, e.g. VanillaSpeed, NCPSpeed, etc.
// e.g., import io.github.sst.remake.module.impl.movement.speed.VanillaSpeed;

public class SpeedModule extends Module {
    // Used for lagback disabling and tick tracking
    public static int tickCounter = 0;

    // Mode system for Speed
    private final SubModuleSetting mode = new SubModuleSetting(
            "Mode", "Speed mode",
            new VanillaSpeed(), new UpdatedNCPSpeed(), new HypixelSpeed(), new HypixelNewSpeed(), new AACSpeed(), new OldAACSpeed(),
            new NCPSpeed(), new BoostSpeed(), new SlowHopSpeed(), new LowHopSpeed(), new MinemenSpeed(), new MineplexSpeed(),
            new LegitSpeed(), new CubecraftSpeed(), new InvadedSpeed(), new GommeSpeed(), new VerusSpeed(), new TestSpeed()
    );

    public final BooleanSetting lagbackChecker = new BooleanSetting(
            "Lagback checker",
            "Disable speed when you get lagback",
            true
    );

    public SpeedModule() {
        super("Speed", "Vroom vroom", Category.MOVEMENT);
    }

    @Subscribe
    public void onReceivePacket(ReceivePacketEvent event) {
        // Mirror legacy behavior: reset tick counter and disable on lagback
        if (event.packet instanceof PlayerPositionLookS2CPacket && client.player != null) {
            tickCounter = 0;
            if (this.lagbackChecker.value && this.enabled) {
                Client.INSTANCE.notificationManager.send(new io.github.sst.remake.gui.screen.notifications.Notification("Speed", "Disabled speed due to lagback."));
                this.toggle();
            }
        }
    }

    // Helper used by legacy code paths (some modules may call into Speed)
    public void callHypixelSpeedMethod() {
        // Find Hypixel submodule and, if present, try to invoke its special method if implemented
        for (Setting<?> s : this.settings) {
            if (s instanceof io.github.sst.remake.setting.impl.SubModuleSetting) {
                io.github.sst.remake.setting.impl.SubModuleSetting sms = (io.github.sst.remake.setting.impl.SubModuleSetting) s;
                for (io.github.sst.remake.module.SubModule m : sms.modes) {
                    if (m instanceof HypixelSpeed) {
                        HypixelSpeed hyp = (HypixelSpeed) m;
                        try {
                            java.lang.reflect.Method mm = hyp.getClass().getMethod("method16044");
                            mm.invoke(hyp);
                        } catch (NoSuchMethodException ignored) {
                        } catch (ReflectiveOperationException e) {
                            Client.LOGGER.error("Failed to call HypixelSpeed method", e);
                        }
                    }
                }
            }
        }
    }

    @Subscribe
    public void onTick(ClientPlayerTickEvent event) {
        tickCounter++;
    }

    // Optionally: callHypixelSpeedMethod() pattern if needed by submodule
}
