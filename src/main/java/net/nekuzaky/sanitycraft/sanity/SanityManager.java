package net.nekuzaky.sanitycraft.sanity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SanityManager {
	private static final Map<UUID, PlayerSanityComponent> PLAYER_SANITY = new ConcurrentHashMap<>();
	private static SanityConfig config = new SanityConfig();

	private SanityManager() {
	}

	public static void initialize() {
		config = SanityConfig.loadOrCreate();
	}

	public static SanityConfig getConfig() {
		return config;
	}

	public static boolean applyProfile(String profile) {
		return config.applyNamedProfile(profile);
	}

	public static PlayerSanityComponent get(ServerPlayer player) {
		return PLAYER_SANITY.computeIfAbsent(player.getUUID(), uuid -> new PlayerSanityComponent());
	}

	public static void remove(ServerPlayer player) {
		PLAYER_SANITY.remove(player.getUUID());
	}

	public static void setSanity(ServerPlayer player, int sanity) {
		PlayerSanityComponent component = get(player);
		int before = component.getSanity();
		component.setSanity(sanity);
		SanityNetworking.sync(player, component.getSanity());
		SanityPersistence.set(player, component.getSanity());
		if (before > 0 && component.getSanity() <= 0) {
			SanityJournal.log(player, "My sanity collapsed to zero.");
		}
	}

	public static void addSanity(ServerPlayer player, int delta) {
		setSanity(player, get(player).getSanity() + delta);
	}

	public static void copy(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
		PlayerSanityComponent source = get(oldPlayer);
		PlayerSanityComponent target = get(newPlayer);
		target.setSanity(source.getSanity());
		target.resetZeroSanityTimer();
		SanityPersistence.set(newPlayer, target.getSanity());
	}

	public static void tickZeroSanityDeath(ServerPlayer player) {
		if (!config.zeroSanityDeathEnabled) {
			return;
		}
		PlayerSanityComponent component = get(player);
		int delayTicks = Math.max(1, config.zeroSanityDeathDelaySeconds) * 20;
		if (component.tickZeroSanityTimer(delayTicks)) {
			component.resetZeroSanityTimer();
			player.hurt(player.damageSources().magic(), Float.MAX_VALUE);
			player.displayClientMessage(Component.literal("Your mind collapsed."), true);
		}
	}

	public static void tick(ServerPlayer player) {
		PlayerSanityComponent component = get(player);
		component.tickCooldowns();
		if (!component.shouldUpdate(config.getUpdateIntervalTicks())) {
			return;
		}

		int before = component.getSanity();
		SanityEnvironmentSnapshot environment = SanityEnvironmentHelper.snapshot(player, config);
		int delta = SanityCalculator.computeDelta(environment, config);
		delta -= computePartyStressPenalty(player);
		component.addSanity(delta);
		SanityEffects.apply(player, component, config);
		if (environment.ritualSafeZone() && component.canWriteJournal()) {
			SanityJournal.log(player, "The torch circle held. For now I can breathe.");
			component.resetJournalCooldown(player.getRandom());
		} else if (environment.anomalyNearby() && component.canWriteJournal()) {
			SanityJournal.log(player, "I found an anomaly structure. It distorts my thoughts.");
			component.resetJournalCooldown(player.getRandom());
		}
		if (delta < 0 && component.canWriteJournal()) {
			SanityJournal.log(player, "Something in this place is eroding my mind.");
			component.resetJournalCooldown(player.getRandom());
		}
		if (component.getSanity() != before) {
			SanityNetworking.sync(player, component.getSanity());
			SanityPersistence.set(player, component.getSanity());
		}
	}

	public static void onSleepWake(ServerPlayer player) {
		if (!config.nightmareSleepEnabled) {
			return;
		}
		PlayerSanityComponent component = get(player);
		int sanity = component.getSanity();
		if (sanity <= Math.max(1, config.nightmareSleepThreshold)) {
			int variant = player.getRandom().nextInt(3);
			if (variant == 0) {
				addSanity(player, -Math.max(0, config.nightmareSleepPenalty));
				player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 140, 0, true, false, false));
				player.displayClientMessage(Component.literal("Nightmare: You woke up blind with panic."), true);
				SanityJournal.log(player, "I woke up blind in panic.");
			} else if (variant == 1) {
				addSanity(player, -Math.max(0, config.nightmareSleepPenalty / 2));
				player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 220, 0, true, false, false));
				player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 140, 0, true, false, false));
				player.displayClientMessage(Component.literal("Nightmare: Your body feels heavy and wrong."), true);
				SanityJournal.log(player, "My body felt like stone after that dream.");
			} else {
				addSanity(player, Math.max(0, config.nightmareSleepMinorRecovery));
				player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 120, 0, true, false, false));
				player.displayClientMessage(Component.literal("Nightmare: You survived it... barely."), true);
				SanityJournal.log(player, "I escaped a nightmare, but it left a mark.");
			}
			player.level().playSound(null, player.blockPosition(), SoundEvents.WARDEN_NEARBY_CLOSEST, SoundSource.HOSTILE, 0.85F, 0.75F + player.getRandom().nextFloat() * 0.2F);
		} else {
			addSanity(player, Math.max(0, config.nightmareSleepMinorRecovery));
			SanityJournal.log(player, "Sleep quieted the noise in my head.");
		}
	}

	private static int computePartyStressPenalty(ServerPlayer player) {
		if (!config.partyStressLinkEnabled) {
			return 0;
		}
		AABB area = player.getBoundingBox().inflate(Math.max(1, config.partyStressRadius));
		int stressed = 0;
		if (player.getServer() == null) {
			return 0;
		}
		for (ServerPlayer other : player.getServer().getPlayerList().getPlayers()) {
			if (other == player || !other.level().dimension().equals(player.level().dimension()) || !other.getBoundingBox().intersects(area)) {
				continue;
			}
			if (get(other).getSanity() <= Math.max(0, config.partyStressThreshold)) {
				stressed++;
			}
		}
		if (stressed <= 0) {
			return 0;
		}
		return Math.min(Math.max(0, config.partyStressMaxLoss), stressed * Math.max(0, config.partyStressLossPerPlayer));
	}
}
