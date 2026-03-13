package com.sanitycraft.data.profile;

import com.sanitycraft.sanity.SanityDifficultyProfile;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public final class DifficultyProfiles {
	private DifficultyProfiles() {
	}

	public static Optional<SanityDifficultyProfile> parse(String raw) {
		if (raw == null || raw.isBlank()) {
			return Optional.empty();
		}
		String normalized = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_');
		return Arrays.stream(SanityDifficultyProfile.values()).filter(profile -> profile.name().equals(normalized)).findFirst();
	}

	public static String ids() {
		return Arrays.stream(SanityDifficultyProfile.values())
				.map(profile -> profile.name().toLowerCase(Locale.ROOT).replace('_', '-'))
				.collect(Collectors.joining(", "));
	}
}
