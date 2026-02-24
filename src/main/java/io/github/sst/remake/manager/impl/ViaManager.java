package io.github.sst.remake.manager.impl;

import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.util.viaversion.version.ClientSideVersionUtils;

public final class ViaManager extends Manager {
    @Override
    public void init() {
        super.init();
        ClientSideVersionUtils.init();
    }
}
