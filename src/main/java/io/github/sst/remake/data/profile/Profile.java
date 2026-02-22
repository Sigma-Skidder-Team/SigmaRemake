package io.github.sst.remake.data.profile;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Profile {
    public String name;
    public final JsonObject content;

    public Profile(String name, Profile base) {
        this(name, base.content);
    }

    public Profile clone(String name) {
        return new Profile(name, this.content);
    }
}
