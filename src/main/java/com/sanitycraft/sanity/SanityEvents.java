package com.sanitycraft.sanity;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class SanityEvents {
	private static boolean registered;

	private SanityEvents() {
	}

	public static void register() {
		if (registered) {
			return;
		}
		registered = true;

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			SanityManager.load(handler.player);
			SanityManager.tick(handler.player);
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			SanityManager.save(handler.player);
			SanityManager.remove(handler.player);
		});

		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			SanityManager.copy(oldPlayer, newPlayer);
			SanityManager.tick(newPlayer);
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> server.getPlayerList().getPlayers().forEach(SanityManager::tick));
	}
}
