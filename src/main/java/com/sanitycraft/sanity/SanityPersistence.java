package com.sanitycraft.sanity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;

public final class SanityPersistence {
	private static final Map<UUID, Integer> SESSION_CACHE = new ConcurrentHashMap<>();

	private SanityPersistence() {
	}

	public static int load(ServerPlayer player) {
		return SESSION_CACHE.getOrDefault(player.getUUID(), SanityThresholds.DEFAULT_SANITY);
	}

	public static void save(ServerPlayer player, SanityComponent component) {
		// TODO Phase 3: replace with PersistentState-backed world persistence.
		SESSION_CACHE.put(player.getUUID(), component.getSanity());
	}
}
