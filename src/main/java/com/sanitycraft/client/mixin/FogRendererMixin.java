package com.sanitycraft.client.mixin;

import com.sanitycraft.client.effects.SanityVisualProfile;
import com.sanitycraft.client.hud.ClientSanityState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
	private static float sanitycraft$smoothedFogFactor = 1.0F;

	@ModifyArg(
			method = "setupFog",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/fog/environment/FogEnvironment;setupFog(Lnet/minecraft/client/renderer/fog/FogData;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/multiplayer/ClientLevel;FLnet/minecraft/client/DeltaTracker;)V"),
			index = 4,
			require = 0)
	private static float sanitycraft$adjustFogDistance(float originalRenderDistance) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.level == null) {
			sanitycraft$smoothedFogFactor = 1.0F;
			return originalRenderDistance;
		}

		int sanity = ClientSanityState.getSanity();
		float distortion = ClientSanityState.getDistortionFactor();
		float fogDensity = SanityVisualProfile.getFogDensity(sanity, distortion);
		if (fogDensity <= 0.0F) {
			sanitycraft$smoothedFogFactor += (1.0F - sanitycraft$smoothedFogFactor) * 0.08F;
			return originalRenderDistance * sanitycraft$smoothedFogFactor;
		}

		BlockPos pos = player.blockPosition();
		int blockLight = minecraft.level.getBrightness(LightLayer.BLOCK, pos);
		float lightRelief = SanityVisualProfile.getTorchRelief(blockLight, holdsTorch(player.getMainHandItem()) || holdsTorch(player.getOffhandItem()));
		float targetFactor = 1.0F - fogDensity * 0.48F;
		if (!minecraft.level.canSeeSky(pos)) {
			targetFactor -= 0.05F + fogDensity * 0.08F;
		}
		if (minecraft.level.isRaining()) {
			targetFactor -= 0.03F;
		}
		if (ClientSanityState.hasShadowFlicker()) {
			targetFactor -= 0.05F;
		}
		targetFactor += lightRelief * 0.12F;
		targetFactor = Mth.clamp(targetFactor, 0.20F, 1.0F);
		sanitycraft$smoothedFogFactor += (targetFactor - sanitycraft$smoothedFogFactor) * (0.05F + fogDensity * 0.06F);
		return originalRenderDistance * sanitycraft$smoothedFogFactor;
	}

	private static boolean holdsTorch(ItemStack stack) {
		return !stack.isEmpty() && (stack.is(Items.TORCH) || stack.is(Items.SOUL_TORCH) || stack.is(Items.REDSTONE_TORCH));
	}
}
