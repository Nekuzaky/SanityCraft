package net.nekuzaky.sanitycraft.sanity;

import net.nekuzaky.sanitycraft.SanitycraftMod;
import net.nekuzaky.sanitycraft.entity.StalkerEntity;
import net.nekuzaky.sanitycraft.init.SanitycraftModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class SanityEffects {
	private SanityEffects() {
	}

	public static void apply(ServerPlayer player, PlayerSanityComponent component, SanityConfig config) {
		int sanity = component.getSanity();
		RandomSource random = player.getRandom();
		ServerLevel level = player.level();

		if (sanity <= 60 && sanity > 40) {
			player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 120, 0, true, false, false));
		} else if (sanity > 60 && player.hasEffect(MobEffects.NAUSEA)) {
			player.removeEffect(MobEffects.NAUSEA);
		}

		if (sanity <= 80 && sanity > 60 && component.canPlayStrangeSound()) {
			player.playNotifySound(SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.45F, 0.8F + random.nextFloat() * 0.4F);
			component.resetStrangeSoundCooldown(random);
		}

		if (!config.hallucinationsEnabled) {
			return;
		}

		if (sanity <= 40 && sanity > 20 && component.canPlayHallucination()) {
			playHallucinationSound(player, random);
			spawnShadowHallucination(level, player, random);
			sendWhisper(player, component, random, sanity);
			component.resetHallucinationCooldown(random);
		}

		if (sanity <= 20 && component.canSpawnGhost()) {
			spawnGhostApparition(level, player, random);
			spawnStalkerHallucination(level, player, random, config);
			player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0, true, false, false));
			sendWhisper(player, component, random, sanity);
			component.resetGhostCooldown(random);
		}

		if (sanity <= 60 && sanity > 40 && component.canWhisper() && random.nextFloat() < 0.22F) {
			sendWhisper(player, component, random, sanity);
		}
	}

	private static void playHallucinationSound(ServerPlayer player, RandomSource random) {
		int roll = random.nextInt(4);
		if (roll == 0) {
			player.playNotifySound(SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 0.7F, 0.8F + random.nextFloat() * 0.3F);
		} else if (roll == 1) {
			player.playNotifySound(SoundEvents.PHANTOM_AMBIENT, SoundSource.HOSTILE, 0.7F, 0.75F + random.nextFloat() * 0.2F);
		} else if (roll == 2) {
			player.playNotifySound(SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE, 0.8F, 0.9F + random.nextFloat() * 0.2F);
		} else {
			player.playNotifySound(SoundEvents.WARDEN_NEARBY_CLOSER, SoundSource.HOSTILE, 0.8F, 0.9F + random.nextFloat() * 0.2F);
		}
	}

	private static void spawnShadowHallucination(ServerLevel level, ServerPlayer player, RandomSource random) {
		BlockPos base = randomNearby(player, random, 5.0D, 9.0D);
		level.sendParticles(player, ParticleTypes.SMOKE, true, false, base.getX() + 0.5D, base.getY() + 0.2D, base.getZ() + 0.5D, 20, 0.5D, 1.0D, 0.5D, 0.01D);
		level.sendParticles(player, ParticleTypes.WHITE_ASH, true, false, base.getX() + 0.5D, base.getY() + 0.8D, base.getZ() + 0.5D, 8, 0.25D, 0.6D, 0.25D, 0.01D);
	}

	private static void spawnGhostApparition(ServerLevel level, ServerPlayer player, RandomSource random) {
		BlockPos base = randomNearby(player, random, 6.0D, 11.0D);
		level.sendParticles(player, ParticleTypes.SOUL, true, false, base.getX() + 0.5D, base.getY() + 0.4D, base.getZ() + 0.5D, 28, 0.6D, 1.2D, 0.6D, 0.02D);
		level.sendParticles(player, ParticleTypes.SCULK_SOUL, true, false, base.getX() + 0.5D, base.getY() + 1.0D, base.getZ() + 0.5D, 18, 0.45D, 1.4D, 0.45D, 0.02D);
		level.sendParticles(player, ParticleTypes.SOUL_FIRE_FLAME, true, false, base.getX() + 0.5D, base.getY() + 0.8D, base.getZ() + 0.5D, 10, 0.35D, 0.9D, 0.35D, 0.005D);
		player.playNotifySound(SoundEvents.WARDEN_NEARBY_CLOSEST, SoundSource.HOSTILE, 0.8F, 0.7F + random.nextFloat() * 0.2F);
	}

	private static void spawnStalkerHallucination(ServerLevel level, ServerPlayer player, RandomSource random, SanityConfig config) {
		if (!config.stalkerHallucinationEnabled) {
			return;
		}
		if (random.nextInt(100) >= Math.max(0, Math.min(100, config.stalkerSpawnChancePercent))) {
			return;
		}
		if (!level.getEntitiesOfClass(StalkerEntity.class, player.getBoundingBox().inflate(28.0D), entity -> entity.isAlive() && entity.getTags().contains("sanitycraft_hallucination")).isEmpty()) {
			return;
		}

		BlockPos pos = randomNearby(player, random, Math.max(2.0D, config.stalkerMinSpawnDistance), Math.max(config.stalkerMinSpawnDistance + 1.0D, config.stalkerMaxSpawnDistance));
		StalkerEntity stalker = SanitycraftModEntities.STALKER.create(level, EntitySpawnReason.EVENT);
		if (stalker == null) {
			return;
		}

		stalker.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
		stalker.setTarget(player);
		stalker.addTag("sanitycraft_hallucination");
		level.addFreshEntity(stalker);
		player.playNotifySound(SoundEvents.ENDERMAN_AMBIENT, SoundSource.HOSTILE, 0.8F, 0.7F + random.nextFloat() * 0.3F);

		SanitycraftMod.queueServerWork(Math.max(1, config.stalkerLifetimeSeconds) * 20, () -> {
			if (stalker.isAlive()) {
				stalker.discard();
			}
		});
	}

	private static BlockPos randomNearby(ServerPlayer player, RandomSource random, double minDistance, double maxDistance) {
		double angle = random.nextDouble() * Math.PI * 2.0D;
		double distance = minDistance + random.nextDouble() * (maxDistance - minDistance);
		int x = (int) Math.floor(player.getX() + Math.cos(angle) * distance);
		int y = (int) Math.floor(player.getY());
		int z = (int) Math.floor(player.getZ() + Math.sin(angle) * distance);
		return new BlockPos(x, y, z);
	}

	private static void sendWhisper(ServerPlayer player, PlayerSanityComponent component, RandomSource random, int sanity) {
		if (!component.canWhisper()) {
			return;
		}
		String[] whispers = sanity <= 20 ? new String[] {"Do not look behind you.", "It knows your name.", "Stay in the dark.", "You cannot wake up."}
				: sanity <= 40 ? new String[] {"Something is watching.", "The cave remembers.", "Keep moving.", "Do not trust the silence."}
						: new String[] {"You are not alone.", "Did you hear that?", "Stay near the light.", "The storm is coming."};
		String text = whispers[random.nextInt(whispers.length)];
		player.displayClientMessage(Component.literal(text), true);
		component.resetWhisperCooldown(random);
	}
}
