package com.skidders.sigma.handler.impl;

import com.skidders.sigma.event.impl.KeyPressEvent;
import com.skidders.sigma.handler.Handler;
import com.skidders.sigma.module.Category;
import com.skidders.sigma.module.Module;
import com.skidders.sigma.module.impl.gui.*;
import com.skidders.sigma.module.impl.player.AutoSprint;
import com.skidders.sigma.util.client.events.Listen;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.stream.Collectors;

public class ModuleHandler extends Handler<Module> {
    @Override
    public void init() {
        super.init();
        list.add(new ActiveMods());
        list.add(new AutoSprint());

        list.forEach(m -> m.onInit());
    }

    @Listen
    private void onKey(KeyPressEvent event) {
        if (event.action == GLFW.GLFW_RELEASE && MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().currentScreen == null)
            this.list.forEach(m -> {
                if (m.getKey() == event.key) {
                    m.setEnabled(!m.isEnabled());
                }
            });
    }

    public Module getModuleByName(String input) {
        return list.stream().filter(m -> m.getName().equalsIgnoreCase(input)).findFirst().orElse(null);
    }

    public List<Module> getEnabledModules() {
        return list.stream().filter(Module::isEnabled).collect(Collectors.toList());
    }

    public List<Module> getModulesByCategory(Category input) {
        return list.stream().filter(mod ->
                mod.getCategory().equals(input)).collect(Collectors.toList()
        );
    }
}