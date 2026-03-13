package com.sanitycraft.entity.stalker;

import com.sanitycraft.sanity.HallucinationTags;
import com.sanitycraft.sanity.SanityDebug;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.registries.BuiltInRegistries;

public class StalkerEntity extends Monster {
	private static final int DEFAULT_HALLUCINATION_LIFETIME_TICKS = 20 * 12;

	private UUID hallucinationOwnerId;
	private int hallucinationLifetimeTicks;
	private int lookedAtTicks;
	private boolean watcherMode;

	public StalkerEntity(EntityType<StalkerEntity> type, Level level) {
		super(type, level);
		xpReward = 0;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
	}

	@Override
	public Vec3 getPassengerRidingPosition(Entity entity) {
		return super.getPassengerRidingPosition(entity).add(0.0D, -0.35D, 0.0D);
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return !isHallucination();
	}

	@Override
	public boolean isInvisibleTo(Player player) {
		return isHallucination() && (hallucinationOwnerId == null || !hallucinationOwnerId.equals(player.getUUID())) || super.isInvisibleTo(player);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide() || !isHallucination()) {
			return;
		}

		ServerPlayer targetPlayer = resolveHallucinationTarget();
		if (targetPlayer == null || !targetPlayer.isAlive()) {
			this.discard();
			return;
		}

		if (hallucinationLifetimeTicks <= 0) {
			hallucinationLifetimeTicks = DEFAULT_HALLUCINATION_LIFETIME_TICKS;
		}
		hallucinationLifetimeTicks--;
		this.setTarget(targetPlayer);
		var attackDamage = this.getAttribute(Attributes.ATTACK_DAMAGE);
		if (attackDamage != null && attackDamage.getBaseValue() != 0.0D) {
			attackDamage.setBaseValue(0.0D);
		}

		if (isPlayerLookingAt(targetPlayer)) {
			lookedAtTicks++;
			this.getNavigation().stop();
			if (lookedAtTicks >= 4) {
				vanish(targetPlayer, "player_stared");
			}
			return;
		}

		lookedAtTicks = 0;
		if (hallucinationLifetimeTicks <= 0) {
			vanish(targetPlayer, "lifetime_expired");
			return;
		}
		if (watcherMode) {
			this.getNavigation().stop();
			this.lookAt(targetPlayer, 30.0F, 30.0F);
			return;
		}
		if (this.distanceToSqr(targetPlayer) > 20.0D * 20.0D) {
			repositionNear(targetPlayer);
		}
		if (this.distanceToSqr(targetPlayer) > 2.5D * 2.5D) {
			this.getNavigation().moveTo(targetPlayer, 0.55D);
		} else {
			this.getNavigation().stop();
		}
	}

	public void configureHallucination(ServerPlayer player, int lifetimeTicks) {
		configureHallucination(player, lifetimeTicks, false);
	}

	public void configureHallucination(ServerPlayer player, int lifetimeTicks, boolean watcherMode) {
		hallucinationOwnerId = player.getUUID();
		hallucinationLifetimeTicks = Math.max(20, lifetimeTicks);
		this.watcherMode = watcherMode;
		this.addTag(HallucinationTags.STALKER);
		this.setPersistenceRequired();
		this.setTarget(player);
	}

	public boolean isHallucination() {
		return this.getTags().contains(HallucinationTags.STALKER);
	}

	public boolean isHallucinationFor(ServerPlayer player) {
		if (!isHallucination()) {
			return false;
		}
		if (hallucinationOwnerId != null) {
			return hallucinationOwnerId.equals(player.getUUID());
		}
		return this.getTarget() == player;
	}

	@Override
	public SoundEvent getHurtSound(DamageSource source) {
		return BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("entity.generic.hurt"));
	}

	@Override
	public boolean isAttackable() {
		return !isHallucination() && super.isAttackable();
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (isHallucination()) {
			return false;
		}
		return super.hurtServer(level, source, amount);
	}

	@Override
	public SoundEvent getDeathSound() {
		return BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("entity.generic.death"));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MOVEMENT_SPEED, 0.28D)
				.add(Attributes.MAX_HEALTH, 20.0D)
				.add(Attributes.ARMOR, 0.0D)
				.add(Attributes.ATTACK_DAMAGE, 0.0D)
				.add(Attributes.FOLLOW_RANGE, 24.0D)
				.add(Attributes.STEP_HEIGHT, 0.6D);
	}

	private ServerPlayer resolveHallucinationTarget() {
		if (!(this.level() instanceof ServerLevel serverLevel)) {
			return null;
		}
		if (hallucinationOwnerId != null) {
			ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(hallucinationOwnerId);
			if (owner != null && owner.level() == serverLevel) {
				return owner;
			}
		}
		if (this.getTarget() instanceof ServerPlayer targetPlayer && targetPlayer.isAlive()) {
			return targetPlayer;
		}
		return null;
	}

	private boolean isPlayerLookingAt(ServerPlayer player) {
		Vec3 look = player.getLookAngle().normalize();
		Vec3 toEntity = this.position().add(0.0D, this.getBbHeight() * 0.55D, 0.0D).subtract(player.getEyePosition()).normalize();
		return look.dot(toEntity) > 0.92D && player.hasLineOfSight(this);
	}

	private void repositionNear(ServerPlayer player) {
		float yawRadians = (float) Math.toRadians(player.getYRot());
		double angle = yawRadians + Math.PI + (this.getRandom().nextDouble() - 0.5D) * 0.45D;
		double distance = 7.0D + this.getRandom().nextDouble() * 4.0D;
		double x = player.getX() + Math.cos(angle) * distance;
		double z = player.getZ() + Math.sin(angle) * distance;
		Vec3 safe = findSafePosition(player, x, player.getY(), z);
		this.teleportTo(safe.x, safe.y, safe.z);
		this.getNavigation().stop();
	}

	private Vec3 findSafePosition(ServerPlayer player, double x, double y, double z) {
		BlockPos base = BlockPos.containing(x, y, z);
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
		return new Vec3(x, y, z);
	}

	private void vanish(ServerPlayer player, String reason) {
		if (this.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(player, ParticleTypes.SMOKE, true, false, this.getX(), this.getY() + 1.0D, this.getZ(), 10, 0.18D, 0.32D, 0.18D, 0.01D);
		}
		SanityDebug.logStalkerVanish(player, reason, position());
		this.discard();
	}
}
