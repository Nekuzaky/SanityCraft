package com.sanitycraft.client.hud;

import com.sanitycraft.sanity.SanityThresholds;

public final class ClientSanityState {
	private static int sanity = SanityThresholds.DEFAULT_SANITY;

	private ClientSanityState() {
	}

	public static int getSanity() {
		return sanity;
	}

	public static void setSanity(int value) {
		sanity = SanityThresholds.clamp(value);
	}
}
