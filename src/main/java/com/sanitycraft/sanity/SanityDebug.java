package com.sanitycraft.sanity;

import com.sanitycraft.SanityCraft;
import com.sanitycraft.data.config.SanityCraftConfig;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class SanityDebug {
	private static final long TICK_CONFIRMATION_INTERVAL = 100L;
	private static final Set<UUID> DEBUG_PLAYERS = ConcurrentHashMap.newKeySet();
	private static final Set<UUID> AUDIO_DEBUG_PLAYERS = ConcurrentHashMap.newKeySet();
	private static final Map<UUID, Long> LAST_TICK_CONFIRMATION = new ConcurrentHashMap<>();

	private SanityDebug() {
	}

	public static Component describe(ServerPlayer player, SanityComponent component) {
		SanityThresholds.Stage stage = SanityThresholds.resolve(component.getSanity(), SanityCraftConfig.get());
		return Component.literal(player.getName().getString()
				+ ": " + component.getSanity()
				+ " (" + stage.name().toLowerCase() + ", debug=" + (isEnabled(player) ? "on" : "off") + ")");
	}

	public static Component directorStatus(SanityComponent component) {
		SanityThresholds.Stage stage = SanityThresholds.resolve(component.getSanity(), SanityCraftConfig.get());
		return Component.literal("sanity=" + component.getSanity()
				+ " stage=" + stage.name().toLowerCase()
				+ " budget=" + component.getEventsInWindow() + "/" + SanityCraftConfig.get().events.budgetPerMinute
				+ " global_cd=" + component.getGlobalEffectCooldownTicks()
				+ " shield=" + component.getHallucinationShieldTicks());
	}

	public static void setEnabled(ServerPlayer player, boolean enabled) {
		if (enabled) {
			DEBUG_PLAYERS.add(player.getUUID());
			log(player, "Debug mode enabled.");
			return;
		}
		DEBUG_PLAYERS.remove(player.getUUID());
		LAST_TICK_CONFIRMATION.remove(player.getUUID());
		log(player, "Debug mode disabled.");
	}

	public static void setAudioDebugEnabled(ServerPlayer player, boolean enabled) {
		if (enabled) {
			AUDIO_DEBUG_PLAYERS.add(player.getUUID());
			logAudio(player, "Audio debug enabled.");
			return;
		}
		AUDIO_DEBUG_PLAYERS.remove(player.getUUID());
		logAudio(player, "Audio debug disabled.");
	}

	public static boolean isEnabled(ServerPlayer player) {
		return DEBUG_PLAYERS.contains(player.getUUID());
	}

	public static boolean isVerbose(ServerPlayer player) {
		return SanityCraftConfig.get().general.debugLogging || isEnabled(player);
	}

	public static boolean isAudioVerbose(ServerPlayer player) {
		return SanityCraftConfig.get().general.debugLogging || isEnabled(player) || AUDIO_DEBUG_PLAYERS.contains(player.getUUID());
	}

	public static void clearPlayer(ServerPlayer player) {
		DEBUG_PLAYERS.remove(player.getUUID());
		AUDIO_DEBUG_PLAYERS.remove(player.getUUID());
		LAST_TICK_CONFIRMATION.remove(player.getUUID());
	}

	public static void clearAll() {
		DEBUG_PLAYERS.clear();
		AUDIO_DEBUG_PLAYERS.clear();
		LAST_TICK_CONFIRMATION.clear();
	}

	public static void logStageTransition(ServerPlayer player, SanityUpdate update) {
		if (!update.stageChanged()) {
			return;
		}
		log(player, "Stage transition " + update.previousStage().name().toLowerCase()
				+ " -> " + update.currentStage().name().toLowerCase()
				+ " (" + update.previousSanity() + " -> " + update.currentSanity() + ")");
	}

	public static void logTickOwnerConfirmation(ServerPlayer player, SanityComponent component, long gameTime) {
		if (!isVerbose(player)) {
			return;
		}
		long lastLog = LAST_TICK_CONFIRMATION.getOrDefault(player.getUUID(), Long.MIN_VALUE);
		if (gameTime - lastLog < TICK_CONFIRMATION_INTERVAL) {
			return;
		}
		LAST_TICK_CONFIRMATION.put(player.getUUID(), gameTime);
		SanityThresholds.Stage stage = SanityThresholds.resolve(component.getSanity(), SanityCraftConfig.get());
		log(player, "Tick owner confirmation stage=" + stage.name().toLowerCase()
				+ " sanity=" + component.getSanity()
				+ " budget=" + component.getEventsInWindow()
				+ " global_cd=" + component.getGlobalEffectCooldownTicks());
	}

	public static void logHallucinationAttempt(ServerPlayer player, String hallucinationType, String source, String detail) {
		log(player, "Hallucination attempt type=" + hallucinationType + " source=" + source + " detail=" + detail);
	}

	public static void logHallucinationSuccess(ServerPlayer player, String hallucinationType, Vec3 position, String source) {
		log(player, "Hallucination success type=" + hallucinationType
				+ " source=" + source
				+ " pos=" + format(position));
	}

	public static void logParticleSpawn(ServerPlayer player, String particleName, Vec3 position, int count) {
		log(player, "Particle spawn type=" + particleName + " count=" + count + " pos=" + format(position));
	}

	public static void logStalkerVanish(ServerPlayer player, String reason, Vec3 position) {
		log(player, "Stalker vanish reason=" + reason + " pos=" + format(position));
	}

	public static void logBloodyCreeperPulse(ServerPlayer player, int durationTicks, int intensity, Vec3 position, String reason) {
		log(player, "Bloody creeper scare pulse reason=" + reason
				+ " duration=" + durationTicks
				+ " intensity=" + intensity
				+ " pos=" + format(position));
	}

	public static void logEvent(ServerPlayer player, String eventName) {
		log(player, eventName);
	}

	public static void logAudioRequest(ServerPlayer player, SanityAudioEvent event, String source) {
		logAudio(player, "Audio requested event=" + event.commandName() + " source=" + source);
	}

	public static void logAudioAccepted(ServerPlayer player, SanityAudioEvent event, String source, Vec3 position, String reason) {
		logAudio(player, "Audio accepted event=" + event.commandName()
				+ " source=" + source
				+ " pos=" + format(position)
				+ " context=" + reason);
	}

	public static void logAudioRejected(ServerPlayer player, SanityAudioEvent event, String source, String reason) {
		logAudio(player, "Audio rejected event=" + event.commandName()
				+ " source=" + source
				+ " reason=" + reason);
	}

	private static void log(ServerPlayer player, String message) {
		if (!isVerbose(player)) {
			return;
		}
		SanityCraft.LOGGER.info("[SanityDebug] {} ({}) {}", player.getGameProfile().getName(), player.getUUID(), message);
	}

	private static void logAudio(ServerPlayer player, String message) {
		if (!isAudioVerbose(player)) {
			return;
		}
		SanityCraft.LOGGER.info("[SanityAudio] {} ({}) {}", player.getGameProfile().getName(), player.getUUID(), message);
	}

	private static String format(Vec3 position) {
		return String.format("(%.2f, %.2f, %.2f)", position.x, position.y, position.z);
	}
}
