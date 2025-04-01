package com.skidders.sigma.handler.impl;

import com.skidders.sigma.event.impl.KeyPressEvent;
import com.skidders.sigma.handler.Handler;
import com.skidders.sigma.module.Category;
import com.skidders.sigma.module.Module;
import com.skidders.sigma.module.impl.gui.*;
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
    }

    @Listen
    private void onKey(KeyPressEvent event) {
        if (event.action == GLFW.GLFW_RELEASE && MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().currentScreen == null)
            this.list.forEach(m -> {
                if (m.key == event.key) {
                    m.setEnabled(!m.enabled);
                }
            });
    }

    public Module getModuleByName(String input) {
        return list.stream().filter(m -> m.name.equalsIgnoreCase(input)).findFirst().orElse(null);
    }

    public List<Module> getEnabledModules() {
        return list.stream().filter(m -> m.enabled).collect(Collectors.toList());
    }

    public <V extends Module> V getModuleByClass(Class<V> clazz) {
        Module mod = list.stream().filter(m -> m.getClass().equals(clazz)).findFirst().orElse(null);

        if (mod == null) {
            return null;
        }

        return clazz.cast(mod);
    }

    public List<Module> getModulesByCategory(Category input) {
        return list.stream().filter(mod ->
                mod.category.equals(input)).collect(Collectors.toList()
        );
    }
}