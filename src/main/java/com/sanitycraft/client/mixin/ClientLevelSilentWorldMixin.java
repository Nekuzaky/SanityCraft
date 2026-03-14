package com.sanitycraft.client.mixin;

import com.sanitycraft.client.events.SanityClientEventState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(net.minecraft.client.multiplayer.ClientLevel.class)
abstract class ClientLevelSilentWorldMixin {
	@Inject(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V", at = @At("HEAD"), cancellable = true)
	private void sanitycraft$suppressParticles(ParticleOptions particleOptions, double x, double y, double z, double xd, double yd, double zd, CallbackInfo ci) {
		if (SanityClientEventState.shouldSuppressWorldParticles()) {
			ci.cancel();
		}
	}

	@Inject(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;ZZDDDDDD)V", at = @At("HEAD"), cancellable = true)
	private void sanitycraft$suppressComplexParticles(ParticleOptions particleOptions, boolean overrideLimiter, boolean alwaysVisible, double x, double y, double z, double xd, double yd, double zd, CallbackInfo ci) {
		if (SanityClientEventState.shouldSuppressWorldParticles()) {
			ci.cancel();
		}
	}

	@Inject(method = "addAlwaysVisibleParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V", at = @At("HEAD"), cancellable = true)
	private void sanitycraft$suppressVisibleParticles(ParticleOptions particleOptions, double x, double y, double z, double xd, double yd, double zd, CallbackInfo ci) {
		if (SanityClientEventState.shouldSuppressWorldParticles()) {
			ci.cancel();
		}
	}

	@Inject(method = "addAlwaysVisibleParticle(Lnet/minecraft/core/particles/ParticleOptions;ZDDDDDD)V", at = @At("HEAD"), cancellable = true)
	private void sanitycraft$suppressForcedVisibleParticles(ParticleOptions particleOptions, boolean overrideLimiter, double x, double y, double z, double xd, double yd, double zd, CallbackInfo ci) {
		if (SanityClientEventState.shouldSuppressWorldParticles()) {
			ci.cancel();
		}
	}
}
