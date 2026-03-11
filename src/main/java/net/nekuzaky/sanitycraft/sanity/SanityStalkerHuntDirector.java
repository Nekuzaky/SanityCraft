package net.nekuzaky.sanitycraft.sanity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.nekuzaky.sanitycraft.entity.StalkerEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class SanityStalkerHuntDirector {
	private static final Map<UUID, HuntState> STATES = new ConcurrentHashMap<>();

	private SanityStalkerHuntDirector() {
	}

	public static void attach(ServerPlayer target, StalkerEntity stalker, int lifetimeSeconds) {
		HuntState state = new HuntState(target.getUUID(), stalker.getUUID(), Math.max(4, lifetimeSeconds) * 20);
		stalker.addTag("sanitycraft_hallucination");
		stalker.setPersistenceRequired();
		STATES.put(stalker.getUUID(), state);
	}

	public static void tick(ServerPlayer player) {
		int sanity = SanityManager.get(player).getSanity();
		RandomSource random = player.getRandom();

		STATES.entrySet().removeIf(entry -> {
			HuntState state = entry.getValue();
			if (!state.targetId.equals(player.getUUID())) {
				return false;
			}

			if (state.remainingTicks-- <= 0 || sanity > 35) {
				StalkerEntity stalker = findNearPlayer(player, state.stalkerId);
				if (stalker != null && stalker.isAlive()) {
					stalker.discard();
				}
				return true;
			}

			StalkerEntity stalker = findNearPlayer(player, state.stalkerId);
			if (stalker == null || !stalker.isAlive()) {
				return true;
			}
			AttributeInstance attack = stalker.getAttribute(Attributes.ATTACK_DAMAGE);
			if (attack != null && attack.getBaseValue() > 0.0D) {
				attack.setBaseValue(0.0D);
			}

			if (--state.teleportCooldown <= 0) {
				state.teleportCooldown = random.nextIntBetweenInclusive(35, 70);
				teleportNearPlayer(stalker, player, random, false);
			}

			if (isPlayerLookingAt(player, stalker)) {
				state.observeCooldown = random.nextIntBetweenInclusive(26, 50);
				stalker.setInvisible(true);
				teleportNearPlayer(stalker, player, random, true);
			} else if (state.observeCooldown > 0) {
				state.observeCooldown--;
				if (state.observeCooldown <= 0) {
					stalker.setInvisible(false);
				}
			}

			if (--state.phaseCooldown <= 0) {
				state.phaseCooldown = random.nextIntBetweenInclusive(24, 50);
				boolean nowInvisible = !stalker.isInvisible();
				stalker.setInvisible(nowInvisible);
				if (nowInvisible) {
					player.playNotifySound(SoundEvents.ENDERMAN_AMBIENT, SoundSource.HOSTILE, 0.6F, 0.7F + random.nextFloat() * 0.25F);
				} else {
					player.playNotifySound(SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 0.8F, 0.85F + random.nextFloat() * 0.2F);
				}
			}

			stalker.setTarget(player);
			return false;
		});
	}

	private static StalkerEntity findNearPlayer(ServerPlayer player, UUID id) {
		if (id == null) {
			return null;
		}
		return player.level().getEntitiesOfClass(StalkerEntity.class, player.getBoundingBox().inflate(96.0D), e -> e.getUUID().equals(id)).stream().findFirst().orElse(null);
	}

	private static void teleportNearPlayer(StalkerEntity stalker, ServerPlayer player, RandomSource random, boolean flank) {
		double angle;
		double dist;
		if (flank) {
			float yaw = (float) Math.toRadians(player.getYRot());
			float side = random.nextBoolean() ? 1.0F : -1.0F;
			angle = yaw + (Math.PI / 2.0D) * side;
			dist = 6.0D + random.nextDouble() * 4.0D;
		} else {
			angle = random.nextDouble() * Math.PI * 2.0D;
			dist = random.nextDouble() * 6.0D + 4.0D;
		}
		double x = player.getX() + Math.cos(angle) * dist;
		double z = player.getZ() + Math.sin(angle) * dist;
		double y = player.getY();
		stalker.teleportTo(x, y, z);
		player.level().sendParticles(player, ParticleTypes.SMOKE, true, false, x, y + 1.0D, z, 14, 0.3D, 0.6D, 0.3D, 0.01D);
	}

	private static boolean isPlayerLookingAt(ServerPlayer player, StalkerEntity stalker) {
		Vec3 look = player.getLookAngle().normalize();
		Vec3 toStalker = stalker.position().add(0.0D, stalker.getBbHeight() * 0.5D, 0.0D).subtract(player.getEyePosition()).normalize();
		double dot = look.dot(toStalker);
		return dot > 0.90D && player.hasLineOfSight(stalker);
	}

	private static class HuntState {
		private final UUID targetId;
		private final UUID stalkerId;
		private int remainingTicks;
		private int teleportCooldown = 30;
		private int phaseCooldown = 20;
		private int observeCooldown = 0;

		private HuntState(UUID targetId, UUID stalkerId, int remainingTicks) {
			this.targetId = targetId;
			this.stalkerId = stalkerId;
			this.remainingTicks = remainingTicks;
		}
	}
}
