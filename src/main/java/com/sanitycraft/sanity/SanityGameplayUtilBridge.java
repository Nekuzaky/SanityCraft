package com.sanitycraft.sanity;

import net.minecraft.util.RandomSource;

public final class SanityGameplayUtilBridge {
	private SanityGameplayUtilBridge() {
	}

	public static int jitter(int baseTicks, RandomSource random, int minimumTicks) {
		int safeBase = Math.max(1, baseTicks);
		int variance = Math.max(1, safeBase / 4);
		return Math.max(minimumTicks, safeBase - variance + random.nextInt(variance * 2 + 1));
	}
}
