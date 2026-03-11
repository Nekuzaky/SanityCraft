package net.nekuzaky.sanitycraft.sanity;

public class SanityCalculator {
	private SanityCalculator() {
	}

	public static int computeDelta(SanityEnvironmentSnapshot env, SanityConfig config) {
		int delta = 0;

		if (env.dark()) {
			delta -= config.darknessLoss;
		}
		if (env.cave()) {
			delta -= config.caveLoss;
		}
		if (env.hostileNearby()) {
			delta -= config.hostileLoss;
		}
		if (env.thunderstorm()) {
			delta -= config.thunderLoss;
		}
		if (env.deepDark()) {
			delta -= config.deepDarkLoss;
		}

		if (env.sleeping()) {
			delta += config.sleepGain;
		}
		if (env.villageNearby()) {
			delta += config.villageGain;
		}
		if (env.lightNearby()) {
			delta += config.lightGain;
		}
		if (env.musicNearby()) {
			delta += config.musicGain;
		}

		return delta;
	}
}
