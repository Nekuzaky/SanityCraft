package com.sanitycraft.sanity;

import com.sanitycraft.data.config.SanityCraftConfig;

public final class SanityCalculator {
	private SanityCalculator() {
	}

	public static int calculateDelta(SanityComponent component, SanityTriggers triggers, SanityRecovery recovery, SanityCraftConfig config) {
		int loss = 0;
		int gain = 0;

		if (triggers.darknessExposure()) {
			loss += config.decay.darknessLoss;
		}
		if (triggers.nearbyHostiles()) {
			loss += config.decay.hostileLoss;
		}
		if (triggers.isolation()) {
			loss += config.decay.isolationLoss;
		}
		if (triggers.undergroundExposure()) {
			loss += config.decay.undergroundLoss;
		}
		if (triggers.nightTime()) {
			loss += config.decay.nightLoss;
		}
		if (triggers.lowHealth()) {
			loss += config.decay.lowHealthLoss;
		}
		if (triggers.lowFood()) {
			loss += config.decay.lowFoodLoss;
		}
		if (triggers.cursedArea()) {
			loss += config.decay.hostileLoss;
		}
		if (triggers.weatherStress()) {
			loss += config.decay.weatherLoss;
		}
		if (loss > 0) {
			SanityThresholds.Stage stage = SanityThresholds.resolve(component.getSanity(), config);
			if (stage == SanityThresholds.Stage.DISTURBED) {
				loss += config.decay.disturbedStagePenalty;
			} else if (stage == SanityThresholds.Stage.FRACTURED) {
				loss += config.decay.fracturedStagePenalty;
			} else if (stage == SanityThresholds.Stage.COLLAPSE) {
				loss += config.decay.collapseStagePenalty;
			}
		}

		if (recovery.safeZone()) {
			gain += config.recovery.safeZoneRecovery;
		}
		if (recovery.lightSource()) {
			gain += config.recovery.lightRecovery;
		}
		if (recovery.daylight()) {
			gain += config.recovery.dayRecovery;
		}

		return gain - loss;
	}
}
