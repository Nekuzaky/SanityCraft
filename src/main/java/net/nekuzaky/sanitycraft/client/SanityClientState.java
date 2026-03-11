package net.nekuzaky.sanitycraft.client;

public class SanityClientState {
	private static int sanity = 100;

	private SanityClientState() {
	}

	public static int getSanity() {
		return sanity;
	}

	public static void setSanity(int value) {
		sanity = Math.max(0, Math.min(100, value));
	}
}
