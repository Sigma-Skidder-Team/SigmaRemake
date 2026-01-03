package io.github.sst.remake.gui.screen.maps;

import io.github.sst.remake.util.math.vec.Vector3m;

public interface MapPanelClickListener {
    void onMapClick(MapPanel panel, int mouseX, int mouseY, Vector3m position);
}
