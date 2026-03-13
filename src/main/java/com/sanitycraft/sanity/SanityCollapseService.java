package com.sanitycraft.sanity;

import com.sanitycraft.data.config.SanityCraftConfig;
import com.sanitycraft.entity.bloodycreeper.BloodyCreeperEntity;
import com.sanitycraft.entity.stalker.StalkerEntity;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.phys.Vec3;

public final class SanityCollapseService {
	private static final double OBSERVER_RANGE = 24.0D;
	private static final double VANISH_DISTANCE = 4.5D;
	private static final int HOLD_EFFECT_TICKS = 18;

	private SanityCollapseService() {
	}

	public static void tickPlayer(ServerPlayer player, SanityComponent component, SanityThresholds.Stage stage, SanityCraftConfig config) {
		if (stage != SanityThresholds.Stage.COLLAPSE || component.getSanity() > 0) {
			return;
		}

		ServerLevel level = (ServerLevel) player.level();
		List<Mob> nearbyMobs = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(OBSERVER_RANGE),
				mob -> isObserverCandidate(player, mob));
		if (nearbyMobs.isEmpty()) {
			return;
		}

		RandomSource random = player.getRandom();
		Mob vanishCandidate = null;
		for (Mob mob : nearbyMobs) {
			freezeIntoObserverState(mob, player);
			if (vanishCandidate == null && shouldVanish(player, mob, component, config, random)) {
				vanishCandidate = mob;
			}
		}

		if (vanishCandidate != null) {
			vanishMob(level, player, vanishCandidate);
			component.setCooldown(SanityComponent.Cooldown.OBSERVER_VANISH, 90 + random.nextInt(80));
		}
	}

	private static boolean isObserverCandidate(ServerPlayer player, Mob mob) {
		if (!mob.isAlive() || mob.isRemoved() || mob.isNoAi()) {
			return false;
		}
		if (mob instanceof StalkerEntity || mob instanceof BloodyCreeperEntity) {
			return false;
		}
		if (mob instanceof TamableAnimal tamable && tamable.isTame()) {
			return false;
		}
		return mob.distanceToSqr(player) <= OBSERVER_RANGE * OBSERVER_RANGE;
	}

	private static void freezeIntoObserverState(Mob mob, ServerPlayer player) {
		mob.setTarget(null);
		mob.getNavigation().stop();
		mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, HOLD_EFFECT_TICKS, 5, false, false, false));
		mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, HOLD_EFFECT_TICKS, 2, false, false, false));
		mob.getLookControl().setLookAt(player, 10.0F, 10.0F);
		mob.setDeltaMovement(mob.getDeltaMovement().multiply(0.14D, 1.0D, 0.14D));

		double dx = player.getX() - mob.getX();
		double dz = player.getZ() - mob.getZ();
		float targetYaw = (float) (Math.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
		float bodyYaw = Mth.approachDegrees(mob.getYRot(), targetYaw, 8.0F);
		float headYaw = Mth.approachDegrees(mob.getYHeadRot(), targetYaw, 12.0F);
		mob.setYRot(bodyYaw);
		mob.setYBodyRot(bodyYaw);
		mob.setYHeadRot(headYaw);
	}

	private static boolean shouldVanish(
			ServerPlayer player,
			Mob mob,
			SanityComponent component,
			SanityCraftConfig config,
			RandomSource random) {
		if (!component.isCooldownReady(SanityComponent.Cooldown.OBSERVER_VANISH)) {
			return false;
		}
		if (mob.distanceToSqr(player) > VANISH_DISTANCE * VANISH_DISTANCE) {
			return false;
		}
		if (random.nextFloat() > 0.08F) {
			return false;
		}
		if (mob.hasCustomName() || mob.isPersistenceRequired() || mob.isLeashed() || mob.isPassenger() || mob.isVehicle()) {
			return false;
		}
		if (mob instanceof AbstractVillager) {
			return false;
		}
		return component.tryConsumeEffectBudget(config, 1);
	}

	private static void vanishMob(ServerLevel level, ServerPlayer player, Mob mob) {
		Vec3 position = mob.position().add(0.0D, mob.getBbHeight() * 0.5D, 0.0D);
		level.sendParticles(ParticleTypes.SMOKE, position.x, position.y, position.z, 12, 0.25D, 0.35D, 0.25D, 0.01D);
		level.sendParticles(ParticleTypes.SOUL, position.x, position.y, position.z, 5, 0.18D, 0.24D, 0.18D, 0.0D);
		level.playSound(null, mob.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.32F, 1.55F + player.getRandom().nextFloat() * 0.18F);
		SanityDebug.logEvent(player, "observer_vanish entity=" + mob.getType().getDescriptionId() + " pos=" + position);
		mob.discard();
	}
}
