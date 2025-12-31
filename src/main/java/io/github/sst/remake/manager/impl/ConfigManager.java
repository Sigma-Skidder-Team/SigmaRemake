package io.github.sst.remake.manager.impl;

import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.client.ConfigUtils;

public class ConfigManager extends Manager implements IMinecraft {

    public boolean guiBlur = false;
    public boolean hqBlur = false;

    @Override
    public void init() {
        if (!ConfigUtils.CLIENT_FOLDER.exists()) {
            ConfigUtils.CLIENT_FOLDER.mkdirs();
        }
    }

}
