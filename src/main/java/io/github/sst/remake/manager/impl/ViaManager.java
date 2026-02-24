package io.github.sst.remake.manager.impl;

import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.util.viaversion.ViaProtocols;
import io.github.sst.remake.util.viaversion.version.ClientSideVersionUtils;
import net.minecraft.client.option.GameOptions;

public final class ViaManager extends Manager {
    @Override
    public void init() {
        super.init();
        ClientSideVersionUtils.init();
    }

    public void onVersionChange(GameOptions ignoredOptions, Double version) {
        int newIndex = version.intValue();
        ViaProtocols protocol = ViaProtocols.getByIndex(newIndex);

        ClientSideVersionUtils.setClientSideVersion(protocol.protocol);
    }
}
