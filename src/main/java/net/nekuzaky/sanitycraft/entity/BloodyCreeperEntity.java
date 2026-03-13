package net.nekuzaky.sanitycraft.entity;

import com.sanitycraft.network.sync.ClientEffectSyncService;
import com.sanitycraft.registry.ModParticles;
import com.sanitycraft.sanity.HallucinationTags;
import com.sanitycraft.sanity.SanityDebug;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class BloodyCreeperEntity extends Monster {
	private static final int MAX_EXPLOSION_SMOKE_PARTICLES = 22;
	private static final int DEFAULT_HALLUCINATION_LIFETIME_TICKS = 20 * 6;
	private boolean hallucinationExploded = false;
	private UUID hallucinationOwnerId;
	private int hallucinationLifetimeTicks;

	public BloodyCreeperEntity(EntityType<BloodyCreeperEntity> type, Level world) {
		super(type, world);
		xpReward = 0;
		setNoAi(false);
		setPersistenceRequired();
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false) {
			@Override
			protected boolean canPerformAttack(LivingEntity entity) {
				return this.isTimeToAttack() && this.mob.distanceToSqr(entity) < (this.mob.getBbWidth() * this.mob.getBbWidth() + entity.getBbWidth()) && this.mob.getSensing().hasLineOfSight(entity);
			}
		});
		this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1));
		this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
		this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(5, new FloatGoal(this));
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return !isHallucination();
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide() || !this.isAlive() || !isHallucination()) {
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
		if (this.distanceToSqr(targetPlayer) > 18.0D * 18.0D) {
			repositionNear(targetPlayer);
		}
		if (this.distanceToSqr(targetPlayer) <= 9.0D || hallucinationLifetimeTicks <= 0) {
			triggerHallucinationExplosion();
			return;
		}
		if (this.distanceToSqr(targetPlayer) > 2.5D * 2.5D) {
			this.getNavigation().moveTo(targetPlayer, 0.70D);
		}
	}

	public void configureHallucination(ServerPlayer player, int lifetimeTicks) {
		hallucinationOwnerId = player.getUUID();
		hallucinationLifetimeTicks = Math.max(20, lifetimeTicks);
		this.addTag(HallucinationTags.BLOODY_CREEPER);
		this.setPersistenceRequired();
		this.setTarget(player);
	}

	public boolean isHallucination() {
		return this.getTags().contains(HallucinationTags.BLOODY_CREEPER);
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

	public void triggerHallucinationExplosion() {
		if (hallucinationExploded || this.level().isClientSide()) {
			return;
		}
		hallucinationExploded = true;
		Level level = this.level();
		level.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.0F, 0.95F + this.getRandom().nextFloat() * 0.15F);
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 0.8D, this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
			serverLevel.sendParticles(
					ParticleTypes.SMOKE,
					this.getX(),
					this.getY() + 0.6D,
					this.getZ(),
					MAX_EXPLOSION_SMOKE_PARTICLES,
					0.35D,
					0.25D,
					0.35D,
					0.02D);
		}
		ServerPlayer targetPlayer = resolveHallucinationTarget();
		if (targetPlayer != null) {
			if (level instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(targetPlayer, ModParticles.BLOOD, true, false, this.getX(), this.getY() + 0.7D, this.getZ(), 12, 0.18D, 0.12D, 0.18D, 0.01D);
			}
			ClientEffectSyncService.sendScarePulse(targetPlayer, 16, 6);
			SanityDebug.logEvent(targetPlayer, "bloody_creeper_explosion");
		}
		this.discard();
	}

	@Override
	public SoundEvent getHurtSound(DamageSource ds) {
		return BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("entity.generic.hurt"));
	}

	@Override
	public SoundEvent getDeathSound() {
		return BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse("entity.generic.death"));
	}

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
		builder = builder.add(Attributes.MAX_HEALTH, 10);
		builder = builder.add(Attributes.ARMOR, 0);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 3);
		builder = builder.add(Attributes.FOLLOW_RANGE, 16);
		builder = builder.add(Attributes.STEP_HEIGHT, 0.6);
		return builder;
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
		return serverLevel.getPlayers(player -> !player.isSpectator() && player.distanceToSqr(this) <= 24.0D * 24.0D).stream().findFirst().orElse(null);
	}

	private void repositionNear(ServerPlayer player) {
		float yawRadians = (float) Math.toRadians(player.getYRot());
		double angle = yawRadians + Math.PI + (this.getRandom().nextDouble() - 0.5D) * 0.55D;
		double distance = 6.0D + this.getRandom().nextDouble() * 3.0D;
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
}
