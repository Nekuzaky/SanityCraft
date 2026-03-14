package com.sanitycraft.client.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sanitycraft.entity.observer.ObserverEntity;
import com.sanitycraft.registry.ModEntities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public final class SanityHallucinatedEntityManager {
	private static final List<ManagedHallucination> HALLUCINATIONS = new ArrayList<>();
	private static final Map<Integer, VisualOverride> VISUAL_OVERRIDES = new HashMap<>();
	private static int nextHallucinationEntityId = -60_000;

	private SanityHallucinatedEntityManager() {
	}

	public static void spawnSilentWorldPresence(Minecraft client, BlockPos anchor, long now, int durationTicks, int variant) {
		if (client.level == null) {
			return;
		}

		removeMatching(client.level, PresenceHallucination.class);
		ObserverEntity observer = new ObserverEntity(ModEntities.OBSERVER, client.level);
		observer.setId(nextEntityId());
		observer.setPos(anchor.getX() + 0.5D, anchor.getY(), anchor.getZ() + 0.5D);
		observer.noPhysics = true;
		observer.setSilent(true);
		observer.setNoAi(true);
		observer.setYRot((variant & 1) == 0 ? -90.0F : 90.0F);
		observer.setYHeadRot(observer.getYRot());
		client.level.addEntity(observer);
		HALLUCINATIONS.add(new PresenceHallucination(observer, now + Math.max(20, durationTicks)));
	}

	public static void spawnAlmostMob(Minecraft client, BlockPos anchor, long now, int durationTicks, int variant, String mobType) {
		if (client.level == null) {
			return;
		}

		removeMatching(client.level, AlmostMobHallucination.class);
		Mob mob = createMob(client.level, mobType);
		if (mob == null) {
			return;
		}

		mob.setId(nextEntityId());
		mob.setPos(anchor.getX() + 0.5D, anchor.getY(), anchor.getZ() + 0.5D);
		mob.noPhysics = true;
		mob.setSilent(true);
		mob.setNoAi(true);
		mob.setYRot((variant & 1) == 0 ? 180.0F : 0.0F);
		mob.setYHeadRot(mob.getYRot());
		mob.setYBodyRot(mob.getYRot());
		client.level.addEntity(mob);
		applyMobAnomaly(mob, mobType, variant);
		HALLUCINATIONS.add(new AlmostMobHallucination(mob, now + Math.max(40, durationTicks)));
	}

	public static void tick(Minecraft client, long now) {
		if (client.level == null || client.player == null) {
			clear(client.level);
			return;
		}

		Iterator<ManagedHallucination> iterator = HALLUCINATIONS.iterator();
		while (iterator.hasNext()) {
			ManagedHallucination hallucination = iterator.next();
			if (hallucination.entity().level() != client.level || !hallucination.tick(client, now)) {
				hallucination.remove(client.level);
				iterator.remove();
			}
		}
	}

	public static void clear(ClientLevel level) {
		for (ManagedHallucination hallucination : HALLUCINATIONS) {
			hallucination.remove(level);
		}
		HALLUCINATIONS.clear();
		VISUAL_OVERRIDES.clear();
	}

	public static boolean pushRenderTransform(Entity entity, PoseStack poseStack) {
		VisualOverride override = VISUAL_OVERRIDES.get(entity.getId());
		if (override == null) {
			return false;
		}
		poseStack.pushPose();
		poseStack.scale(override.scaleX(), override.scaleY(), override.scaleZ());
		return true;
	}

	private static void removeMatching(ClientLevel level, Class<? extends ManagedHallucination> type) {
		Iterator<ManagedHallucination> iterator = HALLUCINATIONS.iterator();
		while (iterator.hasNext()) {
			ManagedHallucination hallucination = iterator.next();
			if (!type.isInstance(hallucination)) {
				continue;
			}
			hallucination.remove(level);
			iterator.remove();
		}
	}

	private static Mob createMob(ClientLevel level, String mobType) {
		return switch (mobType == null ? "" : mobType) {
			case "skeleton" -> new Skeleton(EntityType.SKELETON, level);
			case "cow" -> new Cow(EntityType.COW, level);
			default -> new Zombie(EntityType.ZOMBIE, level);
		};
	}

	private static void applyMobAnomaly(Mob mob, String mobType, int variant) {
		if (mob instanceof Skeleton skeleton) {
			skeleton.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			VISUAL_OVERRIDES.put(skeleton.getId(), new VisualOverride(1.0F, 1.0F, 1.0F));
			return;
		}
		if (mob instanceof Zombie zombie) {
			float extraHeight = 1.08F + Math.abs((variant % 5) * 0.01F);
			VISUAL_OVERRIDES.put(zombie.getId(), new VisualOverride(1.01F, extraHeight, 1.01F));
			return;
		}
		if (mob instanceof Cow cow) {
			float narrow = "cow".equals(mobType) ? 0.98F : 1.0F;
			VISUAL_OVERRIDES.put(cow.getId(), new VisualOverride(narrow, 1.0F, narrow));
		}
	}

	private static int nextEntityId() {
		return nextHallucinationEntityId--;
	}

	private static boolean isDirectlyLookedAt(Minecraft client, Entity entity, double threshold) {
		Vec3 look = client.player.getLookAngle().normalize();
		Vec3 toEntity = entity.position().add(0.0D, entity.getBbHeight() * 0.6D, 0.0D).subtract(client.player.getEyePosition()).normalize();
		return look.dot(toEntity) >= threshold && client.player.hasLineOfSight(entity);
	}

	private static void keepStill(Mob mob) {
		mob.noPhysics = true;
		mob.setDeltaMovement(Vec3.ZERO);
		mob.xxa = 0.0F;
		mob.yya = 0.0F;
		mob.zza = 0.0F;
	}

	private abstract static class ManagedHallucination {
		private final Entity entity;
		private final long endTick;

		private ManagedHallucination(Entity entity, long endTick) {
			this.entity = entity;
			this.endTick = endTick;
		}

		protected Entity entity() {
			return entity;
		}

		protected long endTick() {
			return endTick;
		}

		protected boolean expired(long now) {
			return now > endTick;
		}

		abstract boolean tick(Minecraft client, long now);

		void remove(ClientLevel level) {
			VISUAL_OVERRIDES.remove(entity.getId());
			if (level != null) {
				level.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED);
			}
		}
	}

	private static final class PresenceHallucination extends ManagedHallucination {
		private final RandomSource random = RandomSource.create();

		private PresenceHallucination(ObserverEntity entity, long endTick) {
			super(entity, endTick);
		}

		@Override
		boolean tick(Minecraft client, long now) {
			if (expired(now)) {
				return false;
			}

			ObserverEntity observer = (ObserverEntity) entity();
			observer.noPhysics = true;
			observer.setNoAi(true);
			if ((now & 7L) == 0L) {
				float sway = (float) Math.sin((now + random.nextInt(5)) * 0.17F) * 4.0F;
				observer.setYRot(Mth.wrapDegrees(observer.getYRot() + sway));
				observer.setYHeadRot(observer.getYRot());
				observer.setYBodyRot(observer.getYRot());
			}
			return !isDirectlyLookedAt(client, observer, 0.965D);
		}
	}

	private static final class AlmostMobHallucination extends ManagedHallucination {
		private AlmostMobHallucination(Mob entity, long endTick) {
			super(entity, endTick);
		}

		@Override
		boolean tick(Minecraft client, long now) {
			if (expired(now)) {
				return false;
			}

			Mob mob = (Mob) entity();
			keepStill(mob);
			return mob.distanceToSqr(client.player) > 6.5D * 6.5D;
		}
	}

	private record VisualOverride(float scaleX, float scaleY, float scaleZ) {
	}
}
