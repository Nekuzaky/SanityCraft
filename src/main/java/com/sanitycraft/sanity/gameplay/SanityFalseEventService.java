package com.sanitycraft.sanity.gameplay;

import com.sanitycraft.data.config.SanityCraftConfig;
import com.sanitycraft.sanity.SanityAudioContextRules;
import com.sanitycraft.sanity.SanityAudioEvent;
import com.sanitycraft.sanity.SanityComponent;
import com.sanitycraft.sanity.SanityDebug;
import com.sanitycraft.sanity.SanityThresholds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class SanityFalseEventService {
	private static final int FALSE_EVENT_COOLDOWN = 20 * 8;

	private SanityFalseEventService() {
	}

	public static void tickPlayer(ServerPlayer player, SanityComponent component, SanityThresholds.Stage stage) {
		if (component.getSanity() >= 50 || !component.isCooldownReady(SanityComponent.Cooldown.FALSE_EVENT)) {
			return;
		}

		RandomSource random = player.getRandom();
		float chance = switch (stage) {
			case DISTURBED -> 0.035F;
			case FRACTURED -> 0.050F;
			case COLLAPSE -> 0.068F;
			default -> 0.0F;
		};
		if (!SanityGameplayUtil.roll(random, chance)) {
			return;
		}
		if (!component.tryConsumeEffectBudget(SanityCraftConfig.get(), 1)) {
			return;
		}

		String result = triggerFalseEvent(player, random);
		if (result != null) {
			component.setCooldown(SanityComponent.Cooldown.FALSE_EVENT, SanityGameplayUtil.jitter(FALSE_EVENT_COOLDOWN, random, 20 * 5));
			SanityDebug.logEvent(player, "false_event type=" + result);
		}
	}

	public static String debugTrigger(ServerPlayer player) {
		String result = triggerFalseEvent(player, player.getRandom());
		return result == null ? "No believable false event could be placed here." : "Triggered false event: " + result;
	}

	private static String triggerFalseEvent(ServerPlayer player, RandomSource random) {
		int start = random.nextInt(5);
		for (int offset = 0; offset < 5; offset++) {
			int index = (start + offset) % 5;
			String result = switch (index) {
				case 0 -> playFootstepsBehind(player, random);
				case 1 -> playNearbyChest(player, random);
				case 2 -> playDoorSound(player, random);
				case 3 -> playBlockBreak(player, random);
				default -> playMobCue(player, random);
			};
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	private static String playFootstepsBehind(ServerPlayer player, RandomSource random) {
		BlockPos source = SanityGameplayUtil.blockBehindPlayer(player, 2 + random.nextInt(3));
		if (!player.level().hasChunkAt(source)) {
			return null;
		}
		BlockState below = player.level().getBlockState(source.below());
		SoundEvent sound;
		SoundSource soundSource;
		if (below.is(BlockTags.PLANKS) || below.is(BlockTags.WOODEN_STAIRS) || below.is(BlockTags.WOODEN_SLABS)) {
			sound = SoundEvents.WOOD_STEP;
			soundSource = SoundSource.BLOCKS;
		} else if (below.is(BlockTags.LEAVES) || below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.DIRT) || isForestBiome(player, source)) {
			sound = random.nextBoolean() ? SoundEvents.GRASS_STEP : SoundEvents.AZALEA_LEAVES_STEP;
			soundSource = SoundSource.AMBIENT;
		} else {
			sound = SoundEvents.STONE_STEP;
			soundSource = SoundSource.BLOCKS;
		}
		playPrivateSound(player, sound, soundSource, SanityGameplayUtil.centerOf(source), 0.22F + random.nextFloat() * 0.08F, 0.84F + random.nextFloat() * 0.16F);
		return "footsteps";
	}

	private static String playNearbyChest(ServerPlayer player, RandomSource random) {
		SanityAudioContextRules.AudioDecision decision = SanityAudioContextRules.evaluate(player, SanityAudioEvent.CHEST_SOUND, random, SanityCraftConfig.get());
		if (!decision.accepted()) {
			return null;
		}
		SoundEvent sound = decision.anchor() != null && decision.anchor().state().is(Blocks.BARREL)
				? (random.nextBoolean() ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE)
				: (random.nextBoolean() ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE);
		playPrivateSound(player, sound, SoundSource.BLOCKS, decision.sourcePos(), 0.46F + random.nextFloat() * 0.10F, 0.82F + random.nextFloat() * 0.14F);
		return "container";
	}

	private static String playDoorSound(ServerPlayer player, RandomSource random) {
		List<BlockPos> anchors = new ArrayList<>();
		BlockPos center = player.blockPosition();
		for (int x = -10; x <= 10; x++) {
			for (int y = -3; y <= 3; y++) {
				for (int z = -10; z <= 10; z++) {
					BlockPos pos = center.offset(x, y, z);
					if (!player.level().hasChunkAt(pos)) {
						continue;
					}
					BlockState state = player.level().getBlockState(pos);
					if (state.is(BlockTags.WOODEN_DOORS) || state.is(BlockTags.WOODEN_TRAPDOORS) || state.is(BlockTags.FENCE_GATES)) {
						anchors.add(pos.immutable());
					}
				}
			}
		}
		if (anchors.isEmpty()) {
			return null;
		}
		BlockPos anchor = anchors.get(random.nextInt(anchors.size()));
		SoundEvent sound = player.level().getBlockState(anchor).is(BlockTags.WOODEN_TRAPDOORS)
				? (random.nextBoolean() ? SoundEvents.WOODEN_TRAPDOOR_OPEN : SoundEvents.WOODEN_TRAPDOOR_CLOSE)
				: (random.nextBoolean() ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE);
		playPrivateSound(player, sound, SoundSource.BLOCKS, SanityGameplayUtil.centerOf(anchor), 0.32F + random.nextFloat() * 0.08F, 0.74F + random.nextFloat() * 0.18F);
		return "door";
	}

	private static String playBlockBreak(ServerPlayer player, RandomSource random) {
		List<BlockPos> anchors = new ArrayList<>();
		BlockPos center = player.blockPosition();
		for (int x = -10; x <= 10; x++) {
			for (int y = -4; y <= 4; y++) {
				for (int z = -10; z <= 10; z++) {
					BlockPos pos = center.offset(x, y, z);
					if (!player.level().hasChunkAt(pos) || SanityGameplayUtil.isPlayerFacing(player, SanityGameplayUtil.centerOf(pos), 0.985D)) {
						continue;
					}
					BlockState state = player.level().getBlockState(pos);
					if (state.isAir() || !state.blocksMotion()) {
						continue;
					}
					anchors.add(pos.immutable());
				}
			}
		}
		if (anchors.isEmpty()) {
			return null;
		}
		BlockPos anchor = anchors.get(random.nextInt(anchors.size()));
		BlockState state = player.level().getBlockState(anchor);
		SoundEvent sound = breakSoundFor(state);
		playPrivateSound(player, sound, SoundSource.BLOCKS, SanityGameplayUtil.centerOf(anchor), 0.30F + random.nextFloat() * 0.10F, 0.76F + random.nextFloat() * 0.18F);
		return "block_break";
	}

	private static String playMobCue(ServerPlayer player, RandomSource random) {
		List<Mob> candidates = player.level().getEntitiesOfClass(
				Mob.class,
				player.getBoundingBox().inflate(24.0D),
				mob -> mob.isAlive() && !mob.isRemoved() && mob.distanceToSqr(player) > 5.0D * 5.0D);
		if (candidates.isEmpty()) {
			return null;
		}

		Mob mob = candidates.get(random.nextInt(candidates.size()));
		SoundEvent sound = ambientSoundFor(mob);
		if (sound == null) {
			return null;
		}
		playPrivateSound(player, sound, SoundSource.HOSTILE, mob.position().add(0.0D, mob.getBbHeight() * 0.5D, 0.0D), 0.22F + random.nextFloat() * 0.08F, 0.80F + random.nextFloat() * 0.16F);
		return "mob_" + mob.getType().getDescriptionId();
	}

	private static boolean isForestBiome(ServerPlayer player, BlockPos pos) {
		return player.level().dimension().equals(Level.OVERWORLD)
				&& (player.level().getBiome(pos).is(BiomeTags.IS_FOREST)
						|| player.level().getBiome(pos).is(BiomeTags.IS_TAIGA)
						|| player.level().getBiome(pos).is(BiomeTags.IS_JUNGLE));
	}

	private static SoundEvent breakSoundFor(BlockState state) {
		if (state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS)) {
			return SoundEvents.WOOD_BREAK;
		}
		if (state.is(BlockTags.LEAVES) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT)) {
			return SoundEvents.GRASS_BREAK;
		}
		if (state.is(Blocks.GLASS) || state.is(Blocks.GLASS_PANE) || state.is(Blocks.TINTED_GLASS)) {
			return SoundEvents.GLASS_BREAK;
		}
		return SoundEvents.STONE_BREAK;
	}

	private static SoundEvent ambientSoundFor(Mob mob) {
		if (mob instanceof Zombie) {
			return SoundEvents.ZOMBIE_AMBIENT;
		}
		if (mob instanceof Skeleton) {
			return SoundEvents.SKELETON_AMBIENT;
		}
		if (mob instanceof Spider) {
			return SoundEvents.SPIDER_AMBIENT;
		}
		if (mob instanceof EnderMan) {
			return SoundEvents.ENDERMAN_AMBIENT;
		}
		if (mob instanceof Fox) {
			return SoundEvents.FOX_AMBIENT;
		}
		if (mob instanceof Wolf) {
			return SoundEvents.WOLF_SHAKE;
		}
		if (mob instanceof Cow) {
			return SoundEvents.COW_AMBIENT;
		}
		if (mob instanceof Sheep) {
			return SoundEvents.SHEEP_AMBIENT;
		}
		return null;
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
				Mth.clamp(pitch, 0.5F, 2.0F),
				player.getRandom().nextLong()));
	}
}
