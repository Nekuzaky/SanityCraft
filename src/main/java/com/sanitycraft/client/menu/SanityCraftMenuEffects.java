package com.sanitycraft.client.menu;

import com.sanitycraft.client.hud.ClientSanityState;
import com.sanitycraft.network.sync.MenuTestType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public final class SanityCraftMenuEffects {
	private static final RandomSource RANDOM = RandomSource.create();
	private static final String[] SUBTITLE_KEYS = {
			"sanitycraft.menu.creepy.leave_lights_on",
			"sanitycraft.menu.creepy.you_were_followed",
			"sanitycraft.menu.creepy.the_forest_remembers",
			"sanitycraft.menu.creepy.something_watched_you",
			"sanitycraft.menu.creepy.the_house_notices",
			"sanitycraft.menu.creepy.the_path_went_still",
			"sanitycraft.menu.creepy.nothing_waits_at_the_road",
			"sanitycraft.menu.creepy.the_trees_keep_their_distance"
	};
	private static final String[] PLAYER_SUBTITLE_KEYS = {
			"sanitycraft.menu.player.welcome_back",
			"sanitycraft.menu.player.leave_lights_on",
			"sanitycraft.menu.player.it_remembers_you",
			"sanitycraft.menu.player.you_are_still_here"
	};
	private static final String[] PLAY_GLITCH_KEYS = {
			"sanitycraft.menu.glitch.play.stay",
			"sanitycraft.menu.glitch.play.dont",
			"sanitycraft.menu.glitch.play.look",
			"sanitycraft.menu.glitch.play.continue"
	};
	private static final String[] PHANTOM_BUTTON_KEYS = {
			"sanitycraft.menu.phantom_button.continue",
			"sanitycraft.menu.phantom_button.return",
			"sanitycraft.menu.phantom_button.resume"
	};
	private static final String[] SUBTITLE_LIE_KEYS = {
			"sanitycraft.menu.creepy.you_were_followed",
			"sanitycraft.menu.creepy.it_is_inside",
			"sanitycraft.menu.creepy.the_path_went_still"
	};
	private static final SoundEvent[] AMBIENT_LAYER_SOUNDS = {
			SoundEvents.AMBIENT_CAVE.value(),
			SoundEvents.WARDEN_NEARBY_CLOSEST,
			SoundEvents.AZALEA_LEAVES_STEP,
			SoundEvents.WET_GRASS_STEP
	};
	private static final SoundEvent[] DISTORTION_LAYER_SOUNDS = {
			SoundEvents.WITCH_AMBIENT,
			SoundEvents.WARDEN_SNIFF,
			SoundEvents.PHANTOM_AMBIENT
	};
	private static final long TEXT_LIE_MIN_DURATION_MS = 42L;
	private static final long TEXT_LIE_MAX_DURATION_MS = 82L;
	private static final long PLAYER_NAME_MIN_DURATION_MS = 1800L;
	private static final long PLAYER_NAME_MAX_DURATION_MS = 3400L;
	private static final long PHANTOM_BUTTON_MIN_DURATION_MS = 380L;
	private static final long PHANTOM_BUTTON_MAX_DURATION_MS = 720L;
	private static final long LOGO_FLICKER_MIN_DURATION_MS = 28L;
	private static final long LOGO_FLICKER_MAX_DURATION_MS = 44L;
	private static boolean active;
	private static Component currentSubtitle = Component.translatable(SUBTITLE_KEYS[0]);
	private static Component subtitleOverride;
	private static long subtitleOverrideEndMs;
	private static Component playLabel = Component.translatable("sanitycraft.menu.play");
	private static long playLabelOverrideEndMs;
	private static Component phantomButtonLabel = Component.translatable(PHANTOM_BUTTON_KEYS[0]);
	private static boolean phantomButtonVisible;
	private static int phantomButtonRow;
	private static boolean phantomButtonBetweenRows;
	private static int phantomButtonXOffset;
	private static long phantomButtonEndMs;
	private static float logoBrightness = 1.0F;
	private static long logoFlickerEndMs;
	private static long nextLogoFlickerAtMs;
	private static long nextSubtitleRotateAtMs;
	private static long nextPlayerSubtitleAtMs;
	private static long nextPlayLieAtMs;
	private static long nextPhantomButtonAtMs;
	private static long nextHorizontalGlitchAtMs;
	private static long horizontalGlitchEndMs;
	private static int horizontalGlitchShiftPx;
	private static float distortionBoost;
	private static long distortionBoostEndMs;
	private static long nextAmbientLayerAtMs;
	private static long nextDistortionLayerAtMs;
	private static long nextDropoutAtMs;
	private static long dropoutEndMs;
	private static float appliedMusicVolume = -1.0F;
	private static boolean initialized;

	private SanityCraftMenuEffects() {
	}

	public static void activate(Minecraft client) {
		if (client == null) {
			return;
		}
		active = true;
		initializeSchedules();
	}

	public static void deactivate(Minecraft client) {
		active = false;
		restoreMusicVolume(client);
	}

	public static void tick(Minecraft client) {
		if (client == null) {
			return;
		}

		initializeSchedules();
		long now = System.currentTimeMillis();
		expireTransientState(now);
		if (!active) {
			restoreMusicVolume(client);
			return;
		}

		if (now >= nextSubtitleRotateAtMs) {
			rotateSubtitle(now);
		}
		if (now >= nextPlayerSubtitleAtMs) {
			maybeShowPlayerSubtitle(client, now, false);
		}
		if (now >= nextPlayLieAtMs) {
			maybeTriggerTextLie(client, now, false);
		}
		if (now >= nextPhantomButtonAtMs) {
			maybeShowPhantomButton(now, false);
		}
		if (now >= nextLogoFlickerAtMs) {
			triggerLogoFlicker(now);
		}
		if (now >= nextHorizontalGlitchAtMs) {
			maybeTriggerHorizontalGlitch(now, false);
		}
		if (now >= nextAmbientLayerAtMs) {
			playAmbientLayer(client, now);
		}
		if (now >= nextDistortionLayerAtMs) {
			playDistortionLayer(client, now);
		}
		if (now >= nextDropoutAtMs) {
			maybeStartDropout(now);
		}
		updateMusicDistortion(client, now);
	}

	public static Component getSubtitle() {
		return System.currentTimeMillis() < subtitleOverrideEndMs && subtitleOverride != null ? subtitleOverride : currentSubtitle;
	}

	public static Component getPlayLabel() {
		return System.currentTimeMillis() < playLabelOverrideEndMs ? playLabel : Component.translatable("sanitycraft.menu.play");
	}

	public static void normalizePlayLabel() {
		playLabelOverrideEndMs = 0L;
		playLabel = Component.translatable("sanitycraft.menu.play");
		schedulePlayLie(System.currentTimeMillis());
	}

	public static float getLogoBrightness() {
		return logoBrightness;
	}

	public static float getDistortionStrength() {
		float sanityPressure = getSanityPressure();
		return Mth.clamp(0.06F + sanityPressure * 0.24F + distortionBoost, 0.0F, 0.64F);
	}

	public static boolean hasPhantomButton() {
		return phantomButtonVisible && System.currentTimeMillis() < phantomButtonEndMs;
	}

	public static Component getPhantomButtonLabel() {
		return phantomButtonLabel;
	}

	public static int getPhantomButtonRow() {
		return phantomButtonRow;
	}

	public static boolean isPhantomButtonBetweenRows() {
		return phantomButtonBetweenRows;
	}

	public static int getPhantomButtonXOffset() {
		return phantomButtonXOffset;
	}

	public static void dismissPhantomButton() {
		phantomButtonVisible = false;
		phantomButtonEndMs = 0L;
		schedulePhantomButton(System.currentTimeMillis());
	}

	public static boolean hasHorizontalGlitch() {
		return System.currentTimeMillis() < horizontalGlitchEndMs;
	}

	public static int getHorizontalGlitchShift() {
		return horizontalGlitchShiftPx;
	}

	public static void triggerDebug(MenuTestType testType) {
		long now = System.currentTimeMillis();
		Minecraft client = Minecraft.getInstance();
		switch (testType) {
			case FLICKER -> triggerLogoFlicker(now);
			case PHANTOM_BUTTON -> showPhantomButton(now);
			case DISTORTION -> triggerDistortion(now);
			case TEXT_LIE -> triggerTextLie(client, now);
			case PLAYER_NAME -> showPlayerSubtitle(client, now);
		}
	}

	private static void initializeSchedules() {
		if (initialized) {
			return;
		}
		initialized = true;
		long now = System.currentTimeMillis();
		scheduleSubtitleRotation(now - 1L);
		schedulePlayerSubtitle(now);
		schedulePlayLie(now);
		schedulePhantomButton(now);
		scheduleLogoFlicker(now);
		scheduleHorizontalGlitch(now);
		scheduleAmbientLayer(now);
		scheduleDistortionLayer(now);
		scheduleDropout(now);
	}

	private static void expireTransientState(long now) {
		if (now >= subtitleOverrideEndMs) {
			subtitleOverride = null;
			subtitleOverrideEndMs = 0L;
		}
		if (now >= playLabelOverrideEndMs) {
			playLabel = Component.translatable("sanitycraft.menu.play");
			playLabelOverrideEndMs = 0L;
		}
		if (now >= phantomButtonEndMs) {
			phantomButtonVisible = false;
			phantomButtonEndMs = 0L;
		}
		if (now >= logoFlickerEndMs) {
			logoBrightness = 1.0F;
			logoFlickerEndMs = 0L;
		}
		if (now >= horizontalGlitchEndMs) {
			horizontalGlitchShiftPx = 0;
			horizontalGlitchEndMs = 0L;
		}
		if (now >= distortionBoostEndMs) {
			distortionBoost = 0.0F;
			distortionBoostEndMs = 0L;
		}
		if (now >= dropoutEndMs) {
			dropoutEndMs = 0L;
		}
	}

	private static void rotateSubtitle(long now) {
		currentSubtitle = Component.translatable(SUBTITLE_KEYS[RANDOM.nextInt(SUBTITLE_KEYS.length)]);
		scheduleSubtitleRotation(now);
	}

	private static void maybeShowPlayerSubtitle(Minecraft client, long now, boolean forced) {
		if (!forced && RANDOM.nextFloat() > 0.16F) {
			schedulePlayerSubtitle(now);
			return;
		}
		showPlayerSubtitle(client, now);
	}

	private static void showPlayerSubtitle(Minecraft client, long now) {
		String playerName = client != null && client.getUser() != null
				? client.getUser().getName()
				: Component.translatable("sanitycraft.menu.player.generic").getString();
		String key = PLAYER_SUBTITLE_KEYS[RANDOM.nextInt(PLAYER_SUBTITLE_KEYS.length)];
		subtitleOverride = Component.translatable(key, playerName);
		subtitleOverrideEndMs = now + PLAYER_NAME_MIN_DURATION_MS + RANDOM.nextInt((int) (PLAYER_NAME_MAX_DURATION_MS - PLAYER_NAME_MIN_DURATION_MS));
		schedulePlayerSubtitle(now);
	}

	private static void maybeTriggerTextLie(Minecraft client, long now, boolean forced) {
		if (!forced && RANDOM.nextFloat() > 0.14F) {
			schedulePlayLie(now);
			return;
		}
		triggerTextLie(client, now);
	}

	private static void triggerTextLie(Minecraft client, long now) {
		playLabel = Component.translatable(pickPlayLieKey());
		playLabelOverrideEndMs = now + TEXT_LIE_MIN_DURATION_MS + RANDOM.nextInt((int) (TEXT_LIE_MAX_DURATION_MS - TEXT_LIE_MIN_DURATION_MS + 1));
		String subtitleKey = SUBTITLE_LIE_KEYS[RANDOM.nextInt(SUBTITLE_LIE_KEYS.length)];
		subtitleOverride = Component.translatable(subtitleKey);
		subtitleOverrideEndMs = Math.max(subtitleOverrideEndMs, playLabelOverrideEndMs + 22L);
		schedulePlayLie(now);
		if (client != null && RANDOM.nextFloat() < 0.20F) {
			triggerDistortion(now);
		}
	}

	private static void maybeShowPhantomButton(long now, boolean forced) {
		if (!forced && RANDOM.nextFloat() > 0.15F) {
			schedulePhantomButton(now);
			return;
		}
		showPhantomButton(now);
	}

	private static void showPhantomButton(long now) {
		phantomButtonVisible = true;
		phantomButtonLabel = Component.translatable(PHANTOM_BUTTON_KEYS[RANDOM.nextInt(PHANTOM_BUTTON_KEYS.length)]);
		phantomButtonRow = RANDOM.nextInt(3);
		phantomButtonBetweenRows = phantomButtonRow < 2 && RANDOM.nextFloat() < 0.42F;
		phantomButtonXOffset = RANDOM.nextIntBetweenInclusive(-18, 18);
		phantomButtonEndMs = now + PHANTOM_BUTTON_MIN_DURATION_MS + RANDOM.nextInt((int) (PHANTOM_BUTTON_MAX_DURATION_MS - PHANTOM_BUTTON_MIN_DURATION_MS));
		schedulePhantomButton(now);
	}

	private static void triggerLogoFlicker(long now) {
		logoBrightness = 0.97F + RANDOM.nextFloat() * 0.06F;
		logoFlickerEndMs = now + LOGO_FLICKER_MIN_DURATION_MS + RANDOM.nextInt((int) (LOGO_FLICKER_MAX_DURATION_MS - LOGO_FLICKER_MIN_DURATION_MS + 1));
		scheduleLogoFlicker(now);
	}

	private static void maybeTriggerHorizontalGlitch(long now, boolean forced) {
		if (!forced && RANDOM.nextFloat() > 0.22F) {
			scheduleHorizontalGlitch(now);
			return;
		}
		horizontalGlitchShiftPx = RANDOM.nextIntBetweenInclusive(-6, 6);
		horizontalGlitchEndMs = now + 42L + RANDOM.nextInt(42);
		scheduleHorizontalGlitch(now);
	}

	private static void triggerDistortion(long now) {
		distortionBoost = 0.16F;
		distortionBoostEndMs = now + 2600L;
		horizontalGlitchShiftPx = RANDOM.nextIntBetweenInclusive(-7, 7);
		horizontalGlitchEndMs = now + 70L + RANDOM.nextInt(52);
		scheduleDistortionLayer(now - 1L);
		scheduleDropout(now - 1L);
	}

	private static void playAmbientLayer(Minecraft client, long now) {
		float sanityPressure = getSanityPressure();
		SoundEvent sound = AMBIENT_LAYER_SOUNDS[RANDOM.nextInt(AMBIENT_LAYER_SOUNDS.length)];
		float volume = 0.010F + sanityPressure * 0.018F + RANDOM.nextFloat() * 0.008F;
		float pitch = 0.44F + RANDOM.nextFloat() * 0.14F - sanityPressure * 0.04F;
		playMenuSound(client, sound, volume, pitch);
		scheduleAmbientLayer(now);
	}

	private static void playDistortionLayer(Minecraft client, long now) {
		float sanityPressure = getSanityPressure();
		if (sanityPressure < 0.30F && distortionBoost <= 0.0F) {
			scheduleDistortionLayer(now);
			return;
		}
		SoundEvent sound = DISTORTION_LAYER_SOUNDS[RANDOM.nextInt(DISTORTION_LAYER_SOUNDS.length)];
		float volume = 0.008F + sanityPressure * 0.015F + distortionBoost * 0.020F;
		float pitch = 0.38F + RANDOM.nextFloat() * 0.18F - sanityPressure * 0.08F;
		playMenuSound(client, sound, volume, pitch);
		scheduleDistortionLayer(now);
	}

	private static void maybeStartDropout(long now) {
		float sanityPressure = getSanityPressure();
		if ((sanityPressure >= 0.86F || distortionBoost > 0.0F) && RANDOM.nextFloat() < 0.44F) {
			dropoutEndMs = now + 70L + RANDOM.nextInt(130);
		}
		scheduleDropout(now);
	}

	private static void updateMusicDistortion(Minecraft client, long now) {
		float configuredVolume = client.options.getSoundSourceVolume(SoundSource.MUSIC);
		float sanityPressure = getSanityPressure();
		float wowFlutter = (float) Math.sin(now / 1260.0D) * (0.004F + sanityPressure * 0.005F)
				+ (float) Math.sin(now / 2840.0D) * (0.003F + distortionBoost * 0.020F);
		float tapeSag = 1.0F - sanityPressure * 0.04F - distortionBoost * 0.10F;
		float lowPassFeel = sanityPressure < 0.58F ? 1.0F : 1.0F - (sanityPressure - 0.58F) * 0.12F;
		float dropoutFactor = now < dropoutEndMs ? 0.72F : 1.0F;
		float distorted = configuredVolume * Mth.clamp(tapeSag * lowPassFeel + wowFlutter, 0.62F, 1.0F) * dropoutFactor;
		if (appliedMusicVolume < 0.0F || Math.abs(appliedMusicVolume - distorted) > 0.01F) {
			client.getSoundManager().updateSourceVolume(SoundSource.MUSIC, distorted);
			appliedMusicVolume = distorted;
		}
	}

	private static void restoreMusicVolume(Minecraft client) {
		if (client == null || appliedMusicVolume < 0.0F) {
			return;
		}
		client.getSoundManager().updateSourceVolume(SoundSource.MUSIC, client.options.getSoundSourceVolume(SoundSource.MUSIC));
		appliedMusicVolume = -1.0F;
	}

	private static void playMenuSound(Minecraft client, SoundEvent sound, float volume, float pitch) {
		if (client == null || client.getSoundManager() == null) {
			return;
		}
		client.getSoundManager().play(SimpleSoundInstance.forLocalAmbience(sound, volume, pitch));
	}

	private static float getSanityPressure() {
		return Mth.clamp((70.0F - ClientSanityState.getSanity()) / 70.0F, 0.0F, 1.0F);
	}

	private static void scheduleSubtitleRotation(long now) {
		nextSubtitleRotateAtMs = now + 7000L + RANDOM.nextInt(5500);
	}

	private static void schedulePlayerSubtitle(long now) {
		nextPlayerSubtitleAtMs = now + 78000L + RANDOM.nextInt(90000);
	}

	private static void schedulePlayLie(long now) {
		nextPlayLieAtMs = now + 36000L + RANDOM.nextInt(95000);
	}

	private static void schedulePhantomButton(long now) {
		nextPhantomButtonAtMs = now + 62000L + RANDOM.nextInt(110000);
	}

	private static void scheduleLogoFlicker(long now) {
		nextLogoFlickerAtMs = now + 24000L + RANDOM.nextInt(38000);
	}

	private static void scheduleHorizontalGlitch(long now) {
		nextHorizontalGlitchAtMs = now + 38000L + RANDOM.nextInt(52000);
	}

	private static void scheduleAmbientLayer(long now) {
		nextAmbientLayerAtMs = now + 7600L + RANDOM.nextInt(10800);
	}

	private static void scheduleDistortionLayer(long now) {
		nextDistortionLayerAtMs = now + 18000L + RANDOM.nextInt(22000);
	}

	private static void scheduleDropout(long now) {
		nextDropoutAtMs = now + 28000L + RANDOM.nextInt(32000);
	}

	private static String pickPlayLieKey() {
		if (RANDOM.nextInt(12) == 0) {
			return PLAY_GLITCH_KEYS[PLAY_GLITCH_KEYS.length - 1];
		}
		return PLAY_GLITCH_KEYS[RANDOM.nextInt(PLAY_GLITCH_KEYS.length - 1)];
	}
}
