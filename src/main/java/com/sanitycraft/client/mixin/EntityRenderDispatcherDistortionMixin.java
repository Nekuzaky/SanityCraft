package com.sanitycraft.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sanitycraft.client.events.SanityHallucinatedEntityManager;
import com.sanitycraft.client.events.SanityClientEventState;
import java.util.ArrayDeque;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderDispatcher.class)
abstract class EntityRenderDispatcherDistortionMixin {
	@Unique
	private static final ThreadLocal<ArrayDeque<Integer>> SANITYCRAFT$DISTORTION_STACK = ThreadLocal.withInitial(ArrayDeque::new);

	@Inject(
			method = "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at = @At("HEAD"))
	private <E extends Entity> void sanitycraft$applyDistortion(E entity, double x, double y, double z, float yaw, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, CallbackInfo ci) {
		int pushes = 0;
		if (SanityClientEventState.pushEntityDistortion(entity, poseStack)) {
			pushes++;
		}
		if (SanityHallucinatedEntityManager.pushRenderTransform(entity, poseStack)) {
			pushes++;
		}
		SANITYCRAFT$DISTORTION_STACK.get().push(pushes);
	}

	@Inject(
			method = "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at = @At("TAIL"))
	private <E extends Entity> void sanitycraft$clearDistortion(E entity, double x, double y, double z, float yaw, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, CallbackInfo ci) {
		ArrayDeque<Integer> stack = SANITYCRAFT$DISTORTION_STACK.get();
		int pushes = stack.isEmpty() ? 0 : stack.pop();
		for (int i = 0; i < pushes; i++) {
			poseStack.popPose();
		}
	}
}
