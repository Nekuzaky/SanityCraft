package com.sanitycraft.sanity;

import com.sanitycraft.data.config.SanityCraftConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class SanityTriggerSampler {
	private SanityTriggerSampler() {
	}

	public static SanityTriggers sampleTriggers(ServerPlayer player, SanityCraftConfig config) {
		ServerLevel level = player.level();
		BlockPos pos = player.blockPosition();
		return new SanityTriggers(
				isDark(level, pos, config),
				isLowHealth(player, config),
				hasNearbyHostiles(player, config.decay.hostileRadius),
				isUnderground(level, pos),
				isIsolated(player, config.decay.isolationRadius),
				isNight(level),
				false,
				false,
				false,
				false,
				hasWeatherStress(level, pos),
				isLowFood(player, config));
	}

	public static SanityRecovery sampleRecovery(ServerPlayer player, SanityCraftConfig config) {
		ServerLevel level = player.level();
		BlockPos pos = player.blockPosition();
		boolean safeZone = hasCampfireNearby(level, pos, config.recovery.campfireRadius);
		return new SanityRecovery(
				safeZone,
				level.getBrightness(LightLayer.BLOCK, pos) >= config.recovery.lightLevelThreshold,
				player.isSleeping(),
				false,
				false,
				safeZone,
				level.canSeeSky(pos) && !isNight(level),
				!level.isRaining() && !level.isThundering());
	}

	private static boolean isDark(ServerLevel level, BlockPos pos, SanityCraftConfig config) {
		return level.getBrightness(LightLayer.BLOCK, pos) <= config.decay.darknessLightThreshold
				&& level.getBrightness(LightLayer.SKY, pos) <= config.decay.darknessLightThreshold;
	}

	private static boolean isUnderground(ServerLevel level, BlockPos pos) {
		return !level.canSeeSky(pos) && pos.getY() < level.getSeaLevel() + 2;
	}

	private static boolean isNight(ServerLevel level) {
		long timeOfDay = level.getDayTime() % 24000L;
		return timeOfDay >= 13000L && timeOfDay <= 23000L;
	}

	private static boolean hasNearbyHostiles(ServerPlayer player, int radius) {
		AABB area = player.getBoundingBox().inflate(Math.max(1, radius));
		return !player.level().getEntitiesOfClass(Monster.class, area, Monster::isAlive).isEmpty();
	}

	private static boolean isIsolated(ServerPlayer player, int radius) {
		if (player.getServer() == null) {
			return false;
		}
		double radiusSquared = Math.max(1, radius) * Math.max(1, radius);
		for (ServerPlayer other : player.getServer().getPlayerList().getPlayers()) {
			if (other == player || other.isSpectator() || !other.level().dimension().equals(player.level().dimension())) {
				continue;
			}
			if (other.distanceToSqr(player) <= radiusSquared) {
				return false;
			}
		}
		return true;
	}

	private static boolean isLowHealth(ServerPlayer player, SanityCraftConfig config) {
		return player.getHealth() / Math.max(1.0F, player.getMaxHealth()) <= config.decay.lowHealthThreshold;
	}

	private static boolean isLowFood(ServerPlayer player, SanityCraftConfig config) {
		return player.getFoodData().getFoodLevel() <= config.decay.lowFoodThreshold;
	}

	private static boolean hasWeatherStress(ServerLevel level, BlockPos pos) {
		return level.isThundering() || level.isRainingAt(pos.above());
	}

	private static boolean hasCampfireNearby(ServerLevel level, BlockPos center, int radius) {
		int safeRadius = Math.max(1, radius);
		for (int x = -safeRadius; x <= safeRadius; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -safeRadius; z <= safeRadius; z++) {
					BlockState state = level.getBlockState(center.offset(x, y, z));
					if (state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
