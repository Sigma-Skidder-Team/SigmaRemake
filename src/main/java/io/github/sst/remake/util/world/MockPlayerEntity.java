package io.github.sst.remake.util.world;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class MockPlayerEntity extends AbstractClientPlayerEntity {
    public MockPlayerEntity(ClientWorld var1, GameProfile var2) {
        super(var1, var2);
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return true;
    }

    @Override
    public boolean canRenderCapeTexture() {
        return false;
    }

    @Override
    public PlayerListEntry getPlayerListEntry() {
        return this.cachedScoreboardEntry;
    }

    @Override
    public boolean hasSkinTexture() {
        return true;
    }

    @Override
    public Identifier getSkinTexture() {
        PlayerListEntry entry = this.getPlayerListEntry();
        return entry != null ? entry.getSkinTexture() : DefaultSkinHelper.getTexture(this.getUuid());
    }

    @Override
    public @Nullable Identifier getCapeTexture() {
        PlayerListEntry entry = this.getPlayerListEntry();
        return entry != null ? entry.getCapeTexture() : null;
    }

    @Override
    public boolean canRenderElytraTexture() {
        return this.getPlayerListEntry() != null;
    }

    @Override
    public @Nullable Identifier getElytraTexture() {
        PlayerListEntry entry = this.getPlayerListEntry();
        return entry != null ? entry.getElytraTexture() : null;
    }

    @Override
    public float getSpeed() {
        return 0.0f;
    }
}
