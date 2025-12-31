package io.github.sst.remake.profile;

import com.google.gson.JsonObject;
import io.github.sst.remake.util.client.ConfigUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Profile {
    public final String name;
    public final JsonObject content;

    public String getFullName() {
        return name + ConfigUtils.EXTENSION;
    }
}
