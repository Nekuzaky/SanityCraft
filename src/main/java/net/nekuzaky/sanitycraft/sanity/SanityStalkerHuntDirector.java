package net.nekuzaky.sanitycraft.sanity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.nekuzaky.sanitycraft.entity.StalkerEntity;

public class SanityStalkerHuntDirector {
	private static final Map<UUID, HuntState> STATES = new ConcurrentHashMap<>();

	private SanityStalkerHuntDirector() {
	}

	public static void attach(ServerPlayer target, StalkerEntity stalker, int lifetimeSeconds) {
		HuntState state = new HuntState(target.getUUID(), stalker.getUUID(), Math.max(4, lifetimeSeconds) * 20);
		stalker.addTag("sanitycraft_hallucination");
		stalker.setPersistenceRequired();
		stalker.setTarget(target);
		STATES.put(stalker.getUUID(), state);
	}

	public static void tick(ServerPlayer player) {
		int sanity = SanityManager.get(player).getSanity();
		PlayerSanityComponent component = SanityManager.get(player);
		SanityConfig config = SanityManager.getConfig();
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
			state.tickCooldowns();

			AttributeInstance attack = stalker.getAttribute(Attributes.ATTACK_DAMAGE);
			if (attack != null && attack.getBaseValue() > 0.0D) {
				attack.setBaseValue(0.0D);
			}
			stalker.setTarget(player);

			double distanceSq = stalker.distanceToSqr(player);
			boolean looking = isPlayerLookingAt(player, stalker);
			boolean lineOfSight = player.hasLineOfSight(stalker);

			if (distanceSq > 28.0D * 28.0D || state.repositionCooldown <= 0) {
				state.repositionCooldown = random.nextIntBetweenInclusive(28, 54);
				boolean showFx = component.tryConsumeHorrorEventBudget(config, random, 1);
				teleportNearPlayer(stalker, player, random, random.nextBoolean() ? TeleportPlacement.FLANK : TeleportPlacement.BACK, showFx, config);
				if (showFx) {
					SanityManager.debugEvent(player, "stalker_reposition");
				}
				distanceSq = stalker.distanceToSqr(player);
			}

			if (looking) {
				state.observedTicks++;
				stalker.getNavigation().stop();
				if (state.observedTicks >= 5 || (lineOfSight && state.observeTeleportCooldown <= 0)) {
					state.observedTicks = 0;
					state.observeTeleportCooldown = random.nextIntBetweenInclusive(18, 36);
					state.revealCooldown = random.nextIntBetweenInclusive(18, 34);
					stalker.setInvisible(true);
					boolean showFx = component.tryConsumeHorrorEventBudget(config, random, 1);
					teleportNearPlayer(stalker, player, random, random.nextBoolean() ? TeleportPlacement.FLANK : TeleportPlacement.BACK, showFx, config);
					if (showFx) {
						player.playNotifySound(SoundEvents.ENDERMAN_AMBIENT, SoundSource.HOSTILE, 0.60F, 0.72F + random.nextFloat() * 0.18F);
						SanityManager.debugEvent(player, "stalker_break_line");
					}
				}
			} else {
				state.observedTicks = 0;
				if (stalker.isInvisible() && state.revealCooldown <= 0) {
					stalker.setInvisible(false);
					if (component.tryConsumeHorrorEventBudget(config, random, 1)) {
						player.playNotifySound(SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 0.70F, 0.86F + random.nextFloat() * 0.16F);
						SanityManager.debugEvent(player, "stalker_reveal");
					}
				}
				if (!stalker.isInvisible() && distanceSq > 7.0D * 7.0D && state.pathCooldown <= 0) {
					stalker.getNavigation().moveTo(player, 1.0D);
					state.pathCooldown = 18;
				}
			}

			if (!looking && !stalker.isInvisible() && distanceSq <= 4.5D * 4.5D && state.closeCallCooldown <= 0) {
				triggerCloseCall(player, stalker, state, component, config, random);
			}

			if (state.phaseCooldown <= 0) {
				state.phaseCooldown = random.nextIntBetweenInclusive(24, 50);
				if (!looking && distanceSq >= 5.0D * 5.0D) {
					boolean nowInvisible = !stalker.isInvisible();
					stalker.setInvisible(nowInvisible);
					if (component.tryConsumeHorrorEventBudget(config, random, 1)) {
						if (nowInvisible) {
							player.playNotifySound(SoundEvents.ENDERMAN_AMBIENT, SoundSource.HOSTILE, 0.55F, 0.72F + random.nextFloat() * 0.18F);
						} else {
							player.playNotifySound(SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 0.78F, 0.84F + random.nextFloat() * 0.18F);
						}
						SanityManager.debugEvent(player, "stalker_phase");
					}
				}
			}

			if (!stalker.isInvisible() && state.traceCooldown <= 0 && component.tryConsumeHorrorEventBudget(config, random, 1)) {
				state.traceCooldown = random.nextIntBetweenInclusive(26, 50);
				player.level().sendParticles(player, ParticleTypes.SMOKE, true, false, stalker.getX(), stalker.getY() + 1.0D, stalker.getZ(), Math.min(10, Math.max(2, config.maxDirectedParticlesPerBurst)),
						0.18D, 0.38D, 0.18D, 0.01D);
				player.level().sendParticles(player, ParticleTypes.SCULK_SOUL, true, false, stalker.getX(), stalker.getY() + 1.0D, stalker.getZ(), Math.min(6, Math.max(2, config.maxDirectedParticlesPerBurst)),
						0.12D, 0.22D, 0.12D, 0.01D);
				SanityManager.debugEvent(player, "stalker_trace");
			}

			if (!stalker.isInvisible() && state.audioCooldown <= 0 && component.tryConsumeHorrorEventBudget(config, random, 1)) {
				state.audioCooldown = random.nextIntBetweenInclusive(34, 64);
				if (distanceSq <= 8.0D * 8.0D) {
					player.playNotifySound(SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 0.72F, 0.74F + random.nextFloat() * 0.14F);
				} else {
					player.playNotifySound(SoundEvents.ENDERMAN_AMBIENT, SoundSource.HOSTILE, 0.58F, 0.68F + random.nextFloat() * 0.16F);
				}
				SanityManager.debugEvent(player, "stalker_audio");
			}
			return false;
		});
	}

	private static StalkerEntity findNearPlayer(ServerPlayer player, UUID id) {
		if (id == null) {
			return null;
		}
		return player.level().getEntitiesOfClass(StalkerEntity.class, player.getBoundingBox().inflate(96.0D), e -> e.getUUID().equals(id)).stream().findFirst().orElse(null);
	}

	private static void teleportNearPlayer(StalkerEntity stalker, ServerPlayer player, RandomSource random, TeleportPlacement placement, boolean showEffects, SanityConfig config) {
		double angle;
		double dist;
		float yaw = (float) Math.toRadians(player.getYRot());
		if (placement == TeleportPlacement.FLANK) {
			float side = random.nextBoolean() ? 1.0F : -1.0F;
			angle = yaw + (Math.PI / 2.0D) * side + (random.nextDouble() - 0.5D) * 0.35D;
			dist = 6.0D + random.nextDouble() * 4.0D;
		} else if (placement == TeleportPlacement.BACK) {
			angle = yaw + Math.PI + (random.nextDouble() - 0.5D) * 0.42D;
			dist = 5.5D + random.nextDouble() * 3.5D;
		} else {
			angle = random.nextDouble() * Math.PI * 2.0D;
			dist = random.nextDouble() * 6.0D + 4.0D;
		}
		double x = player.getX() + Math.cos(angle) * dist;
		double z = player.getZ() + Math.sin(angle) * dist;
		Vec3 target = findSafeTeleportPosition(player, x, player.getY(), z);
		stalker.teleportTo(target.x, target.y, target.z);
		stalker.getNavigation().stop();
		if (showEffects) {
			int particles = Math.max(1, Math.min(Math.max(1, config.maxDirectedParticlesPerBurst), 14));
			player.level().sendParticles(player, ParticleTypes.SMOKE, true, false, target.x, target.y + 1.0D, target.z, particles, 0.3D, 0.6D, 0.3D, 0.01D);
		}
	}

	private static boolean isPlayerLookingAt(ServerPlayer player, StalkerEntity stalker) {
		Vec3 look = player.getLookAngle().normalize();
		Vec3 toStalker = stalker.position().add(0.0D, stalker.getBbHeight() * 0.5D, 0.0D).subtract(player.getEyePosition()).normalize();
		double dot = look.dot(toStalker);
		return dot > 0.90D && player.hasLineOfSight(stalker);
	}

	private static void triggerCloseCall(ServerPlayer player, StalkerEntity stalker, HuntState state, PlayerSanityComponent component, SanityConfig config, RandomSource random) {
		state.closeCallCooldown = random.nextIntBetweenInclusive(90, 160);
		state.revealCooldown = random.nextIntBetweenInclusive(18, 32);
		stalker.setInvisible(true);
		boolean showFx = component.tryConsumeHorrorEventBudget(config, random, 1);
		if (showFx) {
			SanityNetworking.triggerScarePulse(player, 8, 3);
			player.playNotifySound(SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 0.88F, 0.74F + random.nextFloat() * 0.12F);
			player.playNotifySound(SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 0.64F, 0.78F + random.nextFloat() * 0.12F);
			player.level().sendParticles(player, ParticleTypes.SMOKE, true, false, stalker.getX(), stalker.getY() + 1.0D, stalker.getZ(), Math.min(14, Math.max(3, config.maxDirectedParticlesPerBurst)), 0.18D,
					0.42D, 0.18D, 0.01D);
			SanityManager.debugEvent(player, "stalker_close_call");
		}
		teleportNearPlayer(stalker, player, random, TeleportPlacement.BACK, showFx, config);
	}

	private static Vec3 findSafeTeleportPosition(ServerPlayer player, double x, double y, double z) {
		BlockPos base = BlockPos.containing(x, y, z);
		for (int dy = 3; dy >= -4; dy--) {
			BlockPos feet = base.offset(0, dy, 0);
			if (isStandable(player, feet)) {
				return new Vec3(feet.getX() + 0.5D, feet.getY(), feet.getZ() + 0.5D);
			}
		}
		return new Vec3(x, y, z);
	}

	private static boolean isStandable(ServerPlayer player, BlockPos feet) {
		BlockPos head = feet.above();
		BlockPos below = feet.below();
		return !player.level().getBlockState(feet).blocksMotion() && !player.level().getBlockState(head).blocksMotion() && player.level().getBlockState(below).blocksMotion();
	}

	private enum TeleportPlacement {
		RANDOM,
		FLANK,
		BACK
	}

	private static class HuntState {
		private final UUID targetId;
		private final UUID stalkerId;
		private int remainingTicks;
		private int repositionCooldown = 28;
		private int phaseCooldown = 20;
		private int observeTeleportCooldown = 0;
		private int revealCooldown = 0;
		private int closeCallCooldown = 70;
		private int audioCooldown = 26;
		private int traceCooldown = 16;
		private int pathCooldown = 0;
		private int observedTicks = 0;

		private HuntState(UUID targetId, UUID stalkerId, int remainingTicks) {
			this.targetId = targetId;
			this.stalkerId = stalkerId;
			this.remainingTicks = remainingTicks;
		}

		private void tickCooldowns() {
			if (repositionCooldown > 0) {
				repositionCooldown--;
			}
			if (phaseCooldown > 0) {
				phaseCooldown--;
			}
			if (observeTeleportCooldown > 0) {
				observeTeleportCooldown--;
			}
			if (revealCooldown > 0) {
				revealCooldown--;
			}
			if (closeCallCooldown > 0) {
				closeCallCooldown--;
			}
			if (audioCooldown > 0) {
				audioCooldown--;
			}
			if (traceCooldown > 0) {
				traceCooldown--;
			}
			if (pathCooldown > 0) {
				pathCooldown--;
			}
		}
	}
}
