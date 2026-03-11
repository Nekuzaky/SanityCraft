package net.nekuzaky.sanitycraft.sanity;

import java.util.ArrayList;
import java.util.List;

public class SanityCalculator {
	private SanityCalculator() {
	}

	public static int computeDelta(SanityEnvironmentSnapshot env, SanityConfig config) {
		return computeDeltaResult(env, config).delta();
	}

	public static SanityDeltaResult computeDeltaResult(SanityEnvironmentSnapshot env, SanityConfig config) {
		int loss = 0;
		int gain = 0;
		List<String> lossSources = new ArrayList<>();
		List<String> gainSources = new ArrayList<>();
		List<String> multiplierSources = new ArrayList<>();

		if (env.dark()) {
			loss += config.darknessLoss;
			lossSources.add("dark:" + config.darknessLoss);
		}
		if (env.cave()) {
			loss += config.caveLoss;
			lossSources.add("cave:" + config.caveLoss);
		}
		if (env.hostileNearby()) {
			loss += config.hostileLoss;
			lossSources.add("hostile:" + config.hostileLoss);
		}
		if (env.thunderstorm()) {
			loss += config.thunderLoss;
			lossSources.add("thunder:" + config.thunderLoss);
		}
		if (env.deepDark()) {
			loss += config.deepDarkLoss;
			lossSources.add("deep_dark:" + config.deepDarkLoss);
		}
		if (env.anomalyNearby()) {
			int anomalyLoss = Math.max(0, config.anomalyLoss);
			loss += anomalyLoss;
			lossSources.add("anomaly:" + anomalyLoss);
		}

		if (env.sleeping()) {
			gain += config.sleepGain;
			gainSources.add("sleep:" + config.sleepGain);
		}
		if (env.villageNearby()) {
			gain += config.villageGain;
			gainSources.add("village:" + config.villageGain);
		}
		if (env.lightNearby()) {
			gain += config.lightGain;
			gainSources.add("light:" + config.lightGain);
		}
		if (env.musicNearby()) {
			gain += config.musicGain;
			gainSources.add("music:" + config.musicGain);
		}
		if (env.ritualSafeZone()) {
			int ritualGain = Math.max(0, config.ritualSafeZoneGain);
			gain += ritualGain;
			gainSources.add("ritual:" + ritualGain);
		}

		float contextMultiplier = 1.0F;
		if (config.contextualDecayEnabled) {
			if (env.night()) {
				float mult = Math.max(0.0F, config.nightDecayMultiplier);
				contextMultiplier *= mult;
				multiplierSources.add("night:x" + formatMultiplier(mult));
			}
			if (env.rain()) {
				float mult = Math.max(0.0F, config.rainDecayMultiplier);
				contextMultiplier *= mult;
				multiplierSources.add("rain:x" + formatMultiplier(mult));
			}
			if (env.underground()) {
				float mult = Math.max(0.0F, config.undergroundDecayMultiplier);
				contextMultiplier *= mult;
				multiplierSources.add("underground:x" + formatMultiplier(mult));
			}
		}

		int scaledLoss = Math.round(loss * contextMultiplier);
		int delta = gain - scaledLoss;
		return new SanityDeltaResult(
				gain,
				loss,
				scaledLoss,
				contextMultiplier,
				gainSources.isEmpty() ? "-" : String.join(", ", gainSources),
				lossSources.isEmpty() ? "-" : String.join(", ", lossSources),
				multiplierSources.isEmpty() ? "x1.00" : String.join(", ", multiplierSources),
				delta);
	}

	private static String formatMultiplier(float value) {
		return String.format(java.util.Locale.ROOT, "%.2f", value);
	}
}
