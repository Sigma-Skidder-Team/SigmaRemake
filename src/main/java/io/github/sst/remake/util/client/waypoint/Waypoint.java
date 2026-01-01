package io.github.sst.remake.util.client.waypoint;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Waypoint {
    public final String name;
    public final int color;

    public int x, z;
    public float y;

    public Waypoint(String name, int x, int z, int color) {
        this(name, color, x, z, 64);
    }

}
