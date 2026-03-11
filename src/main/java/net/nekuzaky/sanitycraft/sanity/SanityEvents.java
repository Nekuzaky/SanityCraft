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
				SanityManager.tick(serverPlayer);
			}
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			PlayerSanityComponent component = SanityManager.get(handler.player);
			SanityNetworking.sync(handler.player, component.getSanity());
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> SanityManager.remove(handler.player));
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			SanityManager.copy(oldPlayer, newPlayer);
			SanityNetworking.sync(newPlayer, SanityManager.get(newPlayer).getSanity());
		});
	}
}
