package net.nekuzaky.sanitycraft.sanity;

public record SanityEnvironmentSnapshot(
		boolean dark,
		boolean cave,
		boolean hostileNearby,
		boolean thunderstorm,
		boolean deepDark,
		boolean sleeping,
		boolean villageNearby,
		boolean lightNearby,
		boolean musicNearby) {
}
