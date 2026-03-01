package io.github.sst.remake.gui.screen.maps;

import net.minecraft.util.math.Vec3i;

public interface MapRightClickListener {
    void onRightClick(WorldMapView frame, int mouseX, int mouseY, Vec3i position);
}
