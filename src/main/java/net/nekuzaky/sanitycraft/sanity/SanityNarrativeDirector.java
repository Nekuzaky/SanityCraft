package net.nekuzaky.sanitycraft.sanity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.nekuzaky.sanitycraft.util.ServerWorkQueue;

public class SanityNarrativeDirector {
	private SanityNarrativeDirector() {
	}

	public static void tick(ServerPlayer player, PlayerSanityComponent component, SanityConfig config) {
		if (!config.hallucinationsEnabled || !config.narrativeEventsEnabled) {
			return;
		}
		if (component.hasHallucinationShield()) {
			return;
		}

		int sanity = component.getSanity();
		if (sanity > 70) {
			return;
		}

		RandomSource random = player.getRandom();
		if (sanity <= 45 && component.canWhisper() && random.nextFloat() < clamp01(config.narrativeWhisperChance) && component.tryConsumeHorrorEventBudget(config, random, 1)) {
			sendNarrativeWhisper(player, sanity, random);
			component.resetWhisperCooldown(random);
			SanityManager.debugEvent(player, "narrative_whisper");
		}

		if (sanity <= 55 && random.nextFloat() < clamp01(config.narrativeFootstepChance) && component.tryConsumeHorrorEventBudget(config, random, 1)) {
			playDistantFootstep(player, random);
			SanityManager.debugEvent(player, "narrative_footstep");
		}
		if (config.biomePersonalityEnabled && sanity <= 58 && random.nextFloat() < clamp01(config.biomePersonalityChance) && component.tryConsumeHorrorEventBudget(config, random, 1)) {
			playBiomePersonality(player, random);
			SanityManager.debugEvent(player, "biome_personality");
		}
		if (config.paranoiaMimicEnabled && sanity <= 50 && component.canPlayMimic() && random.nextFloat() < clamp01(config.paranoiaMimicChance) && component.tryConsumeHorrorEventBudget(config, random, 1)) {
			playParanoiaMimic(player, random);
			component.resetMimicCooldown(random);
			SanityManager.debugEvent(player, "paranoia_mimic");
		}
		if (config.eyeContactHallucinationEnabled && sanity <= 35 && component.canTriggerBaseEvent() && random.nextFloat() < clamp01(config.eyeContactChance) && component.tryConsumeHorrorEventBudget(config, random, 1)) {
			triggerEyeContact(player, random);
			component.resetBaseEventCooldown(random);
			SanityManager.debugEvent(player, "eye_contact");
		}
		if (config.shadowFigureEnabled && sanity <= 42 && component.canPlayMimic() && random.nextFloat() < clamp01(config.shadowFigureChance) && component.tryConsumeHorrorEventBudget(config, random, 1)) {
			triggerShadowFigure(player, random);
			component.resetMimicCooldown(random);
			SanityManager.debugEvent(player, "shadow_figure");
		}
		if (config.falseUiEventsEnabled && sanity <= 45 && component.canTriggerFalseUi() && random.nextFloat() < clamp01(config.falseUiEventChance) && component.tryConsumeHorrorEventBudget(config, random, 1)) {
			triggerFalseUiEvent(player, random);
			component.resetFalseUiCooldown(random);
			SanityManager.debugEvent(player, "false_ui_event");
		}
		if (sanity <= 58 && component.canTriggerBaseEvent() && random.nextFloat() < (0.06F + Math.max(0.0F, (55.0F - sanity) / 250.0F))) {
			BaseContext context = scanBaseContext(player);
			if (context.baseScore >= 2 && component.tryConsumeHorrorEventBudget(config, random, 1)) {
				triggerBaseEvent(player, context, random);
				component.resetBaseEventCooldown(random);
				SanityManager.debugEvent(player, "base_event");
			}
		}

		if (!config.streamerSafeMode && sanity <= 20 && component.canJumpscare() && random.nextFloat() < clamp01(config.narrativeJumpscareChance)
				&& component.tryConsumeHorrorEventBudget(config, random, 2)) {
			int variant = random.nextInt(3);
			int duration = random.nextIntBetweenInclusive(18, 30);
			SanityNetworking.triggerJumpscare(player, variant, duration);
			component.resetJumpscareCooldown(random);
			SanityManager.debugEvent(player, "jumpscare");
		}
		if (sanity <= 15 && random.nextFloat() < 0.15F && component.tryConsumeHorrorEventBudget(config, random, 2)) {
			ExtremeLowSanityEffects.triggerExtremeHorror(player, sanity, random);
			ExtremeLowSanityEffects.spawnDreadAura(player, random);
			SanityManager.debugEvent(player, "extreme_horror");
		}
	}

	private static void sendNarrativeWhisper(ServerPlayer player, int sanity, RandomSource random) {
		String[] lines = sanity <= 20
				? new String[] {"YOUR MIND IS NOT YOURS", "IT IS INSIDE THE WALLS", "DO NOT OPEN THE INVENTORY", "IT WALKS BEHIND YOU", "TURN AROUND NOW", "YOUR SHADOW MOVED WRONG", "COUNT YOUR FINGERS", "DO NOT LOOK UP"}
				: sanity <= 40 ? new String[] {"someone is near", "there is no safe place", "do not stop moving", "you missed a sound behind you", "the walls are closing", "nothing is real here", "trust nothing you see", "your reflection lied to you"}
						: new String[] {"the cave is breathing", "you should leave now", "something watches the light", "keep your eyes forward", "lights flicker for a reason", "silence is deafening", "empty places are never empty", "the darkness blinks"};
		player.displayClientMessage(Component.literal(lines[random.nextInt(lines.length)]), true);
	}

	private static void playDistantFootstep(ServerPlayer player, RandomSource random) {
		float pitch = 0.6F + random.nextFloat() * 0.35F;
		if (random.nextBoolean()) {
			player.playNotifySound(SoundEvents.WARDEN_STEP, SoundSource.HOSTILE, 0.55F, pitch);
		} else {
			player.playNotifySound(SoundEvents.ZOMBIE_STEP, SoundSource.HOSTILE, 0.55F, pitch);
		}
	}

	private static void playParanoiaMimic(ServerPlayer player, RandomSource random) {
		int roll = random.nextInt(5);
		if (roll == 0) {
			player.playNotifySound(SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE, 0.75F, 0.9F + random.nextFloat() * 0.12F);
			SanityJournal.log(player, "I swear I heard a creeper fuse.");
		} else if (roll == 1) {
			player.playNotifySound(SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.7F, 0.85F + random.nextFloat() * 0.2F);
			SanityJournal.log(player, "A chest opened somewhere nearby.");
		} else if (roll == 2) {
			player.playNotifySound(SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 0.7F, 0.8F + random.nextFloat() * 0.18F);
			SanityJournal.log(player, "A door moved in the dark.");
		} else if (roll == 3) {
			player.playNotifySound(SoundEvents.ITEM_BREAK.value(), SoundSource.PLAYERS, 0.65F, 0.65F + random.nextFloat() * 0.2F);
			SanityJournal.log(player, "Something snapped behind me.");
		} else {
			player.playNotifySound(SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 0.7F, 0.8F + random.nextFloat() * 0.2F);
			SanityJournal.log(player, "A trap sound echoed from nowhere.");
		}
	}

	private static void playBiomePersonality(ServerPlayer player, RandomSource random) {
		var biome = player.level().getBiome(player.blockPosition());
		if (biome.is(BiomeTags.IS_NETHER)) {
			player.playNotifySound(SoundEvents.GHAST_AMBIENT, SoundSource.HOSTILE, 0.65F, 0.75F + random.nextFloat() * 0.2F);
			SanityJournal.log(player, "The Nether whispered my name.");
		} else if (biome.is(BiomeTags.IS_OCEAN)) {
			player.playNotifySound(SoundEvents.DROWNED_AMBIENT_WATER, SoundSource.HOSTILE, 0.65F, 0.8F + random.nextFloat() * 0.2F);
			SanityJournal.log(player, "I heard breathing from the water.");
		} else if (biome.is(BiomeTags.IS_FOREST)) {
			player.playNotifySound(SoundEvents.FOX_SCREECH, SoundSource.AMBIENT, 0.6F, 0.7F + random.nextFloat() * 0.2F);
			SanityJournal.log(player, "The forest sounded wrong.");
		} else {
			player.playNotifySound(SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.6F, 0.72F + random.nextFloat() * 0.2F);
			SanityJournal.log(player, "The biome has a voice now.");
		}
	}

	private static void triggerFalseUiEvent(ServerPlayer player, RandomSource random) {
		int roll = random.nextInt(3);
		if (roll == 0) {
			player.displayClientMessage(Component.literal("Inventory corrupted. Re-indexing..."), true);
			player.playNotifySound(SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.55F, 0.7F + random.nextFloat() * 0.15F);
		} else if (roll == 1) {
			int fakePing = random.nextIntBetweenInclusive(420, 980);
			player.displayClientMessage(Component.literal("Server ping unstable: " + fakePing + "ms"), true);
			player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 0.5F, 0.5F);
		} else {
			player.displayClientMessage(Component.literal("Advancement made! The Mind Watches Back"), true);
			player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.6F, 0.65F + random.nextFloat() * 0.2F);
		}
		SanityJournal.log(player, "My UI lied to me again.");
	}

	private static void triggerBaseEvent(ServerPlayer player, BaseContext context, RandomSource random) {
		int attempts = 0;
		while (attempts++ < 5) {
			int roll = random.nextInt(5);
			if (roll == 0 && context.doorPos != null) {
				triggerDoorKnock(player, context.doorPos, random);
				return;
			}
			if (roll == 1 && context.storagePos != null) {
				triggerStorageRattle(player, context.storagePos, random);
				return;
			}
			if (roll == 2 && context.lightPos != null) {
				triggerLightSputter(player, context.lightPos, random);
				return;
			}
			if (roll == 3 && context.windowPos != null) {
				triggerWindowTap(player, context.windowPos, random);
				return;
			}
			if (roll == 4) {
				triggerRoofFootsteps(player, context.roofPos != null ? context.roofPos : player.blockPosition().above(2), random);
				return;
			}
		}
		triggerRoofFootsteps(player, context.roofPos != null ? context.roofPos : player.blockPosition().above(2), random);
	}

	private static void triggerDoorKnock(ServerPlayer player, BlockPos doorPos, RandomSource random) {
		ServerLevel level = player.level();
		level.playSound(null, doorPos, SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 0.85F, 0.72F + random.nextFloat() * 0.12F);
		ServerWorkQueue.queue(8, () -> {
			if (player.isRemoved() || !player.level().dimension().equals(level.dimension())) {
				return;
			}
			level.playSound(null, doorPos, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 0.72F, 0.68F + random.nextFloat() * 0.14F);
		});
		SanityNetworking.triggerScarePulse(player, 6, 2);
		SanityJournal.log(player, "Someone knocked on the door. Nobody answered.");
	}

	private static void triggerStorageRattle(ServerPlayer player, BlockPos storagePos, RandomSource random) {
		ServerLevel level = player.level();
		level.playSound(null, storagePos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.72F, 0.85F + random.nextFloat() * 0.15F);
		ServerWorkQueue.queue(10, () -> {
			if (player.isRemoved() || !player.level().dimension().equals(level.dimension())) {
				return;
			}
			level.playSound(null, storagePos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.80F, 0.70F + random.nextFloat() * 0.12F);
		});
		SanityJournal.log(player, "I heard a chest lid move in the base.");
	}

	private static void triggerLightSputter(ServerPlayer player, BlockPos lightPos, RandomSource random) {
		ServerLevel level = player.level();
		level.sendParticles(player, ParticleTypes.SMOKE, true, false, lightPos.getX() + 0.5D, lightPos.getY() + 0.6D, lightPos.getZ() + 0.5D, 10, 0.18D, 0.20D, 0.18D, 0.01D);
		level.playSound(null, lightPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.60F, 0.75F + random.nextFloat() * 0.18F);
		SanityNetworking.triggerScarePulse(player, 5, 2);
		SanityJournal.log(player, "The light coughed smoke for no reason.");
	}

	private static void triggerWindowTap(ServerPlayer player, BlockPos windowPos, RandomSource random) {
		ServerLevel level = player.level();
		level.playSound(null, windowPos, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 0.72F, 0.75F + random.nextFloat() * 0.12F);
		ServerWorkQueue.queue(6, () -> {
			if (player.isRemoved() || !player.level().dimension().equals(level.dimension())) {
				return;
			}
			level.playSound(null, windowPos, SoundEvents.ITEM_BREAK.value(), SoundSource.BLOCKS, 0.45F, 0.95F + random.nextFloat() * 0.20F);
		});
		SanityJournal.log(player, "Something tapped the window, then stopped.");
	}

	private static void triggerRoofFootsteps(ServerPlayer player, BlockPos roofPos, RandomSource random) {
		ServerLevel level = player.level();
		level.playSound(null, roofPos, SoundEvents.WARDEN_STEP, SoundSource.HOSTILE, 0.85F, 0.80F + random.nextFloat() * 0.14F);
		ServerWorkQueue.queue(5, () -> {
			if (player.isRemoved() || !player.level().dimension().equals(level.dimension())) {
				return;
			}
			level.playSound(null, roofPos.offset(random.nextIntBetweenInclusive(-2, 2), 0, random.nextIntBetweenInclusive(-2, 2)), SoundEvents.ZOMBIE_STEP, SoundSource.HOSTILE, 0.82F,
					0.74F + random.nextFloat() * 0.18F);
		});
		ServerWorkQueue.queue(10, () -> {
			if (player.isRemoved() || !player.level().dimension().equals(level.dimension())) {
				return;
			}
			level.playSound(null, roofPos.offset(random.nextIntBetweenInclusive(-1, 1), 0, random.nextIntBetweenInclusive(-1, 1)), SoundEvents.WARDEN_NEARBY_CLOSER, SoundSource.HOSTILE, 0.90F,
					0.65F + random.nextFloat() * 0.2F);
		});
		SanityNetworking.triggerScarePulse(player, 10, 3);
		SanityJournal.log(player, "Something dragged itself across the roof.");
	}

	private static BaseContext scanBaseContext(ServerPlayer player) {
		BlockPos center = player.blockPosition();
		BaseContext context = new BaseContext();
		boolean bedFound = false;
		boolean storageFound = false;
		boolean lightFound = false;
		boolean doorFound = false;
		boolean windowFound = false;
		boolean workstationFound = false;
		for (int x = -6; x <= 6; x++) {
			for (int y = -3; y <= 4; y++) {
				for (int z = -6; z <= 6; z++) {
					BlockPos check = center.offset(x, y, z);
					BlockState state = player.level().getBlockState(check);
					if (state.isAir()) {
						continue;
					}
					if (!bedFound && state.is(BlockTags.BEDS)) {
						context.baseScore += 2;
						context.roofPos = check.above(2);
						bedFound = true;
						continue;
					}
					if (!storageFound && isStorageBlock(state)) {
						context.baseScore += 2;
						context.storagePos = check.immutable();
						storageFound = true;
						continue;
					}
					if (!doorFound && state.is(BlockTags.DOORS)) {
						context.baseScore += 1;
						context.doorPos = check.immutable();
						context.roofPos = check.above(2);
						doorFound = true;
						continue;
					}
					if (!lightFound && isLightBlock(state)) {
						context.baseScore += 1;
						context.lightPos = check.immutable();
						lightFound = true;
						continue;
					}
					if (!windowFound && isWindowBlock(state)) {
						context.baseScore += 1;
						context.windowPos = check.immutable();
						windowFound = true;
						continue;
					}
					if (!workstationFound && isWorkstationBlock(state)) {
						context.baseScore += 1;
						workstationFound = true;
					}
				}
			}
		}
		if (!player.level().canSeeSky(center)) {
			context.baseScore += 1;
		}
		if (context.roofPos == null) {
			context.roofPos = center.above(2);
		}
		return context;
	}

	private static boolean isStorageBlock(BlockState state) {
		return state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST) || state.is(Blocks.BARREL) || state.is(Blocks.ENDER_CHEST);
	}

	private static boolean isLightBlock(BlockState state) {
		return state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH) || state.is(Blocks.SOUL_TORCH) || state.is(Blocks.SOUL_WALL_TORCH) || state.is(Blocks.REDSTONE_TORCH)
				|| state.is(Blocks.REDSTONE_WALL_TORCH) || state.is(Blocks.LANTERN) || state.is(Blocks.SOUL_LANTERN) || state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE);
	}

	private static boolean isWindowBlock(BlockState state) {
		String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
		return path.contains("glass");
	}

	private static boolean isWorkstationBlock(BlockState state) {
		return state.is(Blocks.CRAFTING_TABLE) || state.is(Blocks.FURNACE) || state.is(Blocks.BLAST_FURNACE) || state.is(Blocks.SMOKER) || state.is(Blocks.ANVIL) || state.is(Blocks.CHIPPED_ANVIL)
				|| state.is(Blocks.DAMAGED_ANVIL) || state.is(Blocks.ENCHANTING_TABLE);
	}

	private static void triggerEyeContact(ServerPlayer player, RandomSource random) {
		player.displayClientMessage(Component.literal("§c§lIT'S LOOKING AT YOU"), true);
		player.playNotifySound(SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 0.95F, 0.4F + random.nextFloat() * 0.2F);
		SanityNetworking.triggerScarePulse(player, 8, 2);
		SanityJournal.log(player, "I felt something's eyes on me. It was real.");
	}

	private static void triggerShadowFigure(ServerPlayer player, RandomSource random) {
		String[] messages = {"A shadow moved where nothing exists.", "Something tall stood in the corner.", "Dark shapes have weight here.", "The shadows are deeper now."};
		player.displayClientMessage(Component.literal("§0" + messages[random.nextInt(messages.length)]), true);
		player.playNotifySound(SoundEvents.ENDERMAN_AMBIENT, SoundSource.HOSTILE, 0.80F, 0.5F + random.nextFloat() * 0.15F);
		SanityNetworking.triggerScarePulse(player, 6, 1);
		SanityJournal.log(player, "The shadows are beginning to think.");
	}

	private static float clamp01(float value) {
		if (value < 0.0F) {
			return 0.0F;
		}
		return Math.min(1.0F, value);
	}

	private static final class BaseContext {
		private int baseScore = 0;
		private BlockPos doorPos;
		private BlockPos storagePos;
		private BlockPos lightPos;
		private BlockPos windowPos;
		private BlockPos roofPos;
	}
}
