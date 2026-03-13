package com.sanitycraft.sanity;

import com.sanitycraft.data.config.SanityCraftConfig;
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
		get(player).updateSanity(SanityPersistence.load(player), SanityCraftConfig.get());
		get(player).requestImmediateSync();
	}

	public static void save(ServerPlayer player) {
		SanityPersistence.save(player, get(player).getSanity());
	}

	public static void remove(ServerPlayer player) {
		COMPONENTS.remove(player.getUUID());
	}

	public static void clearAll() {
		COMPONENTS.clear();
	}

	public static SanityUpdate copy(ServerPlayer source, ServerPlayer target) {
		return setSanity(target, get(source).getSanity());
	}

	public static SanityUpdate setSanity(ServerPlayer player, int value) {
		SanityComponent component = get(player);
		SanityUpdate update = component.updateSanity(value, SanityCraftConfig.get());
		if (update.changed()) {
			SanityPersistence.save(player, update.currentSanity());
		}
		SanityDebug.logStageTransition(player, update);
		return update;
	}

	public static SanityUpdate addSanity(ServerPlayer player, int delta) {
		return setSanity(player, get(player).getSanity() + delta);
	}

	public static void activateMentalShield(ServerPlayer player, int durationTicks) {
		get(player).setHallucinationShieldTicks(durationTicks);
	}
}
