package com.sanitycraft.sanity.gameplay;

import com.sanitycraft.data.config.SanityCraftConfig;
import com.sanitycraft.entity.observer.ObserverEntity;
import com.sanitycraft.registry.ModEntities;
import com.sanitycraft.sanity.SanityComponent;
import com.sanitycraft.sanity.SanityDebug;
import com.sanitycraft.sanity.SanityThresholds;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;

public final class SanityObserverService {
	private static final int OBSERVER_COOLDOWN = 20 * 26;
	private static final int OBSERVER_LIFETIME = 20 * 16;

	private SanityObserverService() {
	}

	public static void tickPlayer(ServerPlayer player, SanityComponent component, SanityThresholds.Stage stage) {
		if (component.getSanity() >= 25) {
			clearMode(player, ObserverEntity.HallucinationMode.OBSERVER);
			return;
		}
		if (!component.isCooldownReady(SanityComponent.Cooldown.OBSERVER)) {
			return;
		}

		RandomSource random = player.getRandom();
		float chance = stage == SanityThresholds.Stage.COLLAPSE ? 0.012F : 0.008F;
		if (!SanityGameplayUtil.roll(random, chance)) {
			return;
		}
		if (!component.tryConsumeEffectBudget(SanityCraftConfig.get(), 2)) {
			return;
		}
		if (spawnHallucination(player, ObserverEntity.HallucinationMode.OBSERVER, false, "runtime")) {
			component.setCooldown(SanityComponent.Cooldown.OBSERVER, SanityGameplayUtil.jitter(OBSERVER_COOLDOWN, random, 20 * 18));
		}
	}

	public static String debugTrigger(ServerPlayer player) {
		return spawnHallucination(player, ObserverEntity.HallucinationMode.OBSERVER, true, "debug")
				? "Triggered observer."
				: "Could not place an observer here.";
	}

	public static boolean spawnCloseCollapseObserver(ServerPlayer player, boolean forced, String source) {
		return spawnHallucination(player, ObserverEntity.HallucinationMode.OBSERVER, forced, source, 8.0D, 18.0D);
	}

	public static void clearPlayer(ServerPlayer player) {
		clearMode(player, ObserverEntity.HallucinationMode.OBSERVER);
		clearMode(player, ObserverEntity.HallucinationMode.DOPPELGANGER);
	}

	static void clearMode(ServerPlayer player, ObserverEntity.HallucinationMode mode) {
		for (ObserverEntity entity : getOwned(player, mode)) {
			entity.discard();
		}
	}

	static boolean spawnHallucination(ServerPlayer player, ObserverEntity.HallucinationMode mode, boolean forced, String source) {
		return spawnHallucination(player, mode, forced, source, Double.NaN, Double.NaN);
	}

	private static boolean spawnHallucination(
			ServerPlayer player,
			ObserverEntity.HallucinationMode mode,
			boolean forced,
			String source,
			double minDistanceOverride,
			double maxDistanceOverride) {
		if (!getOwned(player, mode).isEmpty()) {
			return false;
		}

		RandomSource random = player.getRandom();
		Vec3 spawn = findSpawn(player, mode, random, forced, minDistanceOverride, maxDistanceOverride);
		if (spawn == null) {
			return false;
		}

		var level = (net.minecraft.server.level.ServerLevel) player.level();
		ObserverEntity entity = ModEntities.OBSERVER.create(level, EntitySpawnReason.EVENT);
		if (entity == null) {
			return false;
		}
		entity.setPos(spawn.x, spawn.y, spawn.z);
		entity.configureHallucination(player, mode == ObserverEntity.HallucinationMode.DOPPELGANGER ? 20 * 12 : OBSERVER_LIFETIME, mode);
		level.addFreshEntity(entity);
		SanityDebug.logHallucinationSuccess(player, mode.commandName(), spawn, source);
		return true;
	}

	private static Vec3 findSpawn(ServerPlayer player, ObserverEntity.HallucinationMode mode, RandomSource random, boolean forced, double minDistanceOverride, double maxDistanceOverride) {
		boolean customRange = !Double.isNaN(minDistanceOverride) && !Double.isNaN(maxDistanceOverride);
		double minDistance = mode == ObserverEntity.HallucinationMode.DOPPELGANGER ? 26.0D : 20.0D;
		double maxDistance = mode == ObserverEntity.HallucinationMode.DOPPELGANGER ? 48.0D : 60.0D;
		if (customRange) {
			minDistance = minDistanceOverride;
			maxDistance = Math.max(minDistance + 2.0D, maxDistanceOverride);
		}
		if (forced) {
			minDistance = customRange ? Math.max(6.0D, minDistance - 3.0D) : Math.max(12.0D, minDistance - 8.0D);
			maxDistance = Math.max(minDistance + 6.0D, maxDistance - 10.0D);
		}
		return SanityGameplayUtil.findRingSpawn(
				player,
				random,
				minDistance,
				maxDistance,
				14,
				pos -> {
					if (SanityGameplayUtil.isPlayerFacing(player, SanityGameplayUtil.centerOf(pos), 0.972D)) {
						return false;
					}
					if (mode == ObserverEntity.HallucinationMode.DOPPELGANGER) {
						return SanityGameplayUtil.hasNearbyCover((net.minecraft.server.level.ServerLevel) player.level(), pos)
								|| SanityGameplayUtil.isDimOrCovered((net.minecraft.server.level.ServerLevel) player.level(), pos);
					}
					return SanityGameplayUtil.isDimOrCovered((net.minecraft.server.level.ServerLevel) player.level(), pos)
							&& SanityGameplayUtil.hasNearbyCover((net.minecraft.server.level.ServerLevel) player.level(), pos);
				});
	}

	private static List<ObserverEntity> getOwned(ServerPlayer player, ObserverEntity.HallucinationMode mode) {
		return player.level().getEntitiesOfClass(
				ObserverEntity.class,
				player.getBoundingBox().inflate(96.0D),
				entity -> entity.isAlive() && entity.isHallucinationFor(player, mode));
	}
}
