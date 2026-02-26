package io.github.sst.remake.mixin;

import io.github.sst.remake.event.impl.game.render.RenderEntityRotationsEvent;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>>
        extends EntityRenderer<T> implements FeatureRendererContext<T, M> {

    protected MixinLivingEntityRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Unique
    private boolean hasOverride;
    @Unique
    private float bodyYaw;
    @Unique
    private float yaw;
    @Unique
    private float pitch;

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    private void resetOverride(T entity, float entityYaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, CallbackInfo ci) {
        this.hasOverride = false;
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getPose()Lnet/minecraft/entity/EntityPose;"))
    private void onRotationEvent(T entity, float entityYaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, CallbackInfo ci) {
        float vanillaBodyYaw = MathHelper.lerpAngleDegrees(tickDelta, entity.prevBodyYaw, entity.bodyYaw);

        float vanillaHeadYaw = MathHelper.lerpAngleDegrees(tickDelta, entity.prevHeadYaw, entity.headYaw);

        float vanillaYaw = vanillaHeadYaw - vanillaBodyYaw;

        if (entity.hasVehicle() && entity.getVehicle() instanceof LivingEntity) {

            LivingEntity vehicle = (LivingEntity) entity.getVehicle();

            vanillaBodyYaw = MathHelper.lerpAngleDegrees(tickDelta, vehicle.prevBodyYaw, vehicle.bodyYaw);

            vanillaYaw = vanillaHeadYaw - vanillaBodyYaw;

            float clamped = MathHelper.wrapDegrees(vanillaYaw);

            if (clamped < -85.0F) clamped = -85.0F;
            if (clamped >= 85.0F) clamped = 85.0F;

            vanillaBodyYaw = vanillaHeadYaw - clamped;

            if (clamped * clamped > 2500.0F) {
                vanillaBodyYaw += clamped * 0.2F;
            }

            vanillaYaw = vanillaHeadYaw - vanillaBodyYaw;
        }

        float vanillaPitch = MathHelper.lerp(tickDelta, entity.prevPitch, entity.pitch);

        RenderEntityRotationsEvent event = new RenderEntityRotationsEvent(entity, vanillaYaw, vanillaPitch, vanillaBodyYaw, vanillaHeadYaw, tickDelta);

        event.call();

        this.bodyYaw = event.bodyYaw;
        this.yaw = event.yaw;
        this.pitch = event.pitch;
        this.hasOverride = true;
    }

    @ModifyArgs(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;setupTransforms(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/util/math/MatrixStack;FFF)V"))
    private void modifySetupTransformsArgs(Args args) {
        if (!this.hasOverride) return;
        args.set(3, this.bodyYaw);
    }

    @ModifyArgs(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;setAngles(Lnet/minecraft/entity/Entity;FFFFF)V"))
    private void modifySetAnglesArgs(Args args) {
        if (!this.hasOverride) return;
        args.set(4, this.yaw);
        args.set(5, this.pitch);
    }

    @ModifyArgs(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/FeatureRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/Entity;FFFFFF)V"))
    private void modifyFeatureRendererArgs(Args args) {
        if (!this.hasOverride) return;
        args.set(8, this.yaw);
        args.set(9, this.pitch);
    }
}