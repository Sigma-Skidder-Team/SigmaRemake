package io.github.sst.remake.gui.screen.maps;

public class ZoomRipple {
    public float progress;
    public boolean zoomIn;
    public final MapZoomControl owner;

    public ZoomRipple(MapZoomControl parent, boolean zoomIn) {
        this.owner = parent;
        this.progress = 0.0F;
        this.zoomIn = zoomIn;
    }
}
