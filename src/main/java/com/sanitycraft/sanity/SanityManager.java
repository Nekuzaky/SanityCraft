package com.sanitycraft.sanity;

import com.sanitycraft.network.sync.SanitySyncService;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;

public final class SanityManager {
	private static final Map<UUID, SanityComponent> COMPONENTS = new ConcurrentHashMap<>();

	private SanityManager() {
	}

	public static SanityComponent get(ServerPlayer player) {
		return COMPONENTS.computeIfAbsent(player.getUUID(), ignored -> new SanityComponent());
	}

	public static void load(ServerPlayer player) {
		get(player).setSanity(SanityPersistence.load(player));
	}

	public static void save(ServerPlayer player) {
		SanityPersistence.save(player, get(player));
	}

	public static void remove(ServerPlayer player) {
		COMPONENTS.remove(player.getUUID());
	}

	public static void copy(ServerPlayer source, ServerPlayer target) {
		get(target).setSanity(get(source).getSanity());
	}

	public static void setSanity(ServerPlayer player, int value) {
		get(player).setSanity(value);
	}

	public static void addSanity(ServerPlayer player, int delta) {
		get(player).addSanity(delta);
	}

	public static void tick(ServerPlayer player) {
		SanityComponent component = get(player);
		if (!component.shouldSync()) {
			return;
		}
		SanitySyncService.sync(player, component);
		component.markSynced();
	}
}
