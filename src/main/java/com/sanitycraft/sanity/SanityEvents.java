package com.sanitycraft.sanity;

import com.sanitycraft.network.sync.ClientEffectSyncService;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
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
			server.execute(() -> {
				var component = SanityManager.get(handler.player);
				long gameTime = handler.player.level().getGameTime();
				com.sanitycraft.network.sync.SanitySyncService.syncNow(handler.player, component, gameTime);
			});
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			SanityManager.save(handler.player);
			SanityHallucinationService.dispelOwnedHallucinations(handler.player);
			SanityPsychologicalService.clearPlayer(handler.player);
			ClientEffectSyncService.clearPlayer(handler.player);
			SanityDebug.clearPlayer(handler.player);
			SanityManager.remove(handler.player);
		});

		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			if (alive) {
				SanityManager.copy(oldPlayer, newPlayer);
			} else {
				SanityManager.setSanity(newPlayer, com.sanitycraft.data.config.SanityCraftConfig.get().general.respawnSanity);
			}
			com.sanitycraft.network.sync.SanitySyncService.syncNow(newPlayer, SanityManager.get(newPlayer), newPlayer.level().getGameTime());
		});

		ServerTickEvents.END_SERVER_TICK.register(SanityTickService::tickServer);
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			ClientEffectSyncService.clearAll();
			SanityPsychologicalService.clearAll();
			SanityDebug.clearAll();
			SanityManager.clearAll();
		});
	}
}
