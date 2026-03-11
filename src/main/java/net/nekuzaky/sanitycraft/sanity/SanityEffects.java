package net.nekuzaky.sanitycraft.sanity;

import net.nekuzaky.sanitycraft.SanitycraftMod;
import net.nekuzaky.sanitycraft.entity.BloodyCreeperEntity;
import net.nekuzaky.sanitycraft.entity.StalkerEntity;
import net.nekuzaky.sanitycraft.init.SanitycraftModEntities;
import net.nekuzaky.sanitycraft.init.SanitycraftModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
		SanityStage stage = SanityStageResolver.resolve(sanity);
		if (component.hasHallucinationShield()) {
			if (player.hasEffect(MobEffects.NAUSEA)) {
				player.removeEffect(MobEffects.NAUSEA);
			}
			return;
		}

		if (stage == SanityStage.UNEASY) {
			player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 120, 0, true, false, false));
		} else if (stage == SanityStage.STABLE || stage == SanityStage.MILD_DISCOMFORT) {
			if (player.hasEffect(MobEffects.NAUSEA)) {
				player.removeEffect(MobEffects.NAUSEA);
			}
		}
		applyAfflictions(player, component, stage, config, random);

		if (stage == SanityStage.MILD_DISCOMFORT && component.canPlayStrangeSound() && component.tryConsumeHorrorEventBudget(config, random, 1)) {
			HallucinationSoundManager.playStageSound(player, stage, random);
			component.resetStrangeSoundCooldown(random);
			SanityManager.debugEvent(player, "stage_sound_mild");
		}

		if (!config.hallucinationsEnabled) {
			return;
		}

		if (stage == SanityStage.UNSTABLE && component.canPlayHallucination() && component.tryConsumeHorrorEventBudget(config, random, 2)) {
			HallucinationSoundManager.playStageSound(player, stage, random);
			spawnShadowHallucination(level, player, random, config);
			sendWhisper(player, component, random, sanity);
			component.resetHallucinationCooldown(random);
			SanityManager.debugEvent(player, "hallucination_unstable");
		}

		if (stage == SanityStage.SEVERE_BREAKDOWN && component.canSpawnGhost() && component.tryConsumeHorrorEventBudget(config, random, 3)) {
			HallucinationSoundManager.playStageSound(player, stage, random);
			spawnBloodHallucination(level, player, random, config);
			spawnGhostApparition(level, player, random, config);
			spawnStalkerHallucination(level, player, random, config);
			spawnBloodyCreeperHallucination(level, player, random, config);
			player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0, true, false, false));
			sendWhisper(player, component, random, sanity);
			component.resetGhostCooldown(random);
			SanityManager.debugEvent(player, "hallucination_breakdown");
		}

		if (stage == SanityStage.UNEASY && component.canWhisper() && random.nextFloat() < clamp01(config.uneasyWhisperChance) && component.tryConsumeHorrorEventBudget(config, random, 1)) {
			sendWhisper(player, component, random, sanity);
			SanityManager.debugEvent(player, "whisper_uneasy");
		}
	}

	private static void spawnShadowHallucination(ServerLevel level, ServerPlayer player, RandomSource random, SanityConfig config) {
		BlockPos base = randomNearby(player, random, 5.0D, 9.0D);
		level.sendParticles(player, ParticleTypes.SMOKE, true, false, base.getX() + 0.5D, base.getY() + 0.2D, base.getZ() + 0.5D, clampParticleCount(config, 20), 0.5D, 1.0D, 0.5D, 0.01D);
		level.sendParticles(player, ParticleTypes.WHITE_ASH, true, false, base.getX() + 0.5D, base.getY() + 0.8D, base.getZ() + 0.5D, clampParticleCount(config, 8), 0.25D, 0.6D, 0.25D, 0.01D);
	}

	private static void spawnGhostApparition(ServerLevel level, ServerPlayer player, RandomSource random, SanityConfig config) {
		BlockPos base = randomNearby(player, random, 6.0D, 11.0D);
		level.sendParticles(player, ParticleTypes.SOUL, true, false, base.getX() + 0.5D, base.getY() + 0.4D, base.getZ() + 0.5D, clampParticleCount(config, 28), 0.6D, 1.2D, 0.6D, 0.02D);
		level.sendParticles(player, ParticleTypes.SCULK_SOUL, true, false, base.getX() + 0.5D, base.getY() + 1.0D, base.getZ() + 0.5D, clampParticleCount(config, 18), 0.45D, 1.4D, 0.45D, 0.02D);
		level.sendParticles(player, ParticleTypes.SOUL_FIRE_FLAME, true, false, base.getX() + 0.5D, base.getY() + 0.8D, base.getZ() + 0.5D, clampParticleCount(config, 10), 0.35D, 0.9D, 0.35D, 0.005D);
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
		level.addFreshEntity(stalker);
		SanityStalkerHuntDirector.attach(player, stalker, config.stalkerLifetimeSeconds);
		SanityManager.debugEvent(player, "stalker_spawned");
	}

	private static void spawnBloodyCreeperHallucination(ServerLevel level, ServerPlayer player, RandomSource random, SanityConfig config) {
		if (!config.bloodyCreeperHallucinationEnabled) {
			return;
		}
		if (random.nextInt(100) >= Math.max(0, Math.min(100, config.bloodyCreeperSpawnChancePercent))) {
			return;
		}
		if (!level.getEntitiesOfClass(BloodyCreeperEntity.class, player.getBoundingBox().inflate(26.0D), entity -> entity.isAlive() && entity.getTags().contains("sanitycraft_hallucination_creeper"))
				.isEmpty()) {
			return;
		}

		BlockPos pos = randomNearby(player, random, 7.0D, 15.0D);
		BloodyCreeperEntity creeper = SanitycraftModEntities.BLOODY_CREEPER.create(level, EntitySpawnReason.EVENT);
		if (creeper == null) {
			return;
		}

		creeper.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
		creeper.setTarget(player);
		creeper.addTag("sanitycraft_hallucination_creeper");
		level.addFreshEntity(creeper);
		SanityManager.debugEvent(player, "bloody_creeper_spawned");

		SanitycraftMod.queueServerWork(Math.max(1, config.bloodyCreeperLifetimeSeconds) * 20, () -> {
			if (creeper.isAlive()) {
				creeper.triggerHallucinationExplosion();
			}
		});
	}

	private static void spawnBloodHallucination(ServerLevel level, ServerPlayer player, RandomSource random, SanityConfig config) {
		BlockPos base = randomNearby(player, random, 4.0D, 8.0D);
		SimpleParticleType blood = SanitycraftModParticleTypes.BLOOD;
		if (blood == null) {
			try {
				Object raw = BuiltInRegistries.PARTICLE_TYPE.getValue(ResourceLocation.fromNamespaceAndPath(SanitycraftMod.MODID, "blood"));
				if (raw instanceof SimpleParticleType simple) {
					blood = simple;
				}
			} catch (Exception ignored) {
			}
		}
		if (blood != null) {
			level.sendParticles(player, blood, true, false, base.getX() + 0.5D, base.getY() + 0.7D, base.getZ() + 0.5D, clampParticleCount(config, 18), 0.35D, 0.35D, 0.35D, 0.01D);
		}
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

	private static float clamp01(float value) {
		if (value < 0.0F) {
			return 0.0F;
		}
		return Math.min(1.0F, value);
	}

	private static int clampParticleCount(SanityConfig config, int requested) {
		return Math.max(1, Math.min(Math.max(1, config.maxDirectedParticlesPerBurst), requested));
	}

	private static void applyAfflictions(ServerPlayer player, PlayerSanityComponent component, SanityStage stage, SanityConfig config, RandomSource random) {
		if (!config.sanityAfflictionsEnabled) {
			return;
		}
		if (stage == SanityStage.UNEASY) {
			player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 50, 0, true, false, false));
			if (random.nextFloat() < 0.10F) {
				player.causeFoodExhaustion(0.30F);
			}
		} else if (stage == SanityStage.UNSTABLE) {
			player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 80, 0, true, false, false));
			player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 55, 0, true, false, false));
			player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 70, 0, true, false, false));
			if (random.nextFloat() < 0.22F && component.tryConsumeHorrorEventBudget(config, random, 1)) {
				SanityNetworking.triggerScarePulse(player, 6, 2);
				SanityManager.debugEvent(player, "affliction_tremor_unstable");
			}
			player.causeFoodExhaustion(0.45F);
		} else if (stage == SanityStage.SEVERE_BREAKDOWN) {
			player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 120, 1, true, false, false));
			player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1, true, false, false));
			player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1, true, false, false));
			player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 70, 0, true, false, false));
			if (random.nextFloat() < 0.35F && component.tryConsumeHorrorEventBudget(config, random, 1)) {
				SanityNetworking.triggerScarePulse(player, 9, 3);
				SanityManager.debugEvent(player, "affliction_tremor_breakdown");
			}
			player.causeFoodExhaustion(0.65F);
		}
	}
}
