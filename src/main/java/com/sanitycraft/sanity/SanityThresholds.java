package com.sanitycraft.sanity;

import com.sanitycraft.data.config.SanityCraftConfig;
import net.minecraft.util.Mth;

public final class SanityThresholds {
	public static final int MIN_SANITY = 0;
	public static final int MAX_SANITY = 100;
	public static final int DEFAULT_SANITY = 100;

	private SanityThresholds() {
	}

	public static int clamp(int value) {
		return Mth.clamp(value, MIN_SANITY, MAX_SANITY);
	}

	public static Stage resolve(int sanity) {
		return resolve(sanity, null);
	}

	public static Stage resolve(int sanity, SanityCraftConfig config) {
		int value = clamp(sanity);
		int stableMin = config == null ? 76 : config.thresholds.stableMin;
		int uneasyMin = config == null ? 51 : config.thresholds.uneasyMin;
		int disturbedMin = config == null ? 26 : config.thresholds.disturbedMin;
		int fracturedMin = config == null ? 11 : config.thresholds.fracturedMin;
		if (value >= stableMin) {
			return Stage.STABLE;
		}
		if (value >= uneasyMin) {
			return Stage.UNEASY;
		}
		if (value >= disturbedMin) {
			return Stage.DISTURBED;
		}
		if (value >= fracturedMin) {
			return Stage.FRACTURED;
		}
		return Stage.COLLAPSE;
	}

	public enum Stage {
		STABLE,
		UNEASY,
		DISTURBED,
		FRACTURED,
		COLLAPSE
	}
}
