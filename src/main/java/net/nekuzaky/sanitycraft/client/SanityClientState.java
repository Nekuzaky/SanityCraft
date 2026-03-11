package net.nekuzaky.sanitycraft.client;

public class SanityClientState {
	private static int sanity = 100;
	private static long jumpscareEndMs = 0L;
	private static int jumpscareVariant = 0;
	private static int jumpscareDurationTicks = 0;

	private SanityClientState() {
	}

	public static int getSanity() {
		return sanity;
	}

	public static void setSanity(int value) {
		sanity = Math.max(0, Math.min(100, value));
	}

	public static void triggerJumpscare(int variant, int durationTicks) {
		jumpscareVariant = Math.max(0, variant);
		jumpscareDurationTicks = Math.max(1, durationTicks);
		jumpscareEndMs = System.currentTimeMillis() + (long) jumpscareDurationTicks * 50L;
	}

	public static boolean hasActiveJumpscare() {
		return System.currentTimeMillis() < jumpscareEndMs;
	}

	public static float getJumpscareProgress() {
		if (!hasActiveJumpscare()) {
			return 0.0F;
		}
		float totalMs = Math.max(1.0F, jumpscareDurationTicks * 50.0F);
		float remaining = jumpscareEndMs - System.currentTimeMillis();
		return 1.0F - Math.max(0.0F, Math.min(1.0F, remaining / totalMs));
	}

	public static int getJumpscareVariant() {
		return jumpscareVariant;
	}
}
