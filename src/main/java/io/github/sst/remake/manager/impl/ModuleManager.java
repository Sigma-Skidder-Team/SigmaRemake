package io.github.sst.remake.manager.impl;

import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.module.impl.TestModule;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager extends Manager {

    public final List<Module> modules = new ArrayList<>();

    public Module currentModule = null;

    @Override
    public void init() {
        modules.add(new TestModule());
        initModules();
        super.init();
    }

    @Override
    public void shutdown() {
        modules.clear();
        super.shutdown();
    }

    private void initModules() {
        modules.forEach(Module::onInit);
    }
}
