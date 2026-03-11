package net.nekuzaky.sanitycraft.sanity;

public class SanityCalculator {
	private SanityCalculator() {
	}

	public static int computeDelta(SanityEnvironmentSnapshot env, SanityConfig config) {
		int loss = 0;
		int gain = 0;

		if (env.dark()) {
			loss += config.darknessLoss;
		}
		if (env.cave()) {
			loss += config.caveLoss;
		}
		if (env.hostileNearby()) {
			loss += config.hostileLoss;
		}
		if (env.thunderstorm()) {
			loss += config.thunderLoss;
		}
		if (env.deepDark()) {
			loss += config.deepDarkLoss;
		}
		if (env.anomalyNearby()) {
			loss += Math.max(0, config.anomalyLoss);
		}

		if (env.sleeping()) {
			gain += config.sleepGain;
		}
		if (env.villageNearby()) {
			gain += config.villageGain;
		}
		if (env.lightNearby()) {
			gain += config.lightGain;
		}
		if (env.musicNearby()) {
			gain += config.musicGain;
		}
		if (env.ritualSafeZone()) {
			gain += Math.max(0, config.ritualSafeZoneGain);
		}

		float contextMultiplier = 1.0F;
		if (config.contextualDecayEnabled) {
			if (env.night()) {
				contextMultiplier *= Math.max(0.0F, config.nightDecayMultiplier);
			}
			if (env.rain()) {
				contextMultiplier *= Math.max(0.0F, config.rainDecayMultiplier);
			}
			if (env.underground()) {
				contextMultiplier *= Math.max(0.0F, config.undergroundDecayMultiplier);
			}
		}

		int scaledLoss = Math.round(loss * contextMultiplier);
		return gain - scaledLoss;
	}
}
