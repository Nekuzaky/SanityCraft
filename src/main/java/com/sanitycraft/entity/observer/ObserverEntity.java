package com.sanitycraft.entity.observer;

import com.mojang.authlib.GameProfile;
import com.sanitycraft.sanity.SanityDebug;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ObserverEntity extends Monster {
	private static final EntityDataAccessor<String> DATA_OWNER_ID = SynchedEntityData.defineId(ObserverEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<String> DATA_OWNER_NAME = SynchedEntityData.defineId(ObserverEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<Integer> DATA_MODE = SynchedEntityData.defineId(ObserverEntity.class, EntityDataSerializers.INT);

	private int hallucinationLifetimeTicks;
	private int lookedAtTicks;

	public ObserverEntity(EntityType<ObserverEntity> type, Level level) {
		super(type, level);
		xpReward = 0;
		refreshManifestationState();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_OWNER_ID, "");
		builder.define(DATA_OWNER_NAME, "");
		builder.define(DATA_MODE, HallucinationMode.OBSERVER.id());
	}

	@Override
	public void tick() {
		refreshManifestationState();
		super.tick();
		if (this.level().isClientSide()) {
			return;
		}

		this.setDeltaMovement(Vec3.ZERO);
		this.getNavigation().stop();

		ServerPlayer owner = resolveOwner();
		if (owner == null) {
			return;
		}
		if (!owner.isAlive()) {
			this.discard();
			return;
		}

		this.lookAt(owner, 30.0F, 30.0F);
		alignTo(owner);

		hallucinationLifetimeTicks--;
		if (hallucinationLifetimeTicks <= 0) {
			vanish(owner, "lifetime_expired");
			return;
		}

		if (isPlayerLookingAt(owner)) {
			lookedAtTicks++;
			if (lookedAtTicks >= (isDoppelganger() ? 1 : 2)) {
				vanish(owner, "direct_look");
			}
			return;
		}

		lookedAtTicks = 0;
		if (isDoppelganger() && this.distanceToSqr(owner) <= 8.0D * 8.0D) {
			vanish(owner, "approached");
		}
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	public boolean isAttackable() {
		return !hasBoundOwner();
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return hasBoundOwner() ? false : super.hurtServer(level, source, amount);
	}

	@Override
	public boolean isPushable() {
		return !hasBoundOwner();
	}

	@Override
	public boolean isInvisibleTo(Player player) {
		UUID ownerId = getOwnerUuid();
		return ownerId != null && !ownerId.equals(player.getUUID()) || super.isInvisibleTo(player);
	}

	public void configureHallucination(ServerPlayer player, int lifetimeTicks, HallucinationMode mode) {
		this.entityData.set(DATA_OWNER_ID, player.getUUID().toString());
		this.entityData.set(DATA_OWNER_NAME, player.getGameProfile().getName());
		this.entityData.set(DATA_MODE, mode.id());
		this.hallucinationLifetimeTicks = Math.max(20, lifetimeTicks);
		refreshManifestationState();
	}

	@Nullable
	public UUID getOwnerUuid() {
		String raw = this.entityData.get(DATA_OWNER_ID);
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return UUID.fromString(raw);
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

	public String getOwnerName() {
		return this.entityData.get(DATA_OWNER_NAME);
	}

	public GameProfile createOwnerProfile() {
		UUID ownerId = getOwnerUuid();
		return new GameProfile(ownerId == null ? UUID.randomUUID() : ownerId, getOwnerName().isBlank() ? "observer" : getOwnerName());
	}

	public HallucinationMode getHallucinationMode() {
		return HallucinationMode.byId(this.entityData.get(DATA_MODE));
	}

	public boolean isDoppelganger() {
		return getHallucinationMode() == HallucinationMode.DOPPELGANGER;
	}

	public boolean isHallucinationFor(ServerPlayer player, HallucinationMode mode) {
		UUID ownerId = getOwnerUuid();
		return ownerId != null && ownerId.equals(player.getUUID()) && getHallucinationMode() == mode;
	}

	public boolean hasBoundOwner() {
		return getOwnerUuid() != null;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MOVEMENT_SPEED, 0.0D)
				.add(Attributes.MAX_HEALTH, 20.0D)
				.add(Attributes.ARMOR, 0.0D)
				.add(Attributes.ATTACK_DAMAGE, 0.0D)
				.add(Attributes.FOLLOW_RANGE, 64.0D)
				.add(Attributes.STEP_HEIGHT, 0.6D);
	}

	private void refreshManifestationState() {
		boolean hallucination = hasBoundOwner();
		this.noPhysics = hallucination;
		this.setNoAi(hallucination);
		this.setSilent(hallucination);
	}

	@Nullable
	private ServerPlayer resolveOwner() {
		if (!(this.level() instanceof ServerLevel serverLevel)) {
			return null;
		}
		UUID ownerId = getOwnerUuid();
		return ownerId == null ? null : serverLevel.getServer().getPlayerList().getPlayer(ownerId);
	}

	private boolean isPlayerLookingAt(ServerPlayer player) {
		Vec3 look = player.getLookAngle().normalize();
		Vec3 toEntity = this.position().add(0.0D, this.getBbHeight() * 0.55D, 0.0D).subtract(player.getEyePosition()).normalize();
		return look.dot(toEntity) > 0.965D && player.hasLineOfSight(this);
	}

	private void alignTo(ServerPlayer player) {
		double dx = player.getX() - this.getX();
		double dz = player.getZ() - this.getZ();
		float targetYaw = (float) (Math.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
		float bodyYaw = Mth.approachDegrees(this.getYRot(), targetYaw, 12.0F);
		this.setYRot(bodyYaw);
		this.setYHeadRot(bodyYaw);
		this.setYBodyRot(bodyYaw);
	}

	private void vanish(ServerPlayer player, String reason) {
		if (this.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(player, ParticleTypes.SMOKE, true, false, this.getX(), this.getY() + 1.0D, this.getZ(), 8, 0.18D, 0.30D, 0.18D, 0.01D);
			serverLevel.sendParticles(player, ParticleTypes.SOUL, true, false, this.getX(), this.getY() + 1.0D, this.getZ(), 4, 0.10D, 0.22D, 0.10D, 0.0D);
		}
		SanityDebug.logEvent(player, "observer_vanish mode=" + getHallucinationMode().commandName() + " reason=" + reason);
		this.discard();
	}

	public enum HallucinationMode {
		OBSERVER(0, "observer"),
		DOPPELGANGER(1, "doppelganger");

		private final int id;
		private final String commandName;

		HallucinationMode(int id, String commandName) {
			this.id = id;
			this.commandName = commandName;
		}

		public int id() {
			return id;
		}

		public String commandName() {
			return commandName;
		}

		public static HallucinationMode byId(int id) {
			return id == DOPPELGANGER.id ? DOPPELGANGER : OBSERVER;
		}
	}
}
