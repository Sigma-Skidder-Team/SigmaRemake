package io.github.sst.remake.util.client.waypoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.sst.remake.util.client.WaypointUtils;

public class Waypoint {
    public String name;
    public int color;

    public int x, z;
    public float y;

    public boolean config;
    public String identifier;

    public Waypoint(String name, int color, int x, int z, float y, boolean config, String identifier) {
        this.name = name;
        this.color = color;
        this.x = x;
        this.z = z;
        this.y = y;
        this.config = config;
        this.identifier = identifier;
    }

    public Waypoint(String name, int color, int x, int z, float y) {
        this(name, color, x, z, y, false, WaypointUtils.getWorldIdentifier());
    }

    public Waypoint(String name, int x, int z, int color) {
        this(name, color, x, z, 64, true, WaypointUtils.getWorldIdentifier());
    }

    public Waypoint(JsonObject from, String identifier) throws JsonParseException {
        if (from.has("name")) {
            this.name = from.get("name").getAsString();
        }

        if (from.has("color")) {
            this.color = from.get("color").getAsInt();
        }

        if (from.has("x")) {
            this.x = from.get("x").getAsInt();
        }

        if (from.has("z")) {
            this.z = from.get("z").getAsInt();
        }

        this.y = 64;

        this.config = true;
        this.identifier = identifier;
    }

}
