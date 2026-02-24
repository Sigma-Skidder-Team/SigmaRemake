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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Waypoint waypoint = (Waypoint) o;
        if (color != waypoint.color) return false;
        if (x != waypoint.x) return false;
        if (z != waypoint.z) return false;
        if (!name.equals(waypoint.name)) return false;
        return identifier.equals(waypoint.identifier);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + color;
        result = 31 * result + x;
        result = 31 * result + z;
        result = 31 * result + identifier.hashCode();
        return result;
    }
}