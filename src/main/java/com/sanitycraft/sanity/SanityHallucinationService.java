package com.sanitycraft.sanity;

import com.sanitycraft.data.config.SanityCraftConfig;
import com.sanitycraft.entity.bloodycreeper.BloodyCreeperEntity;
import com.sanitycraft.entity.stalker.StalkerEntity;
import com.sanitycraft.network.sync.ClientEffectSyncService;
import com.sanitycraft.registry.ModEntities;
import com.sanitycraft.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SanityHallucinationService {
	private SanityHallucinationService() {
	}

	public static void tickPlayer(ServerPlayer player, SanityComponent component, SanityThresholds.Stage stage, SanityCraftConfig config) {
		if (component.hasHallucinationShield()) {
			dispelOwnedHallucinations(player);
			return;
		}

		switch (stage) {
			case STABLE -> dispelOwnedHallucinations(player);
			case DISTURBED -> {
				spawnBloodTrace(player, component, config, config.events.bloodParticleChance, false, "stage_disturbed");
				spawnStalker(player, component, config, config.events.disturbedHallucinationChance, false, "stage_disturbed");
			}
			case FRACTURED -> {
				spawnBloodTrace(player, component, config, config.events.bloodParticleChance + 0.03F, false, "stage_fractured");
				spawnStalker(player, component, config, config.events.disturbedHallucinationChance + 0.03F, false, "stage_fractured");
				spawnBloodyCreeper(player, component, config, config.events.disturbedHallucinationChance * 0.55F, false, "stage_fractured");
				maybeTriggerFlicker(player, component, config, config.events.fracturedFlickerChance, 20, 2);
			}
			case COLLAPSE -> {
				spawnBloodTrace(player, component, config, config.events.bloodParticleChance + 0.06F, false, "stage_collapse");
				spawnStalker(player, component, config, config.events.collapseHallucinationChance, false, "stage_collapse");
				spawnBloodyCreeper(player, component, config, config.events.collapseHallucinationChance * 0.8F, false, "stage_collapse");
				maybeTriggerFlicker(player, component, config, config.events.fracturedFlickerChance + 0.06F, 30, 4);
			}
			default -> {
			}
		}
	}

	public static String forceHallucination(ServerPlayer player) {
		SanityComponent component = SanityManager.get(player);
		SanityCraftConfig config = SanityCraftConfig.get();
		SanityThresholds.Stage stage = SanityThresholds.resolve(component.getSanity(), config);
		dispelOwnedHallucinations(player);
		spawnBloodTrace(player, component, config, 1.0F, true, "debug_force_hallucination");
		if (stage == SanityThresholds.Stage.FRACTURED || stage == SanityThresholds.Stage.COLLAPSE) {
			if (spawnBloodyCreeper(player, component, config, 1.0F, true, "debug_force_hallucination")) {
				return "bloodycreeper";
			}
		}
		if (spawnStalker(player, component, config, 1.0F, true, "debug_force_hallucination")) {
			return "stalker";
		}
		return "particles";
	}

	public static boolean forceStalker(ServerPlayer player) {
		dispelOwnedHallucinations(player);
		return spawnStalker(player, SanityManager.get(player), SanityCraftConfig.get(), 1.0F, true, "debug_force_stalker");
	}

	public static boolean spawnWatcherStalker(ServerPlayer player, Vec3 position, int lifetimeTicks, String source) {
		if (hasOwnedStalker(player)) {
			return false;
		}
		ServerLevel level = player.level();
		StalkerEntity stalker = ModEntities.STALKER.create(level, EntitySpawnReason.EVENT);
		if (stalker == null) {
			SanityDebug.logHallucinationAttempt(player, "stalker_watcher", source, "entity_creation_failed");
			return false;
		}
		stalker.setPos(position.x, position.y, position.z);
		stalker.configureHallucination(player, lifetimeTicks, true);
		level.addFreshEntity(stalker);
		SanityDebug.logHallucinationSuccess(player, "stalker_watcher", position, source);
		return true;
	}

	public static boolean forceBloodyCreeper(ServerPlayer player) {
		dispelOwnedHallucinations(player);
		spawnBloodTrace(player, SanityManager.get(player), SanityCraftConfig.get(), 1.0F, true, "debug_force_bloodycreeper");
		return spawnBloodyCreeper(player, SanityManager.get(player), SanityCraftConfig.get(), 1.0F, true, "debug_force_bloodycreeper");
	}

	public static boolean forceBloodTrace(ServerPlayer player) {
		return spawnBloodTrace(player, SanityManager.get(player), SanityCraftConfig.get(), 1.0F, true, "debug_force_particles");
	}

	public static void dispelOwnedHallucinations(ServerPlayer player) {
		AABB area = player.getBoundingBox().inflate(64.0D);
		for (StalkerEntity stalker : player.level().getEntitiesOfClass(StalkerEntity.class, area, entity -> entity.isAlive() && entity.isHallucinationFor(player))) {
			stalker.discard();
		}
		for (BloodyCreeperEntity creeper : player.level().getEntitiesOfClass(BloodyCreeperEntity.class, area, entity -> entity.isAlive() && entity.isHallucinationFor(player))) {
			creeper.discard();
		}
	}

	private static boolean spawnStalker(
			ServerPlayer player,
			SanityComponent component,
			SanityCraftConfig config,
			float chance,
			boolean forced,
			String source) {
		SanityDebug.logHallucinationAttempt(player, "stalker", source, forced ? "forced" : "scheduled");
		if (!forced && !component.isCooldownReady(SanityComponent.Cooldown.STALKER)) {
			SanityDebug.logHallucinationAttempt(player, "stalker", source, "cooldown_blocked");
			return false;
		}
		if (!forced && !isSpawnSafeForPrivateHallucination(player, config)) {
			SanityDebug.logHallucinationAttempt(player, "stalker", source, "private_radius_blocked");
			return false;
		}
		if (!forced && !hasSpawnCover(player, config)) {
			SanityDebug.logHallucinationAttempt(player, "stalker", source, "no_cover");
			return false;
		}
		if (!forced && hasOwnedStalker(player)) {
			SanityDebug.logHallucinationAttempt(player, "stalker", source, "already_active");
			return false;
		}

		RandomSource random = player.getRandom();
		if (!forced && !roll(random, chance)) {
			SanityDebug.logHallucinationAttempt(player, "stalker", source, "chance_miss");
			return false;
		}
		if (!forced && !component.tryConsumeEffectBudget(config, 2)) {
			SanityDebug.logHallucinationAttempt(player, "stalker", source, "budget_blocked");
			return false;
		}

		ServerLevel level = player.level();
		Vec3 spawn = findSpawnPosition(player, random, forced ? 5.0D : 8.0D, forced ? 10.0D : 16.0D);
		StalkerEntity stalker = ModEntities.STALKER.create(level, EntitySpawnReason.EVENT);
		if (stalker == null) {
			SanityDebug.logHallucinationAttempt(player, "stalker", source, "entity_creation_failed");
			return false;
		}
		stalker.setPos(spawn.x, spawn.y, spawn.z);
		stalker.configureHallucination(player, config.events.stalkerLifetimeTicks);
		level.addFreshEntity(stalker);
		if (!forced) {
			component.setCooldown(SanityComponent.Cooldown.STALKER, jitter(config.events.stalkerCooldownTicks, random));
		}
		SanityDebug.logHallucinationSuccess(player, "stalker", spawn, source);
		return true;
	}

	private static boolean spawnBloodyCreeper(
			ServerPlayer player,
			SanityComponent component,
			SanityCraftConfig config,
			float chance,
			boolean forced,
			String source) {
		SanityDebug.logHallucinationAttempt(player, "bloodycreeper", source, forced ? "forced" : "scheduled");
		if (!forced && !component.isCooldownReady(SanityComponent.Cooldown.BLOODY_CREEPER)) {
			SanityDebug.logHallucinationAttempt(player, "bloodycreeper", source, "cooldown_blocked");
			return false;
		}
		if (!forced && !isSpawnSafeForPrivateHallucination(player, config)) {
			SanityDebug.logHallucinationAttempt(player, "bloodycreeper", source, "private_radius_blocked");
			return false;
		}
		if (!forced && hasOwnedBloodyCreeper(player)) {
			SanityDebug.logHallucinationAttempt(player, "bloodycreeper", source, "already_active");
			return false;
		}

		RandomSource random = player.getRandom();
		if (!forced && !roll(random, chance)) {
			SanityDebug.logHallucinationAttempt(player, "bloodycreeper", source, "chance_miss");
			return false;
		}
		if (!forced && !component.tryConsumeEffectBudget(config, 2)) {
			SanityDebug.logHallucinationAttempt(player, "bloodycreeper", source, "budget_blocked");
			return false;
		}

		ServerLevel level = player.level();
		Vec3 spawn = findSpawnPosition(player, random, forced ? 4.0D : 6.0D, forced ? 8.0D : 12.0D);
		BloodyCreeperEntity creeper = ModEntities.BLOODY_CREEPER.create(level, EntitySpawnReason.EVENT);
		if (creeper == null) {
			SanityDebug.logHallucinationAttempt(player, "bloodycreeper", source, "entity_creation_failed");
			return false;
		}
		creeper.setPos(spawn.x, spawn.y, spawn.z);
		creeper.configureHallucination(player, config.events.bloodyCreeperLifetimeTicks);
		level.addFreshEntity(creeper);
		if (!forced) {
			component.setCooldown(SanityComponent.Cooldown.BLOODY_CREEPER, jitter(config.events.bloodyCreeperCooldownTicks, random));
		}
		SanityDebug.logHallucinationSuccess(player, "bloodycreeper", spawn, source);
		return true;
	}

	private static boolean spawnBloodTrace(
			ServerPlayer player,
			SanityComponent component,
			SanityCraftConfig config,
			float chance,
			boolean forced,
			String source) {
		SanityDebug.logHallucinationAttempt(player, "blood_trace", source, forced ? "forced" : "scheduled");
		if (!forced && !component.isCooldownReady(SanityComponent.Cooldown.PARTICLES)) {
			SanityDebug.logHallucinationAttempt(player, "blood_trace", source, "cooldown_blocked");
			return false;
		}

		RandomSource random = player.getRandom();
		if (!forced && !roll(random, chance)) {
			SanityDebug.logHallucinationAttempt(player, "blood_trace", source, "chance_miss");
			return false;
		}
		if (!forced && !component.tryConsumeEffectBudget(config, 1)) {
			SanityDebug.logHallucinationAttempt(player, "blood_trace", source, "budget_blocked");
			return false;
		}

		ServerLevel level = player.level();
		Vec3 at = findSpawnPosition(player, random, 3.0D, 7.0D);
		level.sendParticles(player, ParticleTypes.SMOKE, true, false, at.x, at.y + 0.1D, at.z, 8, 0.22D, 0.12D, 0.22D, 0.01D);
		level.sendParticles(player, ModParticles.BLOOD, true, false, at.x, at.y + 0.2D, at.z, 12, 0.18D, 0.08D, 0.18D, 0.01D);
		if (!forced) {
			component.setCooldown(SanityComponent.Cooldown.PARTICLES, jitter(config.events.particleCooldownTicks, random));
		}
		SanityDebug.logParticleSpawn(player, "blood", at, 12);
		return true;
	}

	private static void maybeTriggerFlicker(
			ServerPlayer player,
			SanityComponent component,
			SanityCraftConfig config,
			float chance,
			int darknessDurationTicks,
			int scareIntensity) {
		if (!component.isCooldownReady(SanityComponent.Cooldown.FLICKER)) {
			return;
		}
		RandomSource random = player.getRandom();
		if (!roll(random, chance)) {
			return;
		}
		if (!component.tryConsumeEffectBudget(config, 1)) {
			return;
		}

		player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, darknessDurationTicks, 0, true, false, false));
		ClientEffectSyncService.sendScarePulse(player, Math.max(8, darknessDurationTicks / 2), scareIntensity, "paranoia_flicker", false);
		component.setCooldown(SanityComponent.Cooldown.FLICKER, jitter(config.events.flickerCooldownTicks, random));
		SanityDebug.logEvent(player, "Paranoia flicker triggered duration=" + darknessDurationTicks + " intensity=" + scareIntensity);
	}

	private static boolean isSpawnSafeForPrivateHallucination(ServerPlayer player, SanityCraftConfig config) {
		if (!config.multiplayer.privateHallucinations || player.getServer() == null) {
			return true;
		}
		double radiusSquared = (double) config.events.privateHallucinationRadius * config.events.privateHallucinationRadius;
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

	private static boolean hasSpawnCover(ServerPlayer player, SanityCraftConfig config) {
		BlockPos pos = player.blockPosition();
		ServerLevel level = player.level();
		return !level.canSeeSky(pos)
				|| level.isRainingAt(pos.above())
				|| level.getBrightness(LightLayer.BLOCK, pos) <= config.decay.darknessLightThreshold;
	}

	private static boolean hasOwnedStalker(ServerPlayer player) {
		return !player.level()
				.getEntitiesOfClass(StalkerEntity.class, player.getBoundingBox().inflate(40.0D), entity -> entity.isAlive() && entity.isHallucinationFor(player))
				.isEmpty();
	}

	private static boolean hasOwnedBloodyCreeper(ServerPlayer player) {
		return !player.level()
				.getEntitiesOfClass(BloodyCreeperEntity.class, player.getBoundingBox().inflate(40.0D), entity -> entity.isAlive() && entity.isHallucinationFor(player))
				.isEmpty();
	}

	private static Vec3 findSpawnPosition(ServerPlayer player, RandomSource random, double minDistance, double maxDistance) {
		BlockPos center = player.blockPosition();
		for (int attempt = 0; attempt < 8; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2.0D;
			double distance = minDistance + random.nextDouble() * Math.max(1.0D, maxDistance - minDistance);
			int x = Mth.floor(player.getX() + Math.cos(angle) * distance);
			int z = Mth.floor(player.getZ() + Math.sin(angle) * distance);
			BlockPos base = new BlockPos(x, center.getY(), z);
			Vec3 safe = findStandablePosition(player, base);
			if (safe != null) {
				return safe;
			}
		}
		return player.position().add(0.0D, 1.0D, 0.0D);
	}

	private static Vec3 findStandablePosition(ServerPlayer player, BlockPos base) {
		for (int dy = 3; dy >= -4; dy--) {
			BlockPos feet = base.offset(0, dy, 0);
			BlockPos head = feet.above();
			BlockPos below = feet.below();
			if (!player.level().getBlockState(feet).blocksMotion()
					&& !player.level().getBlockState(head).blocksMotion()
					&& player.level().getBlockState(below).blocksMotion()) {
				return new Vec3(feet.getX() + 0.5D, feet.getY(), feet.getZ() + 0.5D);
			}
		}
		return null;
	}

	private static boolean roll(RandomSource random, float chance) {
		return random.nextFloat() < Mth.clamp(chance, 0.0F, 1.0F);
	}

	private static int jitter(int baseTicks, RandomSource random) {
		int safeBase = Math.max(1, baseTicks);
		int variance = Math.max(1, safeBase / 4);
		return Math.max(1, safeBase - variance + random.nextInt(variance * 2 + 1));
	}
}
