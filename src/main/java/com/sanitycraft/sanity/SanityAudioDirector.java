package com.sanitycraft.sanity;

import com.sanitycraft.data.config.SanityCraftConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class SanityAudioDirector {
	private static final SanityAudioEvent[] UNEASY_AMBIENT = {
			SanityAudioEvent.HOUSE_SOUND,
			SanityAudioEvent.CAVE_SOUND,
			SanityAudioEvent.FOREST_SOUND
	};
	private static final SanityAudioEvent[] DISTURBED_AMBIENT = {
			SanityAudioEvent.CAVE_SOUND,
			SanityAudioEvent.HOUSE_SOUND,
			SanityAudioEvent.FOREST_SOUND,
			SanityAudioEvent.PHANTOM_SOUND
	};
	private static final SanityAudioEvent[] FRACTURED_AMBIENT = {
			SanityAudioEvent.HOUSE_SOUND,
			SanityAudioEvent.CAVE_SOUND,
			SanityAudioEvent.FOREST_SOUND,
			SanityAudioEvent.PHANTOM_SOUND
	};
	private static final SanityAudioEvent[] COLLAPSE_AMBIENT = {
			SanityAudioEvent.PHANTOM_SOUND,
			SanityAudioEvent.CAVE_SOUND,
			SanityAudioEvent.HOUSE_SOUND,
			SanityAudioEvent.FOREST_SOUND
	};
	private static final SanityAudioEvent[] MOVEMENT_CUES = {
			SanityAudioEvent.HOUSE_SOUND,
			SanityAudioEvent.FOREST_SOUND
	};
	private static final int CHEST_EVENT_COOLDOWN = 20 * 9;

	private SanityAudioDirector() {
	}

	public static void tickPlayer(ServerPlayer player, SanityComponent component, SanityThresholds.Stage stage, SanityCraftConfig config) {
		RandomSource random = player.getRandom();
		switch (stage) {
			case UNEASY -> {
				maybePlayAmbient(player, component, config, random, stage, config.events.uneasyAmbientChance, "stage_uneasy");
				maybePlayMovementCue(player, component, config, random, config.events.uneasyFootstepChance, "stage_uneasy");
			}
			case DISTURBED -> {
				maybePlayAmbient(player, component, config, random, stage, config.events.uneasyAmbientChance + 0.03F, "stage_disturbed");
				maybePlayMovementCue(player, component, config, random, config.events.disturbedFootstepChance, "stage_disturbed");
			}
			case FRACTURED -> {
				maybePlayAmbient(player, component, config, random, stage, config.events.uneasyAmbientChance + 0.05F, "stage_fractured");
				maybePlayMovementCue(player, component, config, random, config.events.disturbedFootstepChance + 0.03F, "stage_fractured");
				maybePlayBreathing(player, component, config, random, config.events.fracturedBreathingChance, false, "stage_fractured");
			}
			case COLLAPSE -> {
				maybePlayAmbient(player, component, config, random, stage, config.events.uneasyAmbientChance + 0.08F, "stage_collapse");
				maybePlayMovementCue(player, component, config, random, config.events.disturbedFootstepChance + 0.06F, "stage_collapse");
				maybePlayBreathing(player, component, config, random, config.events.fracturedBreathingChance + 0.08F, true, "stage_collapse");
				maybePlayWhispers(player, component, config, random, Math.min(0.35F, config.events.fakeAudioChance + 0.10F));
			}
			default -> {
			}
		}
	}

	public static void forceAmbience(ServerPlayer player) {
		SanityThresholds.Stage stage = SanityThresholds.resolve(SanityManager.get(player).getSanity(), SanityCraftConfig.get());
		AudioOutcome outcome = attemptAmbientPalette(player, SanityCraftConfig.get(), player.getRandom(), stage, "debug_force_ambience");
		SanityDebug.logEvent(player, outcome.accepted()
				? "forced_ambience"
				: "forced_ambience_rejected reason=" + outcome.reason());
	}

	public static String forceAudioEvent(ServerPlayer player, SanityAudioEvent event) {
		PlaybackStyle style = switch (event) {
			case CHEST_SOUND -> PlaybackStyle.CHEST;
			case BREATHING -> PlaybackStyle.BREATHING;
			default -> PlaybackStyle.AMBIENT;
		};
		AudioOutcome outcome = attemptEvent(
				player,
				SanityCraftConfig.get(),
				player.getRandom(),
				event,
				"debug_force_" + event.commandName(),
				style,
				false);
		return outcome.accepted()
				? "Forced " + event.commandName() + " at " + format(outcome.position())
				: "Rejected " + event.commandName() + ": " + outcome.reason();
	}

	public static boolean maybeChestSound(
			ServerPlayer player,
			SanityComponent component,
			SanityCraftConfig config,
			RandomSource random,
			float chance,
			String requestSource) {
		if (!component.isCooldownReady(SanityComponent.Cooldown.CHEST_SOUND) || !roll(random, chance)) {
			return false;
		}
		if (!component.tryConsumeEffectBudget(config, 1)) {
			return false;
		}

		AudioOutcome outcome = attemptEvent(player, config, random, SanityAudioEvent.CHEST_SOUND, requestSource, PlaybackStyle.CHEST, false);
		if (!outcome.accepted()) {
			return false;
		}
		component.setCooldown(SanityComponent.Cooldown.CHEST_SOUND, jitter(CHEST_EVENT_COOLDOWN, random));
		return true;
	}

	public static boolean playFalseChest(ServerPlayer player, String source) {
		AudioOutcome outcome = attemptEvent(
				player,
				SanityCraftConfig.get(),
				player.getRandom(),
				SanityAudioEvent.CHEST_SOUND,
				source,
				PlaybackStyle.CHEST,
				false);
		if (!outcome.accepted()) {
			SanityDebug.logAudioRejected(player, SanityAudioEvent.CHEST_SOUND, source, outcome.reason());
		}
		return outcome.accepted();
	}

	public static boolean playFalseSafetyBreathing(ServerPlayer player, boolean intense, String source) {
		AudioOutcome outcome = attemptEvent(
				player,
				SanityCraftConfig.get(),
				player.getRandom(),
				SanityAudioEvent.BREATHING,
				source,
				PlaybackStyle.BREATHING,
				intense);
		if (!outcome.accepted()) {
			SanityDebug.logAudioRejected(player, SanityAudioEvent.BREATHING, source, outcome.reason());
		}
		return outcome.accepted();
	}

	public static void playWhisperBurst(ServerPlayer player, boolean intense, String source) {
		if (intense) {
			player.playNotifySound(SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 0.42F, 0.58F + player.getRandom().nextFloat() * 0.08F);
			player.playNotifySound(SoundEvents.WITCH_AMBIENT, SoundSource.HOSTILE, 0.28F, 0.42F + player.getRandom().nextFloat() * 0.10F);
		} else {
			player.playNotifySound(SoundEvents.WITCH_AMBIENT, SoundSource.HOSTILE, 0.25F, 0.50F + player.getRandom().nextFloat() * 0.14F);
		}
		SanityDebug.logEvent(player, "collapse_whispers source=" + source);
	}

	private static void maybePlayAmbient(
			ServerPlayer player,
			SanityComponent component,
			SanityCraftConfig config,
			RandomSource random,
			SanityThresholds.Stage stage,
			float chance,
			String requestSource) {
		if (!component.isCooldownReady(SanityComponent.Cooldown.AMBIENT_AUDIO)) {
			return;
		}
		if (!roll(random, chance + config.events.fakeAudioChance * 0.25F)) {
			return;
		}
		if (!component.tryConsumeEffectBudget(config, 1)) {
			return;
		}

		AudioOutcome outcome = attemptAmbientPalette(player, config, random, stage, requestSource);
		if (!outcome.accepted()) {
			return;
		}
		component.setCooldown(SanityComponent.Cooldown.AMBIENT_AUDIO, jitter(config.events.ambientCooldownTicks, random));
	}

	private static void maybePlayMovementCue(
			ServerPlayer player,
			SanityComponent component,
			SanityCraftConfig config,
			RandomSource random,
			float chance,
			String requestSource) {
		if (!component.isCooldownReady(SanityComponent.Cooldown.FOOTSTEPS)) {
			return;
		}
		if (!roll(random, chance)) {
			return;
		}
		if (!component.tryConsumeEffectBudget(config, 1)) {
			return;
		}

		AudioOutcome outcome = attemptMovementCue(player, config, random, requestSource);
		if (!outcome.accepted()) {
			return;
		}
		component.setCooldown(SanityComponent.Cooldown.FOOTSTEPS, jitter(config.events.footstepCooldownTicks, random));
	}

	private static void maybePlayBreathing(
			ServerPlayer player,
			SanityComponent component,
			SanityCraftConfig config,
			RandomSource random,
			float chance,
			boolean collapse,
			String requestSource) {
		if (!component.isCooldownReady(SanityComponent.Cooldown.BREATHING)) {
			return;
		}
		if (!roll(random, chance)) {
			return;
		}
		if (!component.tryConsumeEffectBudget(config, 1)) {
			return;
		}

		AudioOutcome outcome = attemptEvent(player, config, random, SanityAudioEvent.BREATHING, requestSource, PlaybackStyle.BREATHING, collapse);
		if (!outcome.accepted()) {
			return;
		}
		component.setCooldown(SanityComponent.Cooldown.BREATHING, jitter(config.events.breathingCooldownTicks, random));
	}

	private static void maybePlayWhispers(
			ServerPlayer player,
			SanityComponent component,
			SanityCraftConfig config,
			RandomSource random,
			float chance) {
		if (!component.isCooldownReady(SanityComponent.Cooldown.WHISPERS)) {
			return;
		}
		if (!roll(random, chance)) {
			return;
		}
		if (!component.tryConsumeEffectBudget(config, 1)) {
			return;
		}

		playWhisperBurst(player, true, "stage_collapse");
		component.setCooldown(SanityComponent.Cooldown.WHISPERS, jitter(config.events.breathingCooldownTicks + 30, random));
	}

	private static AudioOutcome attemptAmbientPalette(
			ServerPlayer player,
			SanityCraftConfig config,
			RandomSource random,
			SanityThresholds.Stage stage,
			String requestSource) {
		SanityAudioEvent[] palette = switch (stage) {
			case UNEASY -> UNEASY_AMBIENT;
			case DISTURBED -> DISTURBED_AMBIENT;
			case FRACTURED -> FRACTURED_AMBIENT;
			case COLLAPSE -> COLLAPSE_AMBIENT;
			default -> UNEASY_AMBIENT;
		};
		return attemptFromPalette(player, config, random, palette, requestSource, PlaybackStyle.AMBIENT, false);
	}

	private static AudioOutcome attemptMovementCue(
			ServerPlayer player,
			SanityCraftConfig config,
			RandomSource random,
			String requestSource) {
		return attemptFromPalette(player, config, random, MOVEMENT_CUES, requestSource, PlaybackStyle.MOVEMENT, false);
	}

	private static AudioOutcome attemptFromPalette(
			ServerPlayer player,
			SanityCraftConfig config,
			RandomSource random,
			SanityAudioEvent[] palette,
			String requestSource,
			PlaybackStyle style,
			boolean intense) {
		int start = palette.length <= 1 ? 0 : random.nextInt(palette.length);
		AudioOutcome lastRejection = AudioOutcome.rejected("no_valid_context");
		for (int offset = 0; offset < palette.length; offset++) {
			SanityAudioEvent event = palette[(start + offset) % palette.length];
			AudioOutcome outcome = attemptEvent(player, config, random, event, requestSource, style, intense);
			if (outcome.accepted()) {
				return outcome;
			}
			lastRejection = outcome;
		}
		return lastRejection;
	}

	private static AudioOutcome attemptEvent(
			ServerPlayer player,
			SanityCraftConfig config,
			RandomSource random,
			SanityAudioEvent event,
			String requestSource,
			PlaybackStyle style,
			boolean intense) {
		SanityDebug.logAudioRequest(player, event, requestSource);
		SanityAudioContextRules.AudioDecision decision = SanityAudioContextRules.evaluate(player, event, random, config);
		if (!decision.accepted()) {
			SanityDebug.logAudioRejected(player, event, requestSource, decision.reason());
			return AudioOutcome.rejected(decision.reason());
		}

		playEvent(player, random, event, style, intense, decision);
		SanityDebug.logAudioAccepted(player, event, requestSource, decision.sourcePos(), decision.reason());
		return AudioOutcome.accepted(decision.sourcePos(), decision.reason());
	}

	private static void playEvent(
			ServerPlayer player,
			RandomSource random,
			SanityAudioEvent event,
			PlaybackStyle style,
			boolean intense,
			SanityAudioContextRules.AudioDecision decision) {
		switch (event) {
			case CHEST_SOUND -> playChestEvent(player, random, decision);
			case PHANTOM_SOUND -> playPrivateSound(
					player,
					SoundEvents.PHANTOM_AMBIENT,
					SoundSource.AMBIENT,
					decision.sourcePos(),
					0.34F + random.nextFloat() * 0.10F,
					0.78F + random.nextFloat() * 0.14F);
			case CAVE_SOUND -> playPrivateSound(
					player,
					SoundEvents.AMBIENT_CAVE,
					SoundSource.AMBIENT,
					decision.sourcePos(),
					0.38F + random.nextFloat() * 0.10F,
					0.76F + random.nextFloat() * 0.16F);
			case HOUSE_SOUND -> playHouseEvent(player, random, style, decision);
			case FOREST_SOUND -> playForestEvent(player, random, style, decision);
			case BREATHING -> {
				SoundEvent sound = intense ? SoundEvents.WARDEN_NEARBY_CLOSEST : SoundEvents.WARDEN_SNIFF;
				float volume = intense ? 0.52F + random.nextFloat() * 0.10F : 0.30F + random.nextFloat() * 0.08F;
				float pitch = intense ? 0.70F + random.nextFloat() * 0.12F : 0.82F + random.nextFloat() * 0.12F;
				playPrivateSound(player, sound, SoundSource.HOSTILE, decision.sourcePos(), volume, pitch);
			}
		}
	}

	private static void playChestEvent(ServerPlayer player, RandomSource random, SanityAudioContextRules.AudioDecision decision) {
		boolean open = random.nextBoolean();
		SoundEvent sound;
		if (decision.anchor() != null && decision.anchor().state().is(Blocks.BARREL)) {
			sound = open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE;
		} else {
			sound = open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE;
		}
		playPrivateSound(
				player,
				sound,
				SoundSource.BLOCKS,
				decision.sourcePos(),
				0.52F + random.nextFloat() * 0.10F,
				0.84F + random.nextFloat() * 0.14F);
	}

	private static void playHouseEvent(
			ServerPlayer player,
			RandomSource random,
			PlaybackStyle style,
			SanityAudioContextRules.AudioDecision decision) {
		SoundEvent sound = switch (style) {
			case MOVEMENT -> random.nextBoolean() ? SoundEvents.WOOD_STEP : SoundEvents.WOODEN_TRAPDOOR_CLOSE;
			default -> switch (random.nextInt(3)) {
				case 0 -> SoundEvents.WOODEN_DOOR_CLOSE;
				case 1 -> SoundEvents.WOODEN_TRAPDOOR_CLOSE;
				default -> SoundEvents.WOOD_STEP;
			};
		};
		float volume = style == PlaybackStyle.MOVEMENT ? 0.28F + random.nextFloat() * 0.08F : 0.34F + random.nextFloat() * 0.10F;
		float pitch = 0.76F + random.nextFloat() * 0.18F;
		playPrivateSound(player, sound, SoundSource.BLOCKS, decision.sourcePos(), volume, pitch);
	}

	private static void playForestEvent(
			ServerPlayer player,
			RandomSource random,
			PlaybackStyle style,
			SanityAudioContextRules.AudioDecision decision) {
		SoundEvent sound = switch (style) {
			case MOVEMENT -> switch (random.nextInt(3)) {
				case 0 -> SoundEvents.AZALEA_LEAVES_STEP;
				case 1 -> SoundEvents.GRASS_STEP;
				default -> SoundEvents.SWEET_BERRY_BUSH_BREAK;
			};
			default -> random.nextBoolean() ? SoundEvents.AZALEA_LEAVES_STEP : SoundEvents.WET_GRASS_STEP;
		};
		float volume = style == PlaybackStyle.MOVEMENT ? 0.24F + random.nextFloat() * 0.08F : 0.30F + random.nextFloat() * 0.10F;
		float pitch = 0.84F + random.nextFloat() * 0.16F;
		playPrivateSound(player, sound, SoundSource.AMBIENT, decision.sourcePos(), volume, pitch);
	}

	private static void playPrivateSound(
			ServerPlayer player,
			Holder<SoundEvent> sound,
			SoundSource source,
			Vec3 position,
			float volume,
			float pitch) {
		player.connection.send(new ClientboundSoundPacket(
				sound,
				source,
				position.x,
				position.y,
				position.z,
				Math.max(0.01F, volume),
				Mth.clamp(pitch, 0.5F, 2.0F),
				player.getRandom().nextLong()));
	}

	private static void playPrivateSound(
			ServerPlayer player,
			SoundEvent sound,
			SoundSource source,
			Vec3 position,
			float volume,
			float pitch) {
		playPrivateSound(player, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), source, position, volume, pitch);
	}

	private static boolean roll(RandomSource random, float chance) {
		return random.nextFloat() < Mth.clamp(chance, 0.0F, 1.0F);
	}

	private static int jitter(int baseTicks, RandomSource random) {
		int safeBase = Math.max(1, baseTicks);
		int variance = Math.max(1, safeBase / 4);
		return Math.max(1, safeBase - variance + random.nextInt(variance * 2 + 1));
	}

	private static String format(Vec3 position) {
		return String.format("(%.2f, %.2f, %.2f)", position.x, position.y, position.z);
	}

	private enum PlaybackStyle {
		AMBIENT,
		MOVEMENT,
		CHEST,
		BREATHING
	}

	private record AudioOutcome(boolean accepted, Vec3 position, String reason) {
		private static AudioOutcome accepted(Vec3 position, String reason) {
			return new AudioOutcome(true, position, reason);
		}

		private static AudioOutcome rejected(String reason) {
			return new AudioOutcome(false, Vec3.ZERO, reason);
		}
	}
}
