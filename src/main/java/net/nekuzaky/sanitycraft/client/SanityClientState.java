package net.nekuzaky.sanitycraft.client;

import net.minecraft.client.Minecraft;

public class SanityClientState {
	private static int sanity = 100;
	private static long jumpscareEndMs = 0L;
	private static int jumpscareVariant = 0;
	private static int jumpscareDurationTicks = 0;
	private static int scarePulseTicks = 0;
	private static int scarePulseIntensity = 0;
	private static long nearMissEndMs = 0L;
	private static int nearMissDurationMs = 300;
	private static int nearMissSide = 1;
	private static long zeroSanityStartMs = 0L;

	private SanityClientState() {
	}

	public static int getSanity() {
		return sanity;
	}

	public static void setSanity(int value) {
		int previous = sanity;
		sanity = Math.max(0, Math.min(100, value));
		if (sanity <= 0) {
			if (previous > 0 || zeroSanityStartMs <= 0L) {
				zeroSanityStartMs = System.currentTimeMillis();
			}
		} else {
			zeroSanityStartMs = 0L;
		}
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

	public static void triggerScarePulse(int durationTicks, int intensity) {
		scarePulseTicks = Math.max(scarePulseTicks, Math.max(1, durationTicks));
		scarePulseIntensity = Math.max(scarePulseIntensity, Math.max(1, intensity));
	}

	public static boolean hasScarePulse() {
		return scarePulseTicks > 0;
	}

	public static float getScarePulseFactor() {
		if (scarePulseTicks <= 0) {
			return 0.0F;
		}
		float t = Math.min(1.0F, scarePulseTicks / 24.0F);
		return Math.max(0.0F, Math.min(1.0F, t * (scarePulseIntensity / 8.0F)));
	}

	public static void tickVisualEffects(Minecraft client) {
		if (scarePulseTicks <= 0 || client.player == null) {
			return;
		}
		float factor = getScarePulseFactor();
		float yawJitter = (client.player.getRandom().nextFloat() - 0.5F) * (0.6F + factor * 1.8F);
		float pitchJitter = (client.player.getRandom().nextFloat() - 0.5F) * (0.4F + factor * 1.3F);
		client.player.setYRot(client.player.getYRot() + yawJitter);
		client.player.setYHeadRot(client.player.getYHeadRot() + yawJitter);
		client.player.setXRot(Math.max(-90.0F, Math.min(90.0F, client.player.getXRot() + pitchJitter)));

		scarePulseTicks--;
		if (scarePulseTicks <= 0) {
			scarePulseIntensity = 0;
		}
	}

	public static void triggerNearMiss(int side, int durationMs) {
		nearMissSide = side >= 0 ? 1 : -1;
		nearMissDurationMs = Math.max(80, durationMs);
		nearMissEndMs = System.currentTimeMillis() + nearMissDurationMs;
	}

	public static boolean hasNearMiss() {
		return System.currentTimeMillis() < nearMissEndMs;
	}

	public static float getNearMissProgress() {
		if (!hasNearMiss()) {
			return 1.0F;
		}
		float total = Math.max(80.0F, nearMissDurationMs);
		float remaining = nearMissEndMs - System.currentTimeMillis();
		return 1.0F - Math.max(0.0F, Math.min(1.0F, remaining / total));
	}

	public static int getNearMissSide() {
		return nearMissSide;
	}

	public static boolean isZeroSanityActive() {
		return sanity <= 0;
	}

	public static float getZeroSanityPulse() {
		if (!isZeroSanityActive()) {
			return 0.0F;
		}
		double t = System.currentTimeMillis() / 85.0D;
		return (float) ((Math.sin(t) + 1.0D) * 0.5D);
	}

	public static int getZeroSanityElapsedSeconds() {
		if (!isZeroSanityActive() || zeroSanityStartMs <= 0L) {
			return 0;
		}
		return (int) ((System.currentTimeMillis() - zeroSanityStartMs) / 1000L);
	}
}
