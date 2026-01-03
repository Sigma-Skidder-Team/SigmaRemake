package io.github.sst.remake.util.client.blur;

import net.minecraft.resource.Resource;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class JelloBlurJSON implements Resource {
    @Override
    public Identifier getId() {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        String var3 = "{\"targets\":[\"jelloswap\",\"jello\"],\"passes\":[{\"name\":\"blur\",\"intarget\":\"minecraft:main\",\"outtarget\":\"jelloswap\",\"uniforms\":[{\"name\":\"BlurDir\",\"values\":[1,0]},{\"name\":\"Radius\",\"values\":[20]}]},{\"name\":\"blur\",\"intarget\":\"jelloswap\",\"outtarget\":\"jello\",\"uniforms\":[{\"name\":\"BlurDir\",\"values\":[0,1]},{\"name\":\"Radius\",\"values\":[20]}]}]}";
        return new ByteArrayInputStream(var3.getBytes());
    }

    @Override
    public @Nullable <T> T getMetadata(ResourceMetadataReader<T> metaReader) {
        return null;
    }

    @Override
    public String getResourcePackName() {
        return "";
    }

    @Override
    public void close() {
    }
}
