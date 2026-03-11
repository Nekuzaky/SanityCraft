package net.nekuzaky.sanitycraft.sanity;

public class SanityStageResolver {
	private SanityStageResolver() {
	}

	public static SanityStage resolve(int sanity) {
		if (sanity >= 81) {
			return SanityStage.STABLE;
		}
		if (sanity >= 61) {
			return SanityStage.MILD_DISCOMFORT;
		}
		if (sanity >= 41) {
			return SanityStage.UNEASY;
		}
		if (sanity >= 21) {
			return SanityStage.UNSTABLE;
		}
		return SanityStage.SEVERE_BREAKDOWN;
	}
}
