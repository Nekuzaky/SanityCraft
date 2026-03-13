package com.sanitycraft.sanity;

public final class SanityCalculator {
	private SanityCalculator() {
	}

	public static int calculateDelta(SanityTriggers triggers, SanityRecovery recovery) {
		int loss = 0;
		int gain = 0;

		if (triggers.darknessExposure()) {
			loss += 2;
		}
		if (triggers.nearbyHostiles()) {
			loss += 2;
		}
		if (triggers.cursedArea()) {
			loss += 2;
		}
		if (triggers.weatherStress()) {
			loss += 1;
		}

		if (recovery.safeZone()) {
			gain += 2;
		}
		if (recovery.lightSource()) {
			gain += 1;
		}
		if (recovery.daylight()) {
			gain += 1;
		}

		return gain - loss;
	}
}
