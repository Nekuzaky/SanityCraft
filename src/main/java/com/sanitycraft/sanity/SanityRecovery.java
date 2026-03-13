package com.sanitycraft.sanity;

public record SanityRecovery(
		boolean safeZone,
		boolean lightSource,
		boolean rest,
		boolean medication,
		boolean protectionCharm,
		boolean protectedBlock,
		boolean daylight,
		boolean clearWeather) {
	public static SanityRecovery empty() {
		return new SanityRecovery(false, false, false, false, false, false, false, false);
	}
}
