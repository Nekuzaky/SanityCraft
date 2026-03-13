package com.sanitycraft.client.mixin;

import com.sanitycraft.client.effects.SanityVisualProfile;
import com.sanitycraft.client.hud.ClientSanityState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	@Inject(method = "getFov", at = @At("RETURN"), cancellable = true, require = 0)
	private void sanitycraft$applySanityFov(Camera camera, float partialTick, boolean useFovSetting, CallbackInfoReturnable<Float> cir) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.level == null || minecraft.isPaused()) {
			return;
		}

		int sanity = ClientSanityState.getSanity();
		float instability = SanityVisualProfile.getInstabilityStrength(sanity, ClientSanityState.getDistortionFactor());
		if (instability <= 0.0F) {
			return;
		}

		double time = (minecraft.level.getGameTime() + partialTick) * 0.085D;
		float breathing = (float) (Math.sin(time) * (0.18F + instability * 0.52F));
		float secondary = (float) (Math.sin(time * 0.42D + 1.7D) * instability * 0.34F);
		float scarePulse = ClientSanityState.hasScarePulse() ? ClientSanityState.getScarePulseIntensity() * 0.04F : 0.0F;
		float shadowPulse = ClientSanityState.hasShadowFlicker()
				? (1.0F - ClientSanityState.getShadowFlickerProgress()) * 0.35F
				: 0.0F;
		float adjustment = breathing + secondary + scarePulse + shadowPulse;
		cir.setReturnValue(Math.max(30.0F, cir.getReturnValue() + Mth.clamp(adjustment, -1.8F, 2.4F)));
	}
}
