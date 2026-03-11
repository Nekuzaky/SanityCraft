package net.nekuzaky.sanitycraft.sanity;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.nekuzaky.sanitycraft.event.PlayerEvents;

public class SanityEvents {
	private static boolean registered = false;

	private SanityEvents() {
	}

	public static void register() {
		if (registered) {
			return;
		}
		registered = true;

		PlayerEvents.END_PLAYER_TICK.register(player -> {
			if (player instanceof ServerPlayer serverPlayer) {
				PlayerSanityComponent component = SanityManager.get(serverPlayer);
				SanityNarrativeDirector.tick(serverPlayer, component, SanityManager.getConfig());
				SanityStalkerHuntDirector.tick(serverPlayer);
				SanityManager.tick(serverPlayer);
			}
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			PlayerSanityComponent component = SanityManager.get(handler.player);
			component.setSanity(SanityPersistence.get(handler.player));
			SanityNetworking.sync(handler.player, component.getSanity());
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			SanityPersistence.set(handler.player, SanityManager.get(handler.player).getSanity());
			SanityManager.remove(handler.player);
		});
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			if (alive) {
				SanityManager.copy(oldPlayer, newPlayer);
			} else {
				SanityManager.setSanity(newPlayer, SanityManager.getConfig().sanityOnRespawnAfterDeath);
			}
			SanityNetworking.sync(newPlayer, SanityManager.get(newPlayer).getSanity());
		});
	}
}
