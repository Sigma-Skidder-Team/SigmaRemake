package io.github.sst.remake.manager.impl;

import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.module.impl.BrainFreezeModule;
import io.github.sst.remake.module.impl.TestModule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager extends Manager {

    public final List<Module> modules = new ArrayList<>();

    public Module currentModule = null;

    @Override
    public void init() {
        modules.add(new TestModule());
        modules.add(new BrainFreezeModule());
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

    public Module getModule(String input) {
        return this.modules.stream().filter(m -> m.name.equalsIgnoreCase(input)).findFirst().orElse(null);
    }

    public List<Module> getModulesByCategory(Category input) {
        return this.modules.stream().filter(mod ->
                mod.category.equals(input)).collect(Collectors.toList()
        );
    }
}
