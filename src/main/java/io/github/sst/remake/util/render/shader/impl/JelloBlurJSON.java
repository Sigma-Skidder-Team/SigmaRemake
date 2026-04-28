package io.github.sst.remake.util.render.shader.impl;

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
        InputStream stream = this.getClass().getResourceAsStream("/assets/sigma/shader/blur.json");

        if (stream == null) {
            throw new RuntimeException("Failed to find blur.json in the assets folder!");
        }

        return stream;
    }

    @Override
    public boolean hasMetadata() {
        return false;
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