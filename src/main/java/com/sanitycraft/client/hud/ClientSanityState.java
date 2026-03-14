package com.sanitycraft.client.hud;

import com.sanitycraft.data.config.SanityCraftConfig;
import com.sanitycraft.sanity.SanityThresholds;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class ClientSanityState {
	private static final String[] WHISPERS = {
			"it knows your name",
			"something stayed inside",
			"do not trust the walls",
			"your base is not safe",
			"it is waiting behind glass",
			"turn around slowly"
	};

	private static int sanity = SanityThresholds.DEFAULT_SANITY;
	private static int scarePulseTicks;
	private static int scarePulseIntensity;
	private static int ambientShakeTicks;
	private static int ambientShakeIntensity;
	private static long blinkEndMs;
	private static int blinkDurationMs;
	private static long phantomTextEndMs;
	private static int phantomTextDurationMs;
	private static int phantomTextIndex;
	private static int phantomTextX;
	private static int phantomTextY;
	private static long edgeWatcherEndMs;
	private static int edgeWatcherDurationMs;
	private static int edgeWatcherSide = 1;
	private static int edgeWatcherY;
	private static long structureEchoEndMs;
	private static int structureEchoDurationMs;
	private static int structureEchoX;
	private static int structureEchoY;
	private static int structureEchoWidth;
	private static int structureEchoHeight;
	private static int structureEchoVariant;
	private static long shadowFlickerEndMs;
	private static int shadowFlickerDurationMs;
	private static long fakeDamageFlashEndMs;
	private static int fakeDamageFlashDurationMs;
	private static int hudDistortionTicks;
	private static int hudDistortionIntensity;
	private static long collapseSilenceEndMs;
	private static int collapseSilenceDurationMs;
	private static int collapseSilenceIntensity;
	private static long collapseFalseHudEndMs;
	private static int collapseFalseHudDurationMs;
	private static int collapseFalseHudIntensity;
	private static int collapseFalseHudVariant;
	private static long nextSubtitleSeedMs;

	private ClientSanityState() {
	}

	public static int getSanity() {
		return sanity;
	}

	public static void setSanity(int value) {
		sanity = SanityThresholds.clamp(value);
	}

	public static SanityThresholds.Stage getStage() {
		return SanityThresholds.resolve(sanity, SanityCraftConfig.get());
	}

	public static float getFearFactor() {
		return (SanityThresholds.MAX_SANITY - sanity) / 100.0F;
	}

	public static float getDarkenFactor() {
		return switch (getStage()) {
			case STABLE -> 0.0F;
			case UNEASY -> 0.16F + getFearFactor() * 0.08F;
			case DISTURBED -> 0.24F + getFearFactor() * 0.14F;
			case FRACTURED -> 0.32F + getFearFactor() * 0.18F;
			case COLLAPSE -> 0.44F + getFearFactor() * 0.20F;
		};
	}

	public static float getDistortionFactor() {
		float pulse = scarePulseTicks <= 0 ? 0.0F : Math.min(1.0F, scarePulseIntensity / 8.0F);
		return switch (getStage()) {
			case STABLE -> pulse * 0.15F;
			case UNEASY -> 0.08F + pulse * 0.18F;
			case DISTURBED -> 0.18F + pulse * 0.26F;
			case FRACTURED -> 0.32F + pulse * 0.30F;
			case COLLAPSE -> 0.48F + pulse * 0.34F;
		};
	}

	public static float getHudInstability() {
		float base = sanity > 9 ? 0.0F : Mth.clamp(0.08F + (10 - sanity) * 0.07F, 0.0F, 0.78F);
		float burst = hudDistortionTicks <= 0 ? 0.0F : Mth.clamp(hudDistortionIntensity / 10.0F, 0.10F, 0.42F);
		float falseHud = hasCollapseFalseHud() ? Mth.clamp(collapseFalseHudIntensity / 10.0F, 0.08F, 0.34F) : 0.0F;
		return Mth.clamp(base + burst + falseHud, 0.0F, 1.0F);
	}

	public static void triggerScarePulse(int durationTicks, int intensity) {
		scarePulseTicks = Math.max(scarePulseTicks, Math.max(1, durationTicks));
		scarePulseIntensity = Math.max(scarePulseIntensity, Math.max(1, intensity));
	}

	public static void triggerHudDistortion(int durationTicks, int intensity, boolean fakeDamageFlash) {
		hudDistortionTicks = Math.max(hudDistortionTicks, Math.max(1, durationTicks));
		hudDistortionIntensity = Math.max(hudDistortionIntensity, Math.max(1, intensity));
		if (fakeDamageFlash) {
			fakeDamageFlashDurationMs = Math.max(fakeDamageFlashDurationMs, 180);
			fakeDamageFlashEndMs = Math.max(fakeDamageFlashEndMs, System.currentTimeMillis() + fakeDamageFlashDurationMs);
		}
	}

	public static void triggerCollapseSilence(int durationTicks, int intensity) {
		collapseSilenceDurationMs = Math.max(collapseSilenceDurationMs, Math.max(300, durationTicks * 50));
		collapseSilenceEndMs = Math.max(collapseSilenceEndMs, System.currentTimeMillis() + collapseSilenceDurationMs);
		collapseSilenceIntensity = Math.max(collapseSilenceIntensity, Math.max(1, intensity));
	}

	public static void triggerCollapseFalseHud(int durationTicks, int intensity, int variant) {
		collapseFalseHudDurationMs = Math.max(collapseFalseHudDurationMs, Math.max(500, durationTicks * 50));
		collapseFalseHudEndMs = Math.max(collapseFalseHudEndMs, System.currentTimeMillis() + collapseFalseHudDurationMs);
		collapseFalseHudIntensity = Math.max(collapseFalseHudIntensity, Math.max(1, intensity));
		collapseFalseHudVariant = variant;
	}

	public static void tick(Minecraft client) {
		if (client.player == null) {
			clearTransientEffects();
			return;
		}

		tickLocalBursts(client);
		tickCameraEffects(client);
		if (hudDistortionTicks > 0) {
			hudDistortionTicks--;
			if (hudDistortionTicks <= 0) {
				hudDistortionIntensity = 0;
			}
		}
	}

	public static boolean hasScarePulse() {
		return scarePulseTicks > 0;
	}

	public static int getScarePulseIntensity() {
		return scarePulseIntensity;
	}

	public static boolean hasHallucinationBlink() {
		return System.currentTimeMillis() < blinkEndMs;
	}

	public static float getHallucinationBlinkProgress() {
		if (!hasHallucinationBlink()) {
			return 1.0F;
		}
		float total = Math.max(120.0F, blinkDurationMs);
		float remaining = blinkEndMs - System.currentTimeMillis();
		return 1.0F - Math.max(0.0F, Math.min(1.0F, remaining / total));
	}

	public static boolean hasPhantomText() {
		return System.currentTimeMillis() < phantomTextEndMs;
	}

	public static float getPhantomTextProgress() {
		if (!hasPhantomText()) {
			return 1.0F;
		}
		float total = Math.max(600.0F, phantomTextDurationMs);
		float remaining = phantomTextEndMs - System.currentTimeMillis();
		return 1.0F - Math.max(0.0F, Math.min(1.0F, remaining / total));
	}

	public static String getPhantomText() {
		return WHISPERS[Math.floorMod(phantomTextIndex, WHISPERS.length)];
	}

	public static int getPhantomTextX() {
		return phantomTextX;
	}

	public static int getPhantomTextY() {
		return phantomTextY;
	}

	public static boolean hasEdgeWatcher() {
		return System.currentTimeMillis() < edgeWatcherEndMs;
	}

	public static float getEdgeWatcherProgress() {
		if (!hasEdgeWatcher()) {
			return 1.0F;
		}
		float total = Math.max(160.0F, edgeWatcherDurationMs);
		float remaining = edgeWatcherEndMs - System.currentTimeMillis();
		return 1.0F - Math.max(0.0F, Math.min(1.0F, remaining / total));
	}

	public static int getEdgeWatcherSide() {
		return edgeWatcherSide;
	}

	public static int getEdgeWatcherY() {
		return edgeWatcherY;
	}

	public static boolean hasStructureEcho() {
		return System.currentTimeMillis() < structureEchoEndMs;
	}

	public static float getStructureEchoProgress() {
		if (!hasStructureEcho()) {
			return 1.0F;
		}
		float total = Math.max(200.0F, structureEchoDurationMs);
		float remaining = structureEchoEndMs - System.currentTimeMillis();
		return 1.0F - Math.max(0.0F, Math.min(1.0F, remaining / total));
	}

	public static int getStructureEchoX() {
		return structureEchoX;
	}

	public static int getStructureEchoY() {
		return structureEchoY;
	}

	public static int getStructureEchoWidth() {
		return structureEchoWidth;
	}

	public static int getStructureEchoHeight() {
		return structureEchoHeight;
	}

	public static int getStructureEchoVariant() {
		return structureEchoVariant;
	}

	public static boolean hasShadowFlicker() {
		return System.currentTimeMillis() < shadowFlickerEndMs;
	}

	public static float getShadowFlickerProgress() {
		if (!hasShadowFlicker()) {
			return 1.0F;
		}
		float total = Math.max(90.0F, shadowFlickerDurationMs);
		float remaining = shadowFlickerEndMs - System.currentTimeMillis();
		return 1.0F - Math.max(0.0F, Math.min(1.0F, remaining / total));
	}

	public static boolean hasFakeDamageFlash() {
		return System.currentTimeMillis() < fakeDamageFlashEndMs;
	}

	public static float getFakeDamageFlashStrength() {
		if (!hasFakeDamageFlash()) {
			return 0.0F;
		}
		float total = Math.max(90.0F, fakeDamageFlashDurationMs);
		float remaining = fakeDamageFlashEndMs - System.currentTimeMillis();
		float progress = 1.0F - Math.max(0.0F, Math.min(1.0F, remaining / total));
		float envelope = progress < 0.28F ? progress / 0.28F : (1.0F - progress) / 0.72F;
		return Mth.clamp(envelope, 0.0F, 1.0F);
	}

	public static boolean hasCollapseSilence() {
		return System.currentTimeMillis() < collapseSilenceEndMs;
	}

	public static float getCollapseSilenceStrength() {
		if (!hasCollapseSilence()) {
			return 0.0F;
		}
		float total = Math.max(240.0F, collapseSilenceDurationMs);
		float remaining = collapseSilenceEndMs - System.currentTimeMillis();
		float progress = 1.0F - Math.max(0.0F, Math.min(1.0F, remaining / total));
		float envelope = progress < 0.22F ? progress / 0.22F : (1.0F - progress) / 0.78F;
		return Mth.clamp(envelope * (0.35F + collapseSilenceIntensity * 0.16F), 0.0F, 1.0F);
	}

	public static boolean hasCollapseFalseHud() {
		return System.currentTimeMillis() < collapseFalseHudEndMs;
	}

	public static int getFalseHeartUnits(int actualUnits, int maxUnits) {
		if (!hasCollapseFalseHud()) {
			return actualUnits;
		}
		float wave = (float) Math.sin(System.currentTimeMillis() / 120.0D + collapseFalseHudVariant * 0.65D);
		int offset = Math.round(wave * Math.max(1.0F, collapseFalseHudIntensity * 0.8F));
		if (((System.currentTimeMillis() / 180L) + collapseFalseHudVariant) % 3L == 0L) {
			offset -= 1;
		}
		return Mth.clamp(actualUnits + offset, 0, maxUnits);
	}

	public static int getFalseFoodUnits(int actualUnits, int maxUnits) {
		if (!hasCollapseFalseHud()) {
			return actualUnits;
		}
		float wave = (float) Math.cos(System.currentTimeMillis() / 95.0D + collapseFalseHudVariant * 0.9D);
		int offset = Math.round(wave * Math.max(1.0F, collapseFalseHudIntensity * 0.7F)) - 1;
		return Mth.clamp(actualUnits + offset, 0, maxUnits);
	}

	public static float getFalseXpProgress(float actualProgress) {
		if (!hasCollapseFalseHud()) {
			return actualProgress;
		}
		float wave = (float) Math.sin(System.currentTimeMillis() / 140.0D + collapseFalseHudVariant * 0.4D) * (0.10F + collapseFalseHudIntensity * 0.035F);
		float snap = (((System.currentTimeMillis() / 160L) + collapseFalseHudVariant) & 3L) == 0L ? -0.15F : 0.0F;
		return Mth.clamp(actualProgress + wave + snap, 0.0F, 1.0F);
	}

	public static int getFalseHotbarSlot() {
		if (!hasCollapseFalseHud()) {
			return -1;
		}
		return Math.floorMod(collapseFalseHudVariant + (int) (System.currentTimeMillis() / 170L), 9);
	}

	public static boolean shouldRefreshSubtitle() {
		long now = System.currentTimeMillis();
		if (now < nextSubtitleSeedMs) {
			return false;
		}
		nextSubtitleSeedMs = now + 2600L + (long) Math.max(0, 100 - sanity) * 12L;
		return true;
	}

	private static void tickLocalBursts(Minecraft client) {
		RandomSource random = client.player.getRandom();
		SanityThresholds.Stage stage = getStage();
		long now = System.currentTimeMillis();
		int width = Math.max(320, client.getWindow().getGuiScaledWidth());
		int height = Math.max(180, client.getWindow().getGuiScaledHeight());

		if (stage.ordinal() >= SanityThresholds.Stage.DISTURBED.ordinal() && ambientShakeTicks <= 0 && random.nextFloat() < 0.012F) {
			ambientShakeTicks = 4 + random.nextInt(stage == SanityThresholds.Stage.COLLAPSE ? 10 : 6);
			ambientShakeIntensity = stage == SanityThresholds.Stage.COLLAPSE ? 3 : stage == SanityThresholds.Stage.FRACTURED ? 2 : 1;
		}
		if (stage.ordinal() >= SanityThresholds.Stage.DISTURBED.ordinal() && now >= blinkEndMs && random.nextFloat() < (stage == SanityThresholds.Stage.DISTURBED ? 0.0032F : 0.0058F)) {
			blinkDurationMs = 140 + random.nextInt(stage == SanityThresholds.Stage.COLLAPSE ? 260 : 160);
			blinkEndMs = now + blinkDurationMs;
		}
		if (stage.ordinal() >= SanityThresholds.Stage.FRACTURED.ordinal() && now >= structureEchoEndMs && random.nextFloat() < (stage == SanityThresholds.Stage.COLLAPSE ? 0.0065F : 0.0038F)) {
			structureEchoDurationMs = 240 + random.nextInt(260);
			structureEchoEndMs = now + structureEchoDurationMs;
			structureEchoWidth = 46 + random.nextInt(36);
			structureEchoHeight = 74 + random.nextInt(48);
			structureEchoX = 32 + random.nextInt(Math.max(32, width - structureEchoWidth - 64));
			structureEchoY = 34 + random.nextInt(Math.max(32, height - structureEchoHeight - 70));
			structureEchoVariant = random.nextInt(3);
		}
		if (stage.ordinal() >= SanityThresholds.Stage.DISTURBED.ordinal() && now >= edgeWatcherEndMs && random.nextFloat() < (stage == SanityThresholds.Stage.COLLAPSE ? 0.006F : 0.003F)) {
			edgeWatcherDurationMs = 240 + random.nextInt(stage == SanityThresholds.Stage.COLLAPSE ? 320 : 200);
			edgeWatcherEndMs = now + edgeWatcherDurationMs;
			edgeWatcherSide = random.nextBoolean() ? 1 : -1;
			edgeWatcherY = 42 + random.nextInt(Math.max(42, height - 160));
		}
		if (stage.ordinal() >= SanityThresholds.Stage.DISTURBED.ordinal() && now >= shadowFlickerEndMs && random.nextFloat() < (stage == SanityThresholds.Stage.COLLAPSE ? 0.0062F : stage == SanityThresholds.Stage.FRACTURED ? 0.0044F : 0.0023F)) {
			shadowFlickerDurationMs = 90 + random.nextInt(stage == SanityThresholds.Stage.COLLAPSE ? 210 : 120);
			shadowFlickerEndMs = now + shadowFlickerDurationMs;
		}
		if (stage.ordinal() >= SanityThresholds.Stage.FRACTURED.ordinal() && now >= phantomTextEndMs && random.nextFloat() < (stage == SanityThresholds.Stage.COLLAPSE ? 0.0055F : 0.0035F)) {
			phantomTextDurationMs = 900 + random.nextInt(stage == SanityThresholds.Stage.COLLAPSE ? 1800 : 1200);
			phantomTextEndMs = now + phantomTextDurationMs;
			phantomTextIndex = random.nextInt(WHISPERS.length);
			phantomTextX = 18 + random.nextInt(Math.max(24, width - 180));
			phantomTextY = 20 + random.nextInt(Math.max(24, height - 80));
		}
		if (sanity <= 4 && now >= fakeDamageFlashEndMs && random.nextFloat() < 0.0042F) {
			fakeDamageFlashDurationMs = 120 + random.nextInt(120);
			fakeDamageFlashEndMs = now + fakeDamageFlashDurationMs;
		}
	}

	private static void tickCameraEffects(Minecraft client) {
		if (client.player == null) {
			return;
		}

		int jitterTicks = scarePulseTicks;
		int jitterIntensity = scarePulseIntensity;
		if (ambientShakeTicks > 0) {
			jitterTicks = Math.max(jitterTicks, 1);
			jitterIntensity = Math.max(jitterIntensity, ambientShakeIntensity);
			ambientShakeTicks--;
		}
		if (jitterTicks <= 0) {
			return;
		}

		float intensity = Math.max(0.0F, Math.min(1.0F, jitterIntensity / 8.0F));
		float yawJitter = (client.player.getRandom().nextFloat() - 0.5F) * (0.18F + intensity * 1.2F);
		float pitchJitter = (client.player.getRandom().nextFloat() - 0.5F) * (0.10F + intensity * 0.85F);
		client.player.setYRot(client.player.getYRot() + yawJitter);
		client.player.setYHeadRot(client.player.getYHeadRot() + yawJitter);
		client.player.setXRot(Math.max(-90.0F, Math.min(90.0F, client.player.getXRot() + pitchJitter)));

		if (scarePulseTicks > 0) {
			scarePulseTicks--;
			if (scarePulseTicks <= 0) {
				scarePulseIntensity = 0;
			}
		}
	}

	private static void clearTransientEffects() {
		ambientShakeTicks = 0;
		ambientShakeIntensity = 0;
		scarePulseTicks = 0;
		scarePulseIntensity = 0;
		blinkEndMs = 0L;
		phantomTextEndMs = 0L;
		edgeWatcherEndMs = 0L;
		structureEchoEndMs = 0L;
		shadowFlickerEndMs = 0L;
		fakeDamageFlashEndMs = 0L;
		hudDistortionTicks = 0;
		hudDistortionIntensity = 0;
		collapseSilenceEndMs = 0L;
		collapseSilenceDurationMs = 0;
		collapseSilenceIntensity = 0;
		collapseFalseHudEndMs = 0L;
		collapseFalseHudDurationMs = 0;
		collapseFalseHudIntensity = 0;
		collapseFalseHudVariant = 0;
	}
}
