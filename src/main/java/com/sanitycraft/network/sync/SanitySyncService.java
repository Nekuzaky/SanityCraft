package com.sanitycraft.network.sync;

import com.sanitycraft.data.config.SanityCraftConfig;
import com.sanitycraft.network.packet.ClientboundSanitySyncPacket;
import com.sanitycraft.sanity.SanityComponent;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class SanitySyncService {
	private static boolean registered;

	private SanitySyncService() {
	}

	public static void register() {
		if (registered) {
			return;
		}
		registered = true;
		PayloadTypeRegistry.playS2C().register(ClientboundSanitySyncPacket.TYPE, ClientboundSanitySyncPacket.CODEC);
	}

	public static void sync(ServerPlayer player, SanityComponent component) {
		ServerPlayNetworking.send(player, new ClientboundSanitySyncPacket(component.getSanity()));
	}

	public static void syncIfNeeded(ServerPlayer player, SanityComponent component, long gameTime, SanityCraftConfig config) {
		if (!component.shouldSync(gameTime, config)) {
			return;
		}
		syncNow(player, component, gameTime);
	}

	public static void syncNow(ServerPlayer player, SanityComponent component, long gameTime) {
		sync(player, component);
		component.markSynced(gameTime);
	}
}
