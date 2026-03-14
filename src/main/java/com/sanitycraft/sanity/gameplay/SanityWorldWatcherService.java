package com.sanitycraft.sanity.gameplay;

import com.sanitycraft.data.config.SanityCraftConfig;
import com.sanitycraft.entity.bloodycreeper.BloodyCreeperEntity;
import com.sanitycraft.entity.stalker.StalkerEntity;
import com.sanitycraft.sanity.SanityComponent;
import com.sanitycraft.sanity.SanityDebug;
import com.sanitycraft.sanity.SanityThresholds;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.npc.AbstractVillager;

public final class SanityWorldWatcherService {
	private static final double WATCH_RANGE = 18.0D;
	private static final int HOLD_TICKS = 24;
	private static final int WATCHER_COOLDOWN = 20 * 8;

	private SanityWorldWatcherService() {
	}

	public static void tickPlayer(ServerPlayer player, SanityComponent component, SanityThresholds.Stage stage) {
		if (component.getSanity() >= 5 || stage != SanityThresholds.Stage.COLLAPSE || !component.isCooldownReady(SanityComponent.Cooldown.WORLD_WATCHER)) {
			return;
		}

		RandomSource random = player.getRandom();
		if (!SanityGameplayUtil.roll(random, 0.055F)) {
			return;
		}
		if (!component.tryConsumeEffectBudget(SanityCraftConfig.get(), 1)) {
			return;
		}

		ServerLevel level = (ServerLevel) player.level();
		List<Mob> nearby = new ArrayList<>(level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(WATCH_RANGE), mob -> isWatcherCandidate(player, mob)));
		if (nearby.isEmpty()) {
			return;
		}

		Collections.shuffle(nearby, new java.util.Random(random.nextLong()));
		int affected = Math.min(1 + random.nextInt(Math.min(3, nearby.size())), nearby.size());
		for (int i = 0; i < affected; i++) {
			freezeIntoWatcherState(nearby.get(i), player);
		}
		component.setCooldown(SanityComponent.Cooldown.WORLD_WATCHER, SanityGameplayUtil.jitter(WATCHER_COOLDOWN, random, 20 * 5));
		SanityDebug.logEvent(player, "world_watcher mobs=" + affected);
	}

	private static boolean isWatcherCandidate(ServerPlayer player, Mob mob) {
		if (!mob.isAlive() || mob.isRemoved() || mob.isNoAi()) {
			return false;
		}
		if (mob instanceof StalkerEntity || mob instanceof BloodyCreeperEntity) {
			return false;
		}
		if (mob instanceof AbstractVillager) {
			return false;
		}
		if (mob instanceof TamableAnimal tamable && tamable.isTame()) {
			return false;
		}
		return mob.distanceToSqr(player) <= WATCH_RANGE * WATCH_RANGE;
	}

	private static void freezeIntoWatcherState(Mob mob, ServerPlayer player) {
		mob.setTarget(null);
		mob.getNavigation().stop();
		mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, HOLD_TICKS, 6, false, false, false));
		mob.getLookControl().setLookAt(player, 12.0F, 12.0F);
		mob.setDeltaMovement(mob.getDeltaMovement().multiply(0.08D, 1.0D, 0.08D));

		double dx = player.getX() - mob.getX();
		double dz = player.getZ() - mob.getZ();
		float targetYaw = (float) (Math.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
		float bodyYaw = Mth.approachDegrees(mob.getYRot(), targetYaw, 10.0F);
		float headYaw = Mth.approachDegrees(mob.getYHeadRot(), targetYaw, 14.0F);
		mob.setYRot(bodyYaw);
		mob.setYBodyRot(bodyYaw);
		mob.setYHeadRot(headYaw);
	}
}
