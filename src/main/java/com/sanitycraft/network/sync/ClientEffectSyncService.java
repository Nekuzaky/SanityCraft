package com.sanitycraft.network.sync;

import com.sanitycraft.data.config.SanityCraftConfig;
import com.sanitycraft.network.packet.ClientboundMenuTestPacket;
import com.sanitycraft.network.packet.ClientboundScarePulsePacket;
import com.sanitycraft.sanity.SanityDebug;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class ClientEffectSyncService {
	private static final long RATE_WINDOW_NANOS = 1_000_000_000L;
	private static final Map<UUID, EffectWindow> RATE_WINDOWS = new ConcurrentHashMap<>();
	private static boolean registered;

	private ClientEffectSyncService() {
	}

	public static void register() {
		if (registered) {
			return;
		}
		registered = true;
		PayloadTypeRegistry.playS2C().register(ClientboundScarePulsePacket.TYPE, ClientboundScarePulsePacket.CODEC);
		PayloadTypeRegistry.playS2C().register(ClientboundMenuTestPacket.TYPE, ClientboundMenuTestPacket.CODEC);
	}

	public static void sendScarePulse(ServerPlayer player, int durationTicks, int intensity) {
		sendScarePulse(player, durationTicks, intensity, "runtime", false);
	}

	public static boolean sendScarePulse(ServerPlayer player, int durationTicks, int intensity, String source, boolean bypassRateLimit) {
		if (!bypassRateLimit && !tryConsumeRateLimit(player)) {
			SanityDebug.logEvent(player, "Scare pulse rate limited source=" + source);
			return false;
		}
		ServerPlayNetworking.send(player, new ClientboundScarePulsePacket(Math.max(1, durationTicks), Math.max(1, intensity)));
		SanityDebug.logEvent(player, "Scare pulse source=" + source + " duration=" + Math.max(1, durationTicks) + " intensity=" + Math.max(1, intensity));
		return true;
	}

	public static void clearPlayer(ServerPlayer player) {
		RATE_WINDOWS.remove(player.getUUID());
	}

	public static void clearAll() {
		RATE_WINDOWS.clear();
	}

	public static void sendMenuTest(ServerPlayer player, MenuTestType testType) {
		ServerPlayNetworking.send(player, new ClientboundMenuTestPacket(testType.id()));
		SanityDebug.logEvent(player, "Menu test effect=" + testType.commandName());
	}

	private static boolean tryConsumeRateLimit(ServerPlayer player) {
		int maxPacketsPerSecond = SanityCraftConfig.get().multiplayer.maxEffectPacketsPerSecond;
		long now = System.nanoTime();
		EffectWindow window = RATE_WINDOWS.computeIfAbsent(player.getUUID(), ignored -> new EffectWindow(now));
		if (now - window.windowStartNanos >= RATE_WINDOW_NANOS) {
			window.windowStartNanos = now;
			window.sentPackets = 0;
		}
		if (window.sentPackets >= maxPacketsPerSecond) {
			return false;
		}
		window.sentPackets++;
		return true;
	}

	private static final class EffectWindow {
		private long windowStartNanos;
		private int sentPackets;

		private EffectWindow(long windowStartNanos) {
			this.windowStartNanos = windowStartNanos;
		}
	}
}
