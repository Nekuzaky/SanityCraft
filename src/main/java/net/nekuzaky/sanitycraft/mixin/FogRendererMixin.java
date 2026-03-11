package net.nekuzaky.sanitycraft.mixin;

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
import net.nekuzaky.sanitycraft.client.SanityClientState;
import net.nekuzaky.sanitycraft.sanity.SanityConfig;
import net.nekuzaky.sanitycraft.sanity.SanityManager;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
	private static float sanitycraft$smoothedFogFactor = 1.0F;

	@ModifyArg(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/environment/FogEnvironment;setupFog(Lnet/minecraft/client/renderer/fog/FogData;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/multiplayer/ClientLevel;FLnet/minecraft/client/DeltaTracker;)V"), index = 4)
	private float sanitycraft$adjustFogDistance(float originalRenderDistance) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.level == null) {
			sanitycraft$smoothedFogFactor = 1.0F;
			return originalRenderDistance;
		}

		SanityConfig config = SanityManager.getConfig();
		if (!config.ambientFogEnabled) {
			sanitycraft$smoothedFogFactor = 1.0F;
			return originalRenderDistance;
		}

		int sanity = SanityClientState.getSanity();
		float fear = Mth.clamp((100.0F - sanity) / 100.0F, 0.0F, 1.0F);
		float dreadStyle = config.dreadFogEnabled ? Mth.clamp(config.dreadFogIntensity, 0.0F, 1.5F) : 0.0F;
		float targetFactor = 1.0F - fear * (0.38F + 0.24F * dreadStyle);

		BlockPos pos = player.blockPosition();
		boolean caveLike = !minecraft.level.canSeeSky(pos) && pos.getY() < minecraft.level.getSeaLevel() + 2;
		boolean forestLike = isForestLikeBiome(player);
		boolean night = minecraft.level.getDayTime() % 24000L >= 13000L;

		if (dreadStyle > 0.0F && forestLike) {
			targetFactor -= (0.05F + fear * 0.12F) * dreadStyle;
		}
		if (dreadStyle > 0.0F && config.dreadFogNightBoost && night) {
			targetFactor -= (0.06F + 0.08F * fear) * dreadStyle;
		}

		if (config.cinematicCaveFogEnabled && caveLike) {
			float caveBonus = Mth.clamp(config.cinematicCaveFogBonusAlpha / 70.0F, 0.0F, 0.28F);
			targetFactor -= caveBonus * (0.5F + fear * 0.5F) * (1.0F + dreadStyle * 0.6F);
		}

		if (minecraft.level.isRaining()) {
			targetFactor -= 0.04F + 0.03F * dreadStyle;
		}
		if (minecraft.level.isThundering()) {
			targetFactor -= 0.06F + 0.05F * dreadStyle;
		}

		if (dreadStyle > 0.0F && (sanity <= 70 || caveLike || forestLike)) {
			float pulse = (Mth.sin(player.tickCount * 0.09F) + 1.0F) * 0.5F;
			targetFactor -= pulse * (0.02F + 0.10F * fear) * dreadStyle;
		}

		if (config.torchRepelsFog) {
			targetFactor += computeTorchRepel(player, config) * (0.30F + 0.15F * dreadStyle);
		}

		float minFactor = Mth.lerp(Mth.clamp(dreadStyle, 0.0F, 1.0F), 0.22F, 0.08F);
		targetFactor = Mth.clamp(targetFactor, minFactor, 1.0F);
		float smoothSpeed = 0.06F + Math.min(dreadStyle, 1.0F) * 0.04F;
		sanitycraft$smoothedFogFactor += (targetFactor - sanitycraft$smoothedFogFactor) * smoothSpeed;
		return originalRenderDistance * sanitycraft$smoothedFogFactor;
	}

	private boolean isForestLikeBiome(LocalPlayer player) {
		return player.level().getBiome(player.blockPosition()).unwrapKey().map(key -> {
			String path = key.location().getPath();
			return path.contains("forest") || path.contains("taiga") || path.contains("swamp") || path.contains("dark_forest");
		}).orElse(false);
	}

	private float computeTorchRepel(LocalPlayer player, SanityConfig config) {
		float repel = 0.0F;
		if (isTorchItem(player.getMainHandItem()) || isTorchItem(player.getOffhandItem())) {
			repel += Mth.clamp(config.heldTorchFogRepel, 0.0F, 1.0F);
		}
		if (player.level().getBrightness(LightLayer.BLOCK, player.blockPosition()) >= 10) {
			repel += Mth.clamp(config.nearbyTorchFogRepel, 0.0F, 1.0F);
		}
		return Mth.clamp(repel, 0.0F, 1.0F);
	}

	private boolean isTorchItem(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		return stack.is(Items.TORCH) || stack.is(Items.SOUL_TORCH) || stack.is(Items.REDSTONE_TORCH);
	}
}
