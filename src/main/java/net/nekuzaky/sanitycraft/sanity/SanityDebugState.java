package net.nekuzaky.sanitycraft.sanity;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SanityDebugState {
	private static final Set<UUID> ENABLED = ConcurrentHashMap.newKeySet();

	private SanityDebugState() {
	}

	public static boolean enable(ServerPlayer player) {
		return ENABLED.add(player.getUUID());
	}

	public static boolean disable(ServerPlayer player) {
		return ENABLED.remove(player.getUUID());
	}

	public static void clear(ServerPlayer player) {
		ENABLED.remove(player.getUUID());
	}

	public static boolean isEnabled(ServerPlayer player) {
		return ENABLED.contains(player.getUUID());
	}

	public static void log(ServerPlayer player, String message) {
		if (!isEnabled(player)) {
			return;
		}
		player.displayClientMessage(Component.literal("[SanityDebug] " + message), false);
	}
}
