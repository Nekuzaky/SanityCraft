package net.nekuzaky.sanitycraft.sanity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class SanityNetworking {
	private static boolean initialized = false;
	private static final Map<UUID, EffectPacketLimiterState> EFFECT_PACKET_LIMITERS = new ConcurrentHashMap<>();

	private SanityNetworking() {
	}

	public static void initialize() {
		if (initialized) {
			return;
		}
		initialized = true;
		PayloadTypeRegistry.playS2C().register(SanitySyncPayload.TYPE, SanitySyncPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(SanityJumpscarePayload.TYPE, SanityJumpscarePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(SanityScarePulsePayload.TYPE, SanityScarePulsePayload.CODEC);
	}

	public static void sync(ServerPlayer player, int sanity) {
		ServerPlayNetworking.send(player, new SanitySyncPayload(sanity));
	}

	public static void triggerJumpscare(ServerPlayer player, int variant, int durationTicks) {
		if (!allowEffectPacket(player)) {
			SanityManager.debug(player, "network_guard blocked jumpscare packet");
			return;
		}
		ServerPlayNetworking.send(player, new SanityJumpscarePayload(variant, durationTicks));
	}

	public static void triggerScarePulse(ServerPlayer player, int durationTicks, int intensity) {
		if (!allowEffectPacket(player)) {
			SanityManager.debug(player, "network_guard blocked scare_pulse packet");
			return;
		}
		ServerPlayNetworking.send(player, new SanityScarePulsePayload(Math.max(1, durationTicks), Math.max(1, intensity)));
	}

	public static void clearRateState(ServerPlayer player) {
		EFFECT_PACKET_LIMITERS.remove(player.getUUID());
	}

	private static boolean allowEffectPacket(ServerPlayer player) {
		SanityConfig config = SanityManager.getConfig();
		EffectPacketLimiterState state = EFFECT_PACKET_LIMITERS.computeIfAbsent(player.getUUID(), id -> new EffectPacketLimiterState());
		long nowMs = System.currentTimeMillis();
		if (nowMs - state.windowStartMs >= 60_000L) {
			state.windowStartMs = nowMs;
			state.sentInWindow = 0;
		}
		long minSpacingMs = Math.max(0, config.networkEffectMinSpacingTicks) * 50L;
		if (nowMs - state.lastSendMs < minSpacingMs) {
			return false;
		}
		if (state.sentInWindow >= Math.max(1, config.networkEffectPacketsPerMinute)) {
			return false;
		}
		state.sentInWindow++;
		state.lastSendMs = nowMs;
		return true;
	}

	private static class EffectPacketLimiterState {
		private long windowStartMs = System.currentTimeMillis();
		private long lastSendMs = 0L;
		private int sentInWindow = 0;
	}
}
