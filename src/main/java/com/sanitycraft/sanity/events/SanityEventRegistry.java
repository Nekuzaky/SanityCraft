package com.sanitycraft.sanity.events;

import com.sanitycraft.network.sync.ClientEffectSyncService;
import com.sanitycraft.sanity.SanityPsychologicalService;
import com.sanitycraft.sanity.SanitySignatureEventService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class SanityEventRegistry {
	private static final String[] FAKE_PLAYER_NAMES = {
			"Elias",
			"Marrow",
			"Noor",
			"Holloway",
			"Iris",
			"Jonah",
			"Aster",
			"Vanta"
	};

	private static final List<SanityEvent> EVENTS = List.of(
			event("floating_block", 0.006F, 20 * 20, 40, 70, 1, null, SanityEventRegistry::triggerFloatingBlock),
			event("fake_creeper", 0.007F, 20 * 18, 40, 46, 1, null, SanityEventRegistry::triggerFakeCreeper),
			event("entity_staring", 0.012F, 20 * 12, 40, 34, 1, null, SanityEventRegistry::triggerEntityStaring),
			event("footstep_echo", 0.020F, 20 * 8, 70, 0, 1, null, SanityEventRegistry::triggerFootstepEcho),
			event("sky_blink", 0.007F, 20 * 10, 70, 2, 1, null, SanityEventRegistry::triggerSkyBlink),
			event("shadow_runner", 0.007F, 20 * 15, 40, 24, 1, null, SanityEventRegistry::triggerShadowRunner),
			event("inventory_hallucination", 0.010F, 20 * 18, 40, 60, 1, null, SanityEventRegistry::triggerInventoryHallucination),
			event("world_freeze", 0.005F, 20 * 24, 20, 28, 2, null, SanityEventRegistry::triggerWorldFreeze),
			event("fake_damage", 0.013F, 20 * 9, 20, 20, 1, null, SanityEventRegistry::triggerFakeDamage),
			event("distant_scream", 0.010F, 20 * 18, 40, 0, 1, SanityEventRegistry::isForestOrCave, SanityEventRegistry::triggerDistantScream),
			event("fake_player", 0.006F, 20 * 20, 20, 72, 1, null, SanityEventRegistry::triggerFakePlayer),
			event("light_flicker", 0.014F, 20 * 10, 40, 16, 1, null, SanityEventRegistry::triggerLightFlicker),
			event("breathing_walls", 0.008F, 20 * 16, 20, 70, 1, SanityEventRegistry::isCave, SanityEventRegistry::triggerBreathingWalls),
			event("block_watcher", 0.010F, 20 * 18, 20, 80, 1, null, SanityEventRegistry::triggerBlockWatcher),
			event("phantom_hotbar_slot", 0.014F, 20 * 7, 20, 30, 1, null, SanityEventRegistry::triggerPhantomHotbar),
			event("sanity_whispers", 0.015F, 20 * 9, 70, 0, 1, null, SanityEventRegistry::triggerSanityWhispers),
			event("moving_trees", 0.007F, 20 * 18, 40, 80, 1, SanityEventRegistry::isForest, SanityEventRegistry::triggerMovingTrees),
			event("entity_distortion", 0.011F, 20 * 11, 20, 54, 1, SanityEventRegistry::hasNearbyHostiles, SanityEventRegistry::triggerEntityDistortion),
			event("gravity_pulse", 0.008F, 20 * 14, 20, 34, 1, null, SanityEventRegistry::triggerGravityPulse),
			event("mirror_player", 0.007F, 20 * 22, 20, 72, 1, null, SanityEventRegistry::triggerMirrorPlayer),
			event("blood_particle", 0.018F, 20 * 6, 20, 40, 1, null, SanityEventRegistry::triggerBloodParticles),
			event("impossible_return", 0.0035F, 20 * 95, 20, 34, 2, SanityEventRegistry::hasImpossibleReturnCandidate, (context, event) -> SanityPsychologicalService.triggerImpossibleReturn(context, event.durationTicks())),
			event("echo_step", 0.0045F, 20 * 60, 20, 20 * 8, 1, SanityEventRegistry::canTriggerEchoStep, SanityEventRegistry::triggerEchoStep),
			event("almost_mob", 0.0030F, 20 * 110, 20, 20 * 7, 1, null, SanityEventRegistry::triggerAlmostMob),
			event("memory_whisper", 0.0024F, 20 * 150, 20, 28, 1, null, (context, event) -> SanityPsychologicalService.triggerMemoryWhisper(context, event.durationTicks())),
			event("time_skip", 0.004F, 20 * 26, 5, 120, 1, null, SanityEventRegistry::triggerTimeSkip),
			event("wrong_sun", 0.0018F, 20 * 180, 5, 2, 1, null, SanityEventRegistry::triggerWrongSun),
			event("sound_reversal", 0.008F, 20 * 12, 20, 0, 1, null, SanityEventRegistry::triggerSoundReversal),
			event("ground_whisper", 0.018F, 20 * 6, 40, 0, 1, SanityEventRegistry::isOnWhisperGround, SanityEventRegistry::triggerGroundWhisper),
			event("world_observation", 0.010F, 20 * 16, 5, 36, 2, null, SanityEventRegistry::triggerWorldObservation),
			event("silent_world", 0.0012F, 20 * 240, 5, 20 * 6, 3, (context, event) -> SanitySignatureEventService.canTriggerSilentWorld(context), (context, event) -> SanitySignatureEventService.triggerSilentWorld(context)));
	private static final Map<String, SanityEvent> BY_ID = EVENTS.stream().collect(Collectors.toUnmodifiableMap(SanityEvent::id, event -> event));

	private SanityEventRegistry() {
	}

	public static List<SanityEvent> events() {
		return EVENTS;
	}

	public static SanityEvent get(String id) {
		return BY_ID.get(id);
	}

	private static BasicSanityEvent event(
			String id,
			float triggerChance,
			int cooldownTicks,
			int sanityRequirement,
			int durationTicks,
			int budgetCost,
			BiPredicate<SanityEventContext, BasicSanityEvent> gate,
			BiFunction<SanityEventContext, BasicSanityEvent, Boolean> action) {
		return new BasicSanityEvent(id, triggerChance, cooldownTicks, sanityRequirement, durationTicks, budgetCost, gate, action);
	}

	private static boolean triggerFloatingBlock(SanityEventContext context, BasicSanityEvent event) {
		return context.sendClientEvent(event.id(), context.player().blockPosition(), event.durationTicks(), 2 + context.random().nextInt(3), context.random().nextInt(), "", event.id());
	}

	private static boolean triggerFakeCreeper(SanityEventContext context, BasicSanityEvent event) {
		Vec3 spawn = findRingSpawn(context.player(), context.random(), 7.0D, 14.0D, 10, pos -> !isPlayerFacing(context.player(), centerOf(pos), 0.972D));
		return spawn != null && context.sendClientEvent(event.id(), BlockPos.containing(spawn), event.durationTicks(), 1, context.random().nextInt(), "", event.id());
	}

	private static boolean triggerEntityStaring(SanityEventContext context, BasicSanityEvent event) {
		List<Mob> nearby = new ArrayList<>(context.player().level().getEntitiesOfClass(
				Mob.class,
				context.player().getBoundingBox().inflate(16.0D),
				mob -> mob.isAlive() && !mob.isNoAi() && mob.distanceToSqr(context.player()) > 9.0D));
		if (nearby.isEmpty()) {
			return false;
		}
		int affected = Math.min(1 + context.random().nextInt(Math.min(3, nearby.size())), nearby.size());
		for (int i = 0; i < affected; i++) {
			freezeFacingPlayer(nearby.get(i), context.player(), event.durationTicks());
		}
		return true;
	}

	private static boolean triggerFootstepEcho(SanityEventContext context, BasicSanityEvent event) {
		BlockPos source = blockBehindPlayer(context.player(), 2 + context.random().nextInt(3));
		if (!context.player().level().hasChunkAt(source)) {
			return false;
		}
		BlockState below = context.player().level().getBlockState(source.below());
		SoundEvent sound = below.is(BlockTags.PLANKS) ? SoundEvents.WOOD_STEP
				: below.is(BlockTags.LEAVES) || below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.DIRT) ? SoundEvents.GRASS_STEP
				: SoundEvents.STONE_STEP;
		playPrivateSound(context.player(), sound, SoundSource.BLOCKS, centerOf(source), 0.24F + context.random().nextFloat() * 0.06F, 0.82F + context.random().nextFloat() * 0.14F);
		return true;
	}

	private static boolean triggerSkyBlink(SanityEventContext context, BasicSanityEvent event) {
		return context.sendClientEvent(event.id(), context.player().blockPosition(), event.durationTicks(), 1, 0, "", event.id());
	}

	private static boolean triggerShadowRunner(SanityEventContext context, BasicSanityEvent event) {
		Vec3 spawn = findRingSpawn(context.player(), context.random(), 12.0D, 22.0D, 12, pos -> hasNearbyCover((ServerLevel) context.player().level(), pos));
		return spawn != null && context.sendClientEvent(event.id(), BlockPos.containing(spawn), event.durationTicks(), 1, context.random().nextInt(), "", event.id());
	}

	private static boolean triggerInventoryHallucination(SanityEventContext context, BasicSanityEvent event) {
		return context.sendClientEvent(event.id(), context.player().blockPosition(), event.durationTicks(), 1, context.random().nextInt(), "", event.id());
	}

	private static boolean triggerWorldFreeze(SanityEventContext context, BasicSanityEvent event) {
		List<LivingEntity> nearby = new ArrayList<>(context.player().level().getEntitiesOfClass(
				LivingEntity.class,
				context.player().getBoundingBox().inflate(14.0D),
				entity -> entity.isAlive() && entity != context.player()));
		if (nearby.isEmpty()) {
			return false;
		}
		for (LivingEntity entity : nearby) {
			immobilizeEntity(entity, context.player(), event.durationTicks(), false);
		}
		return true;
	}

	private static boolean triggerFakeDamage(SanityEventContext context, BasicSanityEvent event) {
		playPrivateSound(context.player(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, context.player().position(), 0.65F, 0.82F + context.random().nextFloat() * 0.15F);
		return ClientEffectSyncService.sendHudDistortion(
				context.player(),
				18 + context.random().nextInt(10),
				context.sanity() <= 10 ? 4 : 3,
				true,
				event.id(),
				false);
	}

	private static boolean triggerDistantScream(SanityEventContext context, BasicSanityEvent event) {
		Vec3 source = findRingSpawn(context.player(), context.random(), 16.0D, 28.0D, 12, pos -> !isPlayerFacing(context.player(), centerOf(pos), 0.978D));
		if (source == null) {
			return false;
		}
		playPrivateSound(context.player(), SoundEvents.GHAST_SCREAM, SoundSource.AMBIENT, source, 0.16F + context.random().nextFloat() * 0.04F, 0.48F + context.random().nextFloat() * 0.12F);
		return true;
	}

	private static boolean triggerFakePlayer(SanityEventContext context, BasicSanityEvent event) {
		Vec3 spawn = findRingSpawn(context.player(), context.random(), 18.0D, 32.0D, 12, pos -> !isPlayerFacing(context.player(), centerOf(pos), 0.974D));
		return spawn != null && context.sendClientEvent(
				event.id(),
				BlockPos.containing(spawn),
				event.durationTicks(),
				1,
				context.random().nextInt(),
				FAKE_PLAYER_NAMES[context.random().nextInt(FAKE_PLAYER_NAMES.length)],
				event.id());
	}

	private static boolean triggerLightFlicker(SanityEventContext context, BasicSanityEvent event) {
		List<BlockPos> candidates = new ArrayList<>();
		BlockPos center = context.player().blockPosition();
		for (int x = -10; x <= 10; x++) {
			for (int y = -4; y <= 4; y++) {
				for (int z = -10; z <= 10; z++) {
					BlockPos pos = center.offset(x, y, z);
					if (!context.player().level().hasChunkAt(pos)) {
						continue;
					}
					BlockState state = context.player().level().getBlockState(pos);
					if (isLightSourceCandidate(state)) {
						candidates.add(pos.immutable());
					}
				}
			}
		}
		if (candidates.isEmpty()) {
			return false;
		}
		int count = Math.min(2 + context.random().nextInt(Math.min(3, candidates.size())), candidates.size());
		for (int i = 0; i < count; i++) {
			BlockPos pos = candidates.get(context.random().nextInt(candidates.size()));
			context.sendFalseBlock(pos, extinguishedVariant(context.player().level().getBlockState(pos)), 8 + context.random().nextInt(Math.max(6, event.durationTicks())));
		}
		return true;
	}

	private static boolean triggerBreathingWalls(SanityEventContext context, BasicSanityEvent event) {
		return context.sendClientEvent(event.id(), context.player().blockPosition(), event.durationTicks(), 1, context.random().nextInt(), "", event.id());
	}

	private static boolean triggerBlockWatcher(SanityEventContext context, BasicSanityEvent event) {
		HitResult hitResult = context.player().pick(10.0D, 0.0F, false);
		if (!(hitResult instanceof BlockHitResult blockHit) || hitResult.getType() != HitResult.Type.BLOCK) {
			return false;
		}
		BlockPos pos = blockHit.getBlockPos();
		BlockState current = context.player().level().getBlockState(pos);
		BlockState adjusted = current;
		if (current.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
			adjusted = current.setValue(BlockStateProperties.HORIZONTAL_FACING, horizontalDirectionToward(context.player(), pos));
		} else if (current.hasProperty(BlockStateProperties.FACING)) {
			adjusted = current.setValue(BlockStateProperties.FACING, directionToward(context.player(), pos));
		}
		if (adjusted == current) {
			return false;
		}
		return context.sendFalseBlock(pos, adjusted, event.durationTicks());
	}

	private static boolean triggerPhantomHotbar(SanityEventContext context, BasicSanityEvent event) {
		int slot = context.random().nextInt(9);
		return context.sendClientEvent(event.id(), context.player().blockPosition(), event.durationTicks(), 1, slot, "", event.id());
	}

	private static boolean triggerSanityWhispers(SanityEventContext context, BasicSanityEvent event) {
		Vec3 side = sideVector(context.player()).scale(context.random().nextBoolean() ? 1.7D : -1.7D);
		Vec3 source = context.player().getEyePosition().add(side).add(context.player().getLookAngle().scale(-0.6D));
		Holder<SoundEvent> sound = context.random().nextBoolean()
				? BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.AMBIENT_CAVE.value())
				: BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ENDERMAN_AMBIENT);
		playPrivateSound(context.player(), sound, SoundSource.AMBIENT, source, 0.12F + context.random().nextFloat() * 0.05F, 0.42F + context.random().nextFloat() * 0.18F);
		return true;
	}

	private static boolean triggerMovingTrees(SanityEventContext context, BasicSanityEvent event) {
		BlockPos center = context.player().blockPosition();
		List<BlockPos> leaves = new ArrayList<>();
		for (int x = -14; x <= 14; x++) {
			for (int y = -6; y <= 8; y++) {
				for (int z = -14; z <= 14; z++) {
					BlockPos pos = center.offset(x, y, z);
					if (!context.player().level().hasChunkAt(pos) || isPlayerFacing(context.player(), centerOf(pos), 0.982D)) {
						continue;
					}
					if (context.player().level().getBlockState(pos).is(BlockTags.LEAVES)) {
						leaves.add(pos.immutable());
					}
				}
			}
		}
		if (leaves.isEmpty()) {
			return false;
		}
		BlockPos base = leaves.get(context.random().nextInt(leaves.size()));
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos shifted = base.relative(direction);
			if (!context.player().level().getBlockState(shifted).isAir()) {
				continue;
			}
			context.sendFalseBlock(base, Blocks.AIR.defaultBlockState(), event.durationTicks());
			context.sendFalseBlock(shifted, Blocks.OAK_LEAVES.defaultBlockState(), event.durationTicks());
			return true;
		}
		return false;
	}

	private static boolean triggerEntityDistortion(SanityEventContext context, BasicSanityEvent event) {
		return context.sendClientEvent(event.id(), context.player().blockPosition(), event.durationTicks(), 2 + context.random().nextInt(2), context.random().nextInt(), "", event.id());
	}

	private static boolean triggerGravityPulse(SanityEventContext context, BasicSanityEvent event) {
		if (context.player().onGround()) {
			context.player().addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, event.durationTicks(), 0, false, false, false));
		} else {
			context.player().addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, Math.max(12, event.durationTicks() / 2), 0, false, false, false));
		}
		ClientEffectSyncService.sendScarePulse(context.player(), 10, 2, event.id(), false);
		return true;
	}

	private static boolean triggerMirrorPlayer(SanityEventContext context, BasicSanityEvent event) {
		Vec3 spawn = findRingSpawn(context.player(), context.random(), 18.0D, 34.0D, 12, pos -> !isPlayerFacing(context.player(), centerOf(pos), 0.972D));
		return spawn != null && context.sendClientEvent(event.id(), BlockPos.containing(spawn), event.durationTicks(), 1, context.random().nextInt(), "", event.id());
	}

	private static boolean triggerBloodParticles(SanityEventContext context, BasicSanityEvent event) {
		return context.sendClientEvent(event.id(), context.player().blockPosition(), event.durationTicks(), context.sanity() <= 5 ? 3 : 2, context.random().nextInt(), "", event.id());
	}

	private static boolean canTriggerEchoStep(SanityEventContext context, BasicSanityEvent event) {
		return context.player().onGround() && !context.player().isPassenger();
	}

	private static boolean triggerEchoStep(SanityEventContext context, BasicSanityEvent event) {
		int delayTicks = 40 + context.random().nextInt(41);
		return context.sendClientEvent(event.id(), context.player().blockPosition(), event.durationTicks(), delayTicks, context.random().nextInt(), "", event.id());
	}

	private static boolean triggerAlmostMob(SanityEventContext context, BasicSanityEvent event) {
		Vec3 spawn = findRingSpawn(context.player(), context.random(), 10.0D, 18.0D, 12, pos -> !isPlayerFacing(context.player(), centerOf(pos), 0.955D));
		if (spawn == null) {
			return false;
		}

		String mobType = chooseAlmostMobType(context.player(), context.random());
		return context.sendClientEvent(
				event.id(),
				BlockPos.containing(spawn),
				event.durationTicks(),
				1,
				context.random().nextInt(),
				mobType,
				event.id());
	}

	private static boolean triggerTimeSkip(SanityEventContext context, BasicSanityEvent event) {
		int skip = 60 + context.random().nextInt(Math.max(20, event.durationTicks()));
		return context.sendClientEvent(event.id(), context.player().blockPosition(), skip, 1, context.random().nextInt(), "", event.id());
	}

	private static boolean triggerWrongSun(SanityEventContext context, BasicSanityEvent event) {
		return context.sendClientEvent(event.id(), context.player().blockPosition(), Math.max(1, event.durationTicks()), 1, context.random().nextInt(), "", event.id());
	}

	private static boolean triggerSoundReversal(SanityEventContext context, BasicSanityEvent event) {
		BlockPos source = blockBehindPlayer(context.player(), 3 + context.random().nextInt(4));
		playPrivateSound(context.player(), SoundEvents.AMBIENT_CAVE, SoundSource.AMBIENT, centerOf(source), 0.14F + context.random().nextFloat() * 0.04F, 0.38F + context.random().nextFloat() * 0.10F);
		return true;
	}

	private static boolean triggerGroundWhisper(SanityEventContext context, BasicSanityEvent event) {
		Vec3 source = centerOf(context.player().blockPosition().below()).add(sideVector(context.player()).scale(context.random().nextBoolean() ? 0.8D : -0.8D));
		playPrivateSound(context.player(), SoundEvents.AMBIENT_CAVE, SoundSource.AMBIENT, source, 0.10F + context.random().nextFloat() * 0.04F, 0.52F + context.random().nextFloat() * 0.10F);
		return true;
	}

	private static boolean triggerWorldObservation(SanityEventContext context, BasicSanityEvent event) {
		List<Mob> nearby = new ArrayList<>(context.player().level().getEntitiesOfClass(
				Mob.class,
				context.player().getBoundingBox().inflate(20.0D),
				mob -> mob.isAlive() && !mob.isNoAi() && mob.distanceToSqr(context.player()) > 9.0D));
		if (nearby.isEmpty()) {
			return false;
		}
		int affected = Math.min(2 + context.random().nextInt(Math.min(4, nearby.size())), nearby.size());
		for (int i = 0; i < affected; i++) {
			freezeFacingPlayer(nearby.get(i), context.player(), event.durationTicks());
		}
		return true;
	}

	private static boolean hasImpossibleReturnCandidate(SanityEventContext context, BasicSanityEvent event) {
		return SanityPsychologicalService.hasImpossibleReturnCandidate(context.player(), context.gameTime());
	}

	private static boolean isForestOrCave(SanityEventContext context, BasicSanityEvent event) {
		return isForest(context, event) || isCave(context, event);
	}

	private static boolean isForest(SanityEventContext context, BasicSanityEvent event) {
		BlockPos pos = context.player().blockPosition();
		return context.player().level().dimension().equals(Level.OVERWORLD)
				&& (context.player().level().getBiome(pos).is(BiomeTags.IS_FOREST)
						|| context.player().level().getBiome(pos).is(BiomeTags.IS_TAIGA)
						|| context.player().level().getBiome(pos).is(BiomeTags.IS_JUNGLE));
	}

	private static boolean isCave(SanityEventContext context, BasicSanityEvent event) {
		BlockPos pos = context.player().blockPosition();
		return !context.player().level().canSeeSky(pos) && pos.getY() < context.player().level().getSeaLevel() + 10;
	}

	private static boolean hasNearbyHostiles(SanityEventContext context, BasicSanityEvent event) {
		return !context.player().level().getEntitiesOfClass(
				Monster.class,
				context.player().getBoundingBox().inflate(18.0D),
				monster -> monster.isAlive() && monster.distanceToSqr(context.player()) > 4.0D).isEmpty();
	}

	private static boolean isOnWhisperGround(SanityEventContext context, BasicSanityEvent event) {
		BlockState below = context.player().level().getBlockState(context.player().blockPosition().below());
		return below.is(Blocks.GRASS_BLOCK)
				|| below.is(Blocks.DIRT)
				|| below.is(Blocks.COARSE_DIRT)
				|| below.is(Blocks.PODZOL)
				|| below.is(Blocks.ROOTED_DIRT)
				|| below.is(Blocks.MOSS_BLOCK);
	}

	private static String chooseAlmostMobType(ServerPlayer player, net.minecraft.util.RandomSource random) {
		List<String> matchingNearby = new ArrayList<>();
		for (Mob mob : player.level().getEntitiesOfClass(
				Mob.class,
				player.getBoundingBox().inflate(18.0D),
				mob -> mob.isAlive() && mob.distanceToSqr(player) > 16.0D)) {
			if (mob instanceof Zombie) {
				matchingNearby.add("zombie");
			} else if (mob instanceof Skeleton) {
				matchingNearby.add("skeleton");
			} else if (mob instanceof Cow) {
				matchingNearby.add("cow");
			}
		}
		if (!matchingNearby.isEmpty()) {
			return matchingNearby.get(random.nextInt(matchingNearby.size()));
		}
		String[] fallback = {"zombie", "skeleton", "cow"};
		return fallback[random.nextInt(fallback.length)];
	}

	private static BlockPos blockBehindPlayer(ServerPlayer player, int distance) {
		Vec3 backward = player.getLookAngle().scale(-distance);
		return BlockPos.containing(player.getX() + backward.x, player.getY(), player.getZ() + backward.z);
	}

	private static Vec3 centerOf(BlockPos pos) {
		return new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
	}

	private static Vec3 sideVector(ServerPlayer player) {
		Vec3 look = player.getLookAngle();
		Vec3 side = new Vec3(-look.z, 0.0D, look.x);
		return side.lengthSqr() <= 1.0E-5D ? new Vec3(1.0D, 0.0D, 0.0D) : side.normalize();
	}

	private static boolean isPlayerFacing(ServerPlayer player, Vec3 target, double threshold) {
		Vec3 look = player.getLookAngle().normalize();
		Vec3 toTarget = target.subtract(player.getEyePosition()).normalize();
		return look.dot(toTarget) >= threshold;
	}

	private static boolean hasNearbyCover(ServerLevel level, BlockPos pos) {
		for (int x = -2; x <= 2; x++) {
			for (int y = -1; y <= 2; y++) {
				for (int z = -2; z <= 2; z++) {
					if (x == 0 && y == 0 && z == 0) {
						continue;
					}
					BlockState state = level.getBlockState(pos.offset(x, y, z));
					if (state.blocksMotion() || state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static Vec3 findStandablePosition(ServerLevel level, BlockPos base) {
		for (int dy = 4; dy >= -5; dy--) {
			BlockPos feet = base.offset(0, dy, 0);
			BlockPos head = feet.above();
			BlockPos below = feet.below();
			if (!level.hasChunkAt(feet)) {
				continue;
			}
			if (!level.getBlockState(feet).blocksMotion()
					&& !level.getBlockState(head).blocksMotion()
					&& level.getBlockState(below).blocksMotion()) {
				return new Vec3(feet.getX() + 0.5D, feet.getY(), feet.getZ() + 0.5D);
			}
		}
		return null;
	}

	private static Vec3 findRingSpawn(ServerPlayer player, net.minecraft.util.RandomSource random, double minDistance, double maxDistance, int attempts, Predicate<BlockPos> predicate) {
		ServerLevel level = (ServerLevel) player.level();
		BlockPos center = player.blockPosition();
		for (int attempt = 0; attempt < attempts; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2.0D;
			double distance = minDistance + random.nextDouble() * Math.max(1.0D, maxDistance - minDistance);
			int x = Mth.floor(player.getX() + Math.cos(angle) * distance);
			int z = Mth.floor(player.getZ() + Math.sin(angle) * distance);
			BlockPos probe = new BlockPos(x, center.getY(), z);
			if (!level.hasChunkAt(probe) || !predicate.test(probe)) {
				continue;
			}
			Vec3 stand = findStandablePosition(level, probe);
			if (stand != null) {
				return stand;
			}
		}
		return null;
	}

	private static void immobilizeEntity(LivingEntity entity, ServerPlayer player, int durationTicks, boolean facePlayer) {
		entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, durationTicks, 6, false, false, false));
		entity.setDeltaMovement(Vec3.ZERO);
		if (entity instanceof Mob mob) {
			mob.setTarget(null);
			mob.getNavigation().stop();
			if (facePlayer) {
				mob.getLookControl().setLookAt(player, 18.0F, 18.0F);
			}
		}
		if (facePlayer) {
			double dx = player.getX() - entity.getX();
			double dz = player.getZ() - entity.getZ();
			float yaw = (float) (Math.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
			entity.setYRot(yaw);
			entity.setYHeadRot(yaw);
			if (entity instanceof Mob mob) {
				mob.setYBodyRot(yaw);
			}
		}
	}

	private static void freezeFacingPlayer(Mob mob, ServerPlayer player, int durationTicks) {
		immobilizeEntity(mob, player, durationTicks, true);
	}

	private static boolean isLightSourceCandidate(BlockState state) {
		return state.is(Blocks.TORCH)
				|| state.is(Blocks.WALL_TORCH)
				|| state.is(Blocks.SOUL_TORCH)
				|| state.is(Blocks.SOUL_WALL_TORCH)
				|| state.is(Blocks.LANTERN)
				|| state.is(Blocks.SOUL_LANTERN)
				|| state.is(Blocks.CAMPFIRE)
				|| state.is(Blocks.SOUL_CAMPFIRE)
				|| state.is(BlockTags.CANDLES);
	}

	private static BlockState extinguishedVariant(BlockState state) {
		if (state.hasProperty(BlockStateProperties.LIT)) {
			return state.setValue(BlockStateProperties.LIT, false);
		}
		return Blocks.AIR.defaultBlockState();
	}

	private static Direction horizontalDirectionToward(ServerPlayer player, BlockPos pos) {
		Vec3 delta = player.position().subtract(centerOf(pos));
		return Direction.getApproximateNearest(delta.x, 0.0D, delta.z);
	}

	private static Direction directionToward(ServerPlayer player, BlockPos pos) {
		Vec3 delta = player.position().subtract(centerOf(pos));
		return Direction.getApproximateNearest(delta.x, delta.y, delta.z);
	}

	private static void playPrivateSound(ServerPlayer player, SoundEvent sound, SoundSource source, Vec3 position, float volume, float pitch) {
		playPrivateSound(player, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), source, position, volume, pitch);
	}

	private static void playPrivateSound(ServerPlayer player, Holder<SoundEvent> sound, SoundSource source, Vec3 position, float volume, float pitch) {
		player.connection.send(new ClientboundSoundPacket(
				sound,
				source,
				position.x,
				position.y,
				position.z,
				Math.max(0.01F, volume),
				Mth.clamp(pitch, 0.3F, 2.0F),
				player.getRandom().nextLong()));
	}

	record BasicSanityEvent(
			String id,
			float triggerChance,
			int cooldownTicks,
			int sanityRequirement,
			int durationTicks,
			int budgetCost,
			BiPredicate<SanityEventContext, BasicSanityEvent> gate,
			BiFunction<SanityEventContext, BasicSanityEvent, Boolean> action) implements SanityEvent {
		@Override
		public boolean canTrigger(SanityEventContext context) {
			return context.sanity() <= sanityRequirement && (gate == null || gate.test(context, this));
		}

		@Override
		public boolean trigger(SanityEventContext context) {
			return action != null && Boolean.TRUE.equals(action.apply(context, this));
		}
	}
}
