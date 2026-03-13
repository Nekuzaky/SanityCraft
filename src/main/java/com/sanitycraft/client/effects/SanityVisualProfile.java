package com.sanitycraft.client.effects;

import net.minecraft.util.Mth;

public final class SanityVisualProfile {
	private SanityVisualProfile() {
	}

	public static float getFogDensity(int sanity, float distortion) {
		float density;
		if (sanity > 70) {
			density = 0.0F;
		} else if (sanity > 40) {
			density = range(sanity, 70, 40, 0.12F, 0.34F);
		} else if (sanity > 20) {
			density = range(sanity, 40, 20, 0.38F, 0.72F);
		} else {
			density = range(sanity, 20, 0, 0.78F, 1.0F);
		}
		return Mth.clamp(density + distortion * 0.12F, 0.0F, 1.0F);
	}

	public static float getVignetteStrength(int sanity, float distortion) {
		float strength;
		if (sanity > 70) {
			strength = 0.0F;
		} else if (sanity > 40) {
			strength = range(sanity, 70, 40, 0.12F, 0.34F);
		} else if (sanity > 20) {
			strength = range(sanity, 40, 20, 0.40F, 0.66F);
		} else {
			strength = range(sanity, 20, 0, 0.68F, 1.0F);
		}
		return Mth.clamp(strength + distortion * 0.08F, 0.0F, 1.0F);
	}

	public static float getGrainStrength(int sanity, float distortion) {
		if (sanity > 40) {
			return 0.0F;
		}
		float strength = sanity > 20
				? range(sanity, 40, 20, 0.18F, 0.58F)
				: range(sanity, 20, 0, 0.62F, 1.0F);
		return Mth.clamp(strength + distortion * 0.12F, 0.0F, 1.0F);
	}

	public static float getChromaticStrength(int sanity, float distortion) {
		if (sanity > 40) {
			return 0.0F;
		}
		float strength = sanity > 20
				? range(sanity, 40, 20, 0.14F, 0.48F)
				: range(sanity, 20, 0, 0.52F, 1.0F);
		return Mth.clamp(strength + distortion * 0.18F, 0.0F, 1.0F);
	}

	public static float getInstabilityStrength(int sanity, float distortion) {
		if (sanity > 40) {
			return 0.0F;
		}
		float strength = sanity > 20
				? range(sanity, 40, 20, 0.06F, 0.22F)
				: range(sanity, 20, 0, 0.30F, 1.0F);
		return Mth.clamp(strength + distortion * 0.20F, 0.0F, 1.0F);
	}

	public static float getAmbientDarkness(int sanity, float distortion) {
		float darkness;
		if (sanity > 70) {
			darkness = 0.0F;
		} else if (sanity > 40) {
			darkness = range(sanity, 70, 40, 0.10F, 0.28F);
		} else if (sanity > 20) {
			darkness = range(sanity, 40, 20, 0.32F, 0.56F);
		} else {
			darkness = range(sanity, 20, 0, 0.60F, 0.90F);
		}
		return Mth.clamp(darkness + distortion * 0.12F, 0.0F, 1.0F);
	}

	public static float getTorchRelief(float blockLight, boolean holdingTorch) {
		float relief = Mth.clamp(blockLight / 15.0F, 0.0F, 1.0F) * 0.65F;
		if (holdingTorch) {
			relief = Math.max(relief, 0.85F);
		}
		return relief;
	}

	private static float range(int sanity, int upper, int lower, float highValue, float lowValue) {
		if (upper <= lower) {
			return lowValue;
		}
		float progress = (upper - sanity) / (float) (upper - lower);
		return Mth.lerp(Mth.clamp(progress, 0.0F, 1.0F), highValue, lowValue);
	}
}
