package io.github.sst.remake.util.game.world.data;

public class PlacementPattern {
    public int offsetX;
    public int offsetY;
    public int offsetZ;
    public boolean isAdditive;

    public PlacementPattern(int x, int y, int z, boolean additive) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        this.isAdditive = additive;
    }
}