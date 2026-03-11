package net.nekuzaky.sanitycraft.sanity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SanityManager {
	private static final Map<UUID, PlayerSanityComponent> PLAYER_SANITY = new ConcurrentHashMap<>();
	private static SanityConfig config = new SanityConfig();

	private SanityManager() {
	}

	public static void initialize() {
		config = SanityConfig.loadOrCreate();
	}

	public static SanityConfig getConfig() {
		return config;
	}

	public static PlayerSanityComponent get(ServerPlayer player) {
		return PLAYER_SANITY.computeIfAbsent(player.getUUID(), uuid -> new PlayerSanityComponent());
	}

	public static void remove(ServerPlayer player) {
		PLAYER_SANITY.remove(player.getUUID());
	}

	public static void setSanity(ServerPlayer player, int sanity) {
		PlayerSanityComponent component = get(player);
		component.setSanity(sanity);
		SanityNetworking.sync(player, component.getSanity());
		SanityPersistence.set(player, component.getSanity());
	}

	public static void addSanity(ServerPlayer player, int delta) {
		setSanity(player, get(player).getSanity() + delta);
	}

	public static void copy(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
		PlayerSanityComponent source = get(oldPlayer);
		PlayerSanityComponent target = get(newPlayer);
		target.setSanity(source.getSanity());
		target.resetZeroSanityTimer();
		SanityPersistence.set(newPlayer, target.getSanity());
	}

	public static void tickZeroSanityDeath(ServerPlayer player) {
		if (!config.zeroSanityDeathEnabled) {
			return;
		}
		PlayerSanityComponent component = get(player);
		int delayTicks = Math.max(1, config.zeroSanityDeathDelaySeconds) * 20;
		if (component.tickZeroSanityTimer(delayTicks)) {
			component.resetZeroSanityTimer();
			player.hurt(player.damageSources().magic(), Float.MAX_VALUE);
			player.displayClientMessage(Component.literal("Your mind collapsed."), true);
		}
	}

	public static void tick(ServerPlayer player) {
		PlayerSanityComponent component = get(player);
		component.tickCooldowns();
		if (!component.shouldUpdate(config.getUpdateIntervalTicks())) {
			return;
		}

		int before = component.getSanity();
		SanityEnvironmentSnapshot environment = SanityEnvironmentHelper.snapshot(player, config);
		int delta = SanityCalculator.computeDelta(environment, config);
		component.addSanity(delta);
		SanityEffects.apply(player, component, config);
		if (component.getSanity() != before) {
			SanityNetworking.sync(player, component.getSanity());
			SanityPersistence.set(player, component.getSanity());
		}
	}
}
