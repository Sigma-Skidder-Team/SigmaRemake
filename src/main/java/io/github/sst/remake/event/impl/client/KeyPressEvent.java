package io.github.sst.remake.event.impl.client;

import io.github.sst.remake.event.Cancellable;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.BlockPos;

@RequiredArgsConstructor
public class KeyPressEvent extends Cancellable {
    public final int key;
    public final boolean pressed;
    public final BlockPos pos;
}
