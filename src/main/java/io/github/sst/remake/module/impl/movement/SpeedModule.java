package io.github.sst.remake.module.impl.movement;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.ClientPlayerTickEvent;
import io.github.sst.remake.event.impl.game.net.ReceivePacketEvent;
import io.github.sst.remake.Client;
import io.github.sst.remake.module.impl.movement.speed.*;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.setting.impl.SubModuleSetting;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class SpeedModule extends Module {
    public static int tickCounter = 0;

    private final SubModuleSetting mode = new SubModuleSetting("Mode", "Speed mode", new LegitSpeed());
    private final BooleanSetting lagbackChecker = new BooleanSetting("Lagback checker", "Disable speed when you get lagback", true);

    public SpeedModule() {
        super("Speed", "Allows you to go faster.", Category.MOVEMENT);
    }

    @Subscribe
    public void onReceivePacket(ReceivePacketEvent event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket && client.player != null) {
            tickCounter = 0;
            if (this.lagbackChecker.value && this.enabled) {
                Client.INSTANCE.notificationManager.send(new io.github.sst.remake.gui.screen.notifications.Notification("Speed", "Disabled speed due to lagback."));
                this.toggle();
            }
        }
    }

    @Subscribe
    public void onTick(ClientPlayerTickEvent event) {
        tickCounter++;
    }
}