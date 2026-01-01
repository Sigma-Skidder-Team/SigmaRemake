package io.github.sst.remake.profile;

import com.google.gson.JsonObject;
import io.github.sst.remake.util.client.ConfigUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Profile {
    public final String name;
    public final JsonObject content;

    public Profile(String name, Profile base) {
        this(name, base.content);
    }

    public String getFullName() {
        return name + ConfigUtils.EXTENSION;
    }

    public Profile clone(String name) {
        return new Profile(name, this.content);
    }
}
