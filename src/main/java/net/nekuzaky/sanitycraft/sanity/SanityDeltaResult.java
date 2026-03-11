package net.nekuzaky.sanitycraft.sanity;

public record SanityDeltaResult(
		int rawGain,
		int rawLoss,
		int scaledLoss,
		float contextMultiplier,
		String gainDetails,
		String lossDetails,
		String multiplierDetails,
		int delta) {
}
