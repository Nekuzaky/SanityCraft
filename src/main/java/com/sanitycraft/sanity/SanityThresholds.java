package com.sanitycraft.sanity;

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
		int value = clamp(sanity);
		if (value >= 76) {
			return Stage.STABLE;
		}
		if (value >= 51) {
			return Stage.UNEASY;
		}
		if (value >= 26) {
			return Stage.DISTURBED;
		}
		if (value >= 11) {
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
