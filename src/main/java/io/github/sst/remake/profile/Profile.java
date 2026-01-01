package io.github.sst.remake.profile;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Profile {
    public final String name;
    public final JsonObject content;

    public Profile(String name, Profile base) {
        this(name, base.content);
    }

    public Profile clone(String name) {
        return new Profile(name, this.content);
    }
}
