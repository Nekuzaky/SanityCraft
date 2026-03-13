package com.sanitycraft.sanity;

public record SanityTriggers(
		boolean darknessExposure,
		boolean lowHealth,
		boolean nearbyHostiles,
		boolean undergroundTooLong,
		boolean aloneTooLong,
		boolean nightTime,
		boolean cursedArea,
		boolean disturbingSound,
		boolean witnessedEvent,
		boolean sleepDeprived,
		boolean weatherStress,
		boolean lowFood) {
	public static SanityTriggers empty() {
		return new SanityTriggers(false, false, false, false, false, false, false, false, false, false, false, false);
	}
}
