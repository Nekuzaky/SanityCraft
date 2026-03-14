package com.sanitycraft.sanity.events;

public enum SanityEventStage {
	STABLE,
	UNEASY,
	DISTURBED,
	FRACTURED,
	COLLAPSE;

	public static SanityEventStage resolve(int sanity) {
		if (sanity > 70) {
			return STABLE;
		}
		if (sanity > 40) {
			return UNEASY;
		}
		if (sanity > 20) {
			return DISTURBED;
		}
		if (sanity > 5) {
			return FRACTURED;
		}
		return COLLAPSE;
	}
}
