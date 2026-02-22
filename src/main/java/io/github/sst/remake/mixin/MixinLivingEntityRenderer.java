package io.github.sst.remake.mixin;

import io.github.sst.remake.event.impl.game.render.RenderEntityPitchEvent;
import io.github.sst.remake.event.impl.game.render.RenderEntityYawEvent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>>
        extends EntityRenderer<T>
        implements FeatureRendererContext<T, M> {

    protected MixinLivingEntityRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Redirect(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F",
                    ordinal = 1
            )
    )
    private float redirectYaw(float delta, float start, float end, LivingEntity livingEntity, float f, float g, MatrixStack matrices, VertexConsumerProvider consumers, int light) {
        RenderEntityYawEvent event = new RenderEntityYawEvent(livingEntity, g);
        event.call();

        if (event.cancelled) {
            return event.result;
        }

        return MathHelper.lerpAngleDegrees(g, start, end);
    }

    @Redirect(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F",
                    ordinal = 0
            )
    )
    private float redirectPitch(float delta, float start, float end, T livingEntity, float f, float g, MatrixStack matrices, VertexConsumerProvider consumers, int light) {
        RenderEntityPitchEvent event = new RenderEntityPitchEvent(livingEntity, g);
        event.call();

        if (event.cancelled) {
            return event.result;
        }

        return MathHelper.lerp(g, start, end);
    }

}
