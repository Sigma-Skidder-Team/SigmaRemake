package com.skidders.sigma.module.impl.player;

import com.skidders.sigma.module.Category;
import com.skidders.sigma.module.Module;
import org.lwjgl.glfw.GLFW;

public class AutoSprint extends Module {
    public AutoSprint() {
        super("AutoSprint", "Sprints for you", Category.PLAYER);
        setKey(GLFW.GLFW_KEY_H);
    }
}
