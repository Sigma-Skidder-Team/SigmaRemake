package com.skidders.sigma.managers;

import com.google.common.eventbus.Subscribe;
import com.skidders.sigma.events.impl.KeyPressEvent;
import com.skidders.sigma.module.Category;
import com.skidders.sigma.module.Module;
import com.skidders.sigma.module.impl.gui.*;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {

    public List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        modules.add(new ActiveMods());
    }

    @Subscribe
    public void onKey(KeyPressEvent event) {
        if (event.action == GLFW.GLFW_RELEASE && MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().currentScreen == null)
            this.modules.forEach(m -> {
                if (m.key == event.key) {
                    m.setEnabled(!m.enabled);
                }
            });
    }

    public Module getModuleByName(String input) {
        return modules.stream().filter(m -> m.name.equalsIgnoreCase(input)).findFirst().orElse(null);
    }

    public List<Module> getEnabledModules() {
        return modules.stream().filter(m -> m.enabled).collect(Collectors.toList());
    }

    public <V extends Module> V getModuleByClass(Class<V> clazz) {
        Module mod = modules.stream().filter(m -> m.getClass().equals(clazz)).findFirst().orElse(null);

        if (mod == null) {
            return null;
        }

        return clazz.cast(mod);
    }

    public List<Module> getModulesByCategory(Category input) {
        return modules.stream().filter(mod ->
                mod.category.equals(input)).collect(Collectors.toList()
        );
    }

}
