package io.github.sst.remake.util.client.blur;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SigmaBlurShader implements ResourceManager {

    public @NotNull Set<String> getAllNamespaces() {
        return MinecraftClient.getInstance().getResourceManager().getAllNamespaces();
    }

    public @NotNull Resource getResource(Identifier resourceLocationIn) throws IOException {
        return !resourceLocationIn.getPath().equals("jelloblur") ? MinecraftClient.getInstance().getResourceManager().getResource(resourceLocationIn) : new JelloBlurJSON();
    }

    public boolean containsResource(Identifier path) {
        return path.getPath().equals("jelloblur") || MinecraftClient.getInstance().getResourceManager().containsResource(path);
    }

    public @NotNull List<Resource> getAllResources(@NotNull Identifier resourceLocationIn) throws IOException {
        return MinecraftClient.getInstance().getResourceManager().getAllResources(resourceLocationIn);
    }

    public @NotNull Collection<Identifier> findResources(@NotNull String pathIn, @NotNull Predicate<String> filter) {
        return MinecraftClient.getInstance().getResourceManager().findResources(pathIn, filter);
    }

    @Override
    public Stream<ResourcePack> streamResourcePacks() {
        return Stream.empty();
    }
}