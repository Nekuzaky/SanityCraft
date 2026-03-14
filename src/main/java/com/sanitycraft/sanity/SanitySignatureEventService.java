package com.sanitycraft.sanity;

import com.sanitycraft.sanity.events.SanityEventContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public final class SanitySignatureEventService {
	private static final Map<UUID, SilentWorldState> ACTIVE_SILENT_WORLD = new ConcurrentHashMap<>();
	private static final Map<UUID, ActivityState> PLAYER_ACTIVITY = new ConcurrentHashMap<>();
	private static final int MIN_ACTIVE_WORLD_TICKS = 20 * 60 * 4;
	private static final int RECENT_MOVEMENT_TICKS = 20 * 18;
	private static final int SILENT_WORLD_RADIUS = 40;
	private static final int MAX_FROZEN_MOBS = 10;
	private static final int MAX_OBSERVING_MOBS = 5;

	private SanitySignatureEventService() {
	}

	public static void tickPlayer(ServerPlayer player, SanityComponent component, long gameTime) {
		updateActivity(player, gameTime);

		SilentWorldState state = ACTIVE_SILENT_WORLD.get(player.getUUID());
		if (state == null) {
			return;
		}
		if (!player.isAlive() || component.hasHallucinationShield() || gameTime >= state.endTick()) {
			ACTIVE_SILENT_WORLD.remove(player.getUUID());
			return;
		}

		ServerLevel level = (ServerLevel) player.level();
		for (int entityId : state.frozenMobIds()) {
			Entity entity = level.getEntity(entityId);
			if (!(entity instanceof Mob mob) || !mob.isAlive() || mob.distanceToSqr(player) > (SILENT_WORLD_RADIUS + 8.0D) * (SILENT_WORLD_RADIUS + 8.0D)) {
				continue;
			}

			// Refresh the immobilization often enough to keep the world feeling unnaturally still.
			mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 16, 7, false, false, false));
			mob.setDeltaMovement(Vec3.ZERO);
			mob.setTarget(null);
			mob.getNavigation().stop();
			mob.xxa = 0.0F;
			mob.yya = 0.0F;
			mob.zza = 0.0F;

			if (gameTime < state.observationTick() || !state.observingMobIds().contains(entityId)) {
				continue;
			}

			mob.getLookControl().setLookAt(player, 18.0F, 18.0F);
			double dx = player.getX() - mob.getX();
			double dz = player.getZ() - mob.getZ();
			float targetYaw = (float) (Math.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
			float yaw = Mth.approachDegrees(mob.getYRot(), targetYaw, 7.0F);
			mob.setYRot(yaw);
			mob.setYHeadRot(yaw);
			mob.setYBodyRot(yaw);
		}
	}

	public static boolean isSilentWorldActive(ServerPlayer player) {
		return ACTIVE_SILENT_WORLD.containsKey(player.getUUID());
	}

	public static boolean canTriggerSilentWorld(SanityEventContext context) {
		if (context.sanity() > 5 || isSilentWorldActive(context.player())) {
			return false;
		}

		ActivityState activity = PLAYER_ACTIVITY.get(context.player().getUUID());
		if (activity == null || activity.activeWorldTicks() < MIN_ACTIVE_WORLD_TICKS || context.gameTime() - activity.lastMovementTick() > RECENT_MOVEMENT_TICKS) {
			return false;
		}

		return collectNearbyMobs(context.player()).size() >= 2;
	}

	public static boolean triggerSilentWorld(SanityEventContext context) {
		if (!canTriggerSilentWorld(context)) {
			return false;
		}

		List<Mob> nearbyMobs = collectNearbyMobs(context.player());
		if (nearbyMobs.size() < 2) {
			return false;
		}

		Vec3 presence = findPresenceSpawn(context.player(), context.random());
		if (presence == null) {
			return false;
		}

		int silenceTicks = 40 + context.random().nextInt(21);
		int observationTicks = 18 + context.random().nextInt(11);
		int presenceTicks = 18 + context.random().nextInt(21);
		int returnTicks = 18;
		int totalDuration = silenceTicks + observationTicks + presenceTicks + returnTicks;

		List<Integer> frozenMobIds = pickMobIds(nearbyMobs, context.random(), MAX_FROZEN_MOBS);
		List<Integer> observingMobIds = new ArrayList<>(frozenMobIds);
		while (observingMobIds.size() > MAX_OBSERVING_MOBS) {
			observingMobIds.remove(context.random().nextInt(observingMobIds.size()));
		}

		ACTIVE_SILENT_WORLD.put(
				context.player().getUUID(),
				new SilentWorldState(
						context.gameTime(),
						context.gameTime() + totalDuration,
						context.gameTime() + silenceTicks,
						context.gameTime() + silenceTicks + observationTicks,
						frozenMobIds,
						observingMobIds));

		return context.sendClientEvent(
				"silent_world",
				BlockPos.containing(presence),
				totalDuration,
				silenceTicks,
				context.random().nextInt(),
				"",
				"silent_world");
	}

	public static void clearPlayer(ServerPlayer player) {
		ACTIVE_SILENT_WORLD.remove(player.getUUID());
	}

	public static void clearPlayerHistory(ServerPlayer player) {
		PLAYER_ACTIVITY.remove(player.getUUID());
	}

	public static void clearAll() {
		ACTIVE_SILENT_WORLD.clear();
		PLAYER_ACTIVITY.clear();
	}

	private static void updateActivity(ServerPlayer player, long gameTime) {
		ActivityState activity = PLAYER_ACTIVITY.get(player.getUUID());
		Vec3 position = player.position();
		if (activity == null) {
			PLAYER_ACTIVITY.put(player.getUUID(), new ActivityState(position, gameTime, 1));
			return;
		}

		long lastMovementTick = activity.lastMovementTick();
		if (position.distanceToSqr(activity.lastPosition()) > 0.0025D) {
			lastMovementTick = gameTime;
		}

		PLAYER_ACTIVITY.put(player.getUUID(), new ActivityState(position, lastMovementTick, activity.activeWorldTicks() + 1));
	}

	private static List<Mob> collectNearbyMobs(ServerPlayer player) {
		return new ArrayList<>(player.level().getEntitiesOfClass(
				Mob.class,
				player.getBoundingBox().inflate(SILENT_WORLD_RADIUS),
				mob -> mob.isAlive() && !mob.isNoAi() && mob.distanceToSqr(player) > 9.0D));
	}

	private static List<Integer> pickMobIds(List<Mob> mobs, RandomSource random, int maxCount) {
		List<Mob> pool = new ArrayList<>(mobs);
		List<Integer> selected = new ArrayList<>();
		while (!pool.isEmpty() && selected.size() < maxCount) {
			selected.add(pool.remove(random.nextInt(pool.size())).getId());
		}
		return selected;
	}

	private static Vec3 findPresenceSpawn(ServerPlayer player, RandomSource random) {
		ServerLevel level = (ServerLevel) player.level();
		Vec3 look = player.getLookAngle().normalize();
		Vec3 side = new Vec3(-look.z, 0.0D, look.x);
		if (side.lengthSqr() <= 1.0E-4D) {
			side = new Vec3(1.0D, 0.0D, 0.0D);
		} else {
			side = side.normalize();
		}

		for (int attempt = 0; attempt < 10; attempt++) {
			double sign = random.nextBoolean() ? 1.0D : -1.0D;
			double forwardBias = 0.12D + random.nextDouble() * 0.16D;
			Vec3 direction = side.scale(sign).add(look.scale(forwardBias)).normalize();
			double distance = 14.0D + random.nextDouble() * 10.0D;
			BlockPos probe = BlockPos.containing(player.position().add(direction.scale(distance)));
			if (!level.hasChunkAt(probe) || isDirectlyFaced(player, centerOf(probe), 0.90D)) {
				continue;
			}

			Vec3 stand = findStandablePosition(level, probe);
			if (stand != null) {
				return stand;
			}
		}

		return null;
	}

	private static boolean isDirectlyFaced(ServerPlayer player, Vec3 target, double threshold) {
		Vec3 look = player.getLookAngle().normalize();
		Vec3 toTarget = target.subtract(player.getEyePosition()).normalize();
		return look.dot(toTarget) >= threshold;
	}

	private static Vec3 findStandablePosition(ServerLevel level, BlockPos base) {
		for (int dy = 4; dy >= -5; dy--) {
			BlockPos feet = base.offset(0, dy, 0);
			BlockPos head = feet.above();
			BlockPos below = feet.below();
			if (!level.hasChunkAt(feet)) {
				continue;
			}
			if (!level.getBlockState(feet).blocksMotion()
					&& !level.getBlockState(head).blocksMotion()
					&& level.getBlockState(below).blocksMotion()) {
				return new Vec3(feet.getX() + 0.5D, feet.getY(), feet.getZ() + 0.5D);
			}
		}
		return null;
	}

	private static Vec3 centerOf(BlockPos pos) {
		return new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
	}

	private record SilentWorldState(
			long startTick,
			long endTick,
			long silenceEndTick,
			long observationTick,
			List<Integer> frozenMobIds,
			List<Integer> observingMobIds) {
	}

	private record ActivityState(Vec3 lastPosition, long lastMovementTick, int activeWorldTicks) {
	}
}
