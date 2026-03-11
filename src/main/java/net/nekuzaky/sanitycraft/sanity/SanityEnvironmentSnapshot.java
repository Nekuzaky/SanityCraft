package net.nekuzaky.sanitycraft.sanity;

public record SanityEnvironmentSnapshot(
		boolean dark,
		boolean cave,
		boolean underground,
		boolean hostileNearby,
		boolean night,
		boolean rain,
		boolean thunderstorm,
		boolean deepDark,
		boolean sleeping,
		boolean villageNearby,
		boolean lightNearby,
		boolean musicNearby,
		boolean ritualSafeZone,
		boolean anomalyNearby) {
}
