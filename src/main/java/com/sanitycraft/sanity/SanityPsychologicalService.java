package com.sanitycraft.sanity;

import com.sanitycraft.data.config.SanityCraftConfig;
import com.sanitycraft.entity.stalker.StalkerEntity;
import com.sanitycraft.registry.ModEntities;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class SanityPsychologicalService {
	private static final String[] WATCHING_LINES = {
			"%s, I can still see you.",
			"%s, that sound came from inside.",
			"%s, your base remembered me.",
			"%s, something moved behind you.",
			"%s, the door was closed a second ago."
	};
	private static final Map<UUID, List<FalseWorldIllusion>> ACTIVE_ILLUSIONS = new ConcurrentHashMap<>();
	private static final int WATCHING_MESSAGE_COOLDOWN = 20 * 14;
	private static final int FAKE_BLOCK_COOLDOWN = 20 * 8;
	private static final int FAKE_STRUCTURE_COOLDOWN = 20 * 12;
	private static final int CHEST_SOUND_COOLDOWN = 20 * 9;
	private static final int WINDOW_WATCHER_COOLDOWN = 20 * 11;
	private static final int FALSE_WORLD_DURATION = 20 * 3;

	private SanityPsychologicalService() {
	}

	public static void tickPlayer(ServerPlayer player, SanityComponent component, SanityThresholds.Stage stage, long gameTime) {
		expireIllusions(player, gameTime);
		if (stage == SanityThresholds.Stage.STABLE) {
			clearPlayer(player);
			return;
		}

		RandomSource random = player.getRandom();
		switch (stage) {
			case UNEASY -> {
				maybeSendWatchingMessage(player, component, random, 0.018F);
				maybePlayChestSounds(player, component, random, 0.024F);
			}
			case DISTURBED -> {
				maybeSendWatchingMessage(player, component, random, 0.028F);
				maybePlayChestSounds(player, component, random, 0.030F);
				maybeFakeBlockBehindPlayer(player, component, gameTime, random, 0.040F);
			}
			case FRACTURED -> {
				maybeSendWatchingMessage(player, component, random, 0.040F);
				maybePlayChestSounds(player, component, random, 0.042F);
				maybeFakeBlockBehindPlayer(player, component, gameTime, random, 0.055F);
				maybeFakeStructure(player, component, gameTime, random, 0.045F);
				maybeTriggerFalseSafety(player, component, random, false);
			}
			case COLLAPSE -> {
				maybeSendWatchingMessage(player, component, random, 0.060F);
				maybePlayChestSounds(player, component, random, 0.060F);
				maybeFakeBlockBehindPlayer(player, component, gameTime, random, 0.085F);
				maybeFakeStructure(player, component, gameTime, random, 0.070F);
				maybeTriggerFalseSafety(player, component, random, true);
			}
			default -> {
			}
		}
	}

	public static void clearPlayer(ServerPlayer player) {
		List<FalseWorldIllusion> illusions = ACTIVE_ILLUSIONS.remove(player.getUUID());
		if (illusions == null || illusions.isEmpty()) {
			return;
		}
		for (FalseWorldIllusion illusion : illusions) {
			revertIllusion(player, illusion.pos());
		}
	}

	public static void clearAll() {
		ACTIVE_ILLUSIONS.clear();
	}

	private static void maybeSendWatchingMessage(ServerPlayer player, SanityComponent component, RandomSource random, float chance) {
		if (!component.isCooldownReady(SanityComponent.Cooldown.WATCHING_MESSAGE) || !roll(random, chance)) {
			return;
		}
		if (!component.tryConsumeEffectBudget(SanityCraftConfig.get(), 1)) {
			return;
		}
		String template = WATCHING_LINES[random.nextInt(WATCHING_LINES.length)];
		player.sendSystemMessage(Component.literal(String.format(template, player.getGameProfile().getName())));
		component.setCooldown(SanityComponent.Cooldown.WATCHING_MESSAGE, jitter(WATCHING_MESSAGE_COOLDOWN, random));
		SanityDebug.logEvent(player, "watching_message");
	}

	private static void maybePlayChestSounds(ServerPlayer player, SanityComponent component, RandomSource random, float chance) {
		if (SanityAudioDirector.maybeChestSound(player, component, SanityCraftConfig.get(), random, chance, "psychological_false_chest")) {
			component.setCooldown(SanityComponent.Cooldown.CHEST_SOUND, jitter(CHEST_SOUND_COOLDOWN, random));
			SanityDebug.logEvent(player, "false_chest_sound");
		}
	}

	private static void maybeFakeBlockBehindPlayer(ServerPlayer player, SanityComponent component, long gameTime, RandomSource random, float chance) {
		if (!component.isCooldownReady(SanityComponent.Cooldown.FALSE_BLOCK) || !roll(random, chance)) {
			return;
		}
		if (!component.tryConsumeEffectBudget(SanityCraftConfig.get(), 1)) {
			return;
		}

		BlockPos target = blockBehindPlayer(player, 3 + random.nextInt(3));
		if (!player.level().hasChunkAt(target)) {
			return;
		}
		BlockState fake = player.level().getBlockState(target).isAir() ? Blocks.COBBLED_DEEPSLATE.defaultBlockState() : Blocks.AIR.defaultBlockState();
		if (sendFalseBlock(player, target, fake, gameTime + FALSE_WORLD_DURATION)) {
			component.setCooldown(SanityComponent.Cooldown.FALSE_BLOCK, jitter(FAKE_BLOCK_COOLDOWN, random));
			SanityDebug.logEvent(player, "false_block_update");
		}
	}

	private static void maybeFakeStructure(ServerPlayer player, SanityComponent component, long gameTime, RandomSource random, float chance) {
		if (!component.isCooldownReady(SanityComponent.Cooldown.FALSE_STRUCTURE) || !roll(random, chance)) {
			return;
		}
		if (!component.tryConsumeEffectBudget(SanityCraftConfig.get(), 2)) {
			return;
		}

		BlockPos anchor = blockAheadOfPlayer(player, 6 + random.nextInt(4));
		if (!player.level().hasChunkAt(anchor)) {
			return;
		}

		List<BlockPos> positions = new ArrayList<>();
		int variant = random.nextInt(3);
		if (variant == 0) {
			for (int y = 0; y < 4; y++) {
				positions.add(anchor.above(y));
			}
			positions.add(anchor.offset(-1, 0, 0));
			positions.add(anchor.offset(1, 0, 0));
			positions.add(anchor.offset(-1, 3, 0));
			positions.add(anchor.offset(1, 3, 0));
		} else if (variant == 1) {
			positions.add(anchor);
			positions.add(anchor.above());
			positions.add(anchor.above(2));
			positions.add(anchor.offset(-1, 1, 0));
			positions.add(anchor.offset(1, 1, 0));
			positions.add(anchor.offset(0, 1, -1));
			positions.add(anchor.offset(0, 1, 1));
		} else {
			for (int y = 0; y < 3; y++) {
				positions.add(anchor.offset(-1, y, 0));
				positions.add(anchor.offset(1, y, 0));
			}
			positions.add(anchor.above(3));
			positions.add(anchor.offset(0, 1, 0));
		}

		boolean sent = false;
		for (BlockPos pos : positions) {
			sent |= sendFalseBlock(player, pos, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), gameTime + FALSE_WORLD_DURATION + 20L);
		}
		if (sent) {
			component.setCooldown(SanityComponent.Cooldown.FALSE_STRUCTURE, jitter(FAKE_STRUCTURE_COOLDOWN, random));
			SanityDebug.logEvent(player, "false_structure");
		}
	}

	private static void maybeTriggerFalseSafety(ServerPlayer player, SanityComponent component, RandomSource random, boolean collapse) {
		if (!isInsideBase(player)) {
			return;
		}

		if (component.isCooldownReady(SanityComponent.Cooldown.BREATHING)
				&& roll(random, collapse ? 0.085F : 0.045F)
				&& component.tryConsumeEffectBudget(SanityCraftConfig.get(), 1)) {
			SanityAudioDirector.playFalseSafetyBreathing(player, collapse, "false_safety");
			component.setCooldown(SanityComponent.Cooldown.BREATHING, jitter(20 * 7, random));
		}
		if (!component.isCooldownReady(SanityComponent.Cooldown.WINDOW_WATCHER) || !roll(random, collapse ? 0.080F : 0.050F)) {
			return;
		}
		if (!component.tryConsumeEffectBudget(SanityCraftConfig.get(), 2)) {
			return;
		}

		Vec3 watcherPos = findWindowWatcherPosition(player);
		if (watcherPos != null) {
			if (SanityHallucinationService.spawnWatcherStalker(player, watcherPos, collapse ? 20 * 8 : 20 * 6, "window_shadow")) {
				component.setCooldown(SanityComponent.Cooldown.WINDOW_WATCHER, jitter(WINDOW_WATCHER_COOLDOWN, random));
			}
			return;
		}

		BlockPos fallback = blockAheadOfPlayer(player, 5 + random.nextInt(3));
		StalkerEntity stalker = ModEntities.STALKER.create(player.level(), EntitySpawnReason.EVENT);
		if (stalker == null) {
			return;
		}
		stalker.setPos(fallback.getX() + 0.5D, fallback.getY(), fallback.getZ() + 0.5D);
		stalker.configureHallucination(player, collapse ? 20 * 8 : 20 * 6, true);
		player.level().addFreshEntity(stalker);
		component.setCooldown(SanityComponent.Cooldown.WINDOW_WATCHER, jitter(WINDOW_WATCHER_COOLDOWN, random));
		SanityDebug.logHallucinationSuccess(player, "window_shadow", stalker.position(), "false_safety_fallback");
	}

	private static void expireIllusions(ServerPlayer player, long gameTime) {
		List<FalseWorldIllusion> illusions = ACTIVE_ILLUSIONS.get(player.getUUID());
		if (illusions == null || illusions.isEmpty()) {
			return;
		}
		Iterator<FalseWorldIllusion> iterator = illusions.iterator();
		while (iterator.hasNext()) {
			FalseWorldIllusion illusion = iterator.next();
			if (gameTime < illusion.expireTick()) {
				continue;
			}
			revertIllusion(player, illusion.pos());
			iterator.remove();
		}
		if (illusions.isEmpty()) {
			ACTIVE_ILLUSIONS.remove(player.getUUID());
		}
	}

	private static boolean sendFalseBlock(ServerPlayer player, BlockPos pos, BlockState fakeState, long expireTick) {
		if (!player.level().hasChunkAt(pos)) {
			return false;
		}
		player.connection.send(new ClientboundBlockUpdatePacket(pos, fakeState));
		ACTIVE_ILLUSIONS.computeIfAbsent(player.getUUID(), ignored -> new ArrayList<>()).add(new FalseWorldIllusion(pos.immutable(), expireTick));
		return true;
	}

	private static void revertIllusion(ServerPlayer player, BlockPos pos) {
		if (!player.level().hasChunkAt(pos)) {
			return;
		}
		player.connection.send(new ClientboundBlockUpdatePacket(pos, player.level().getBlockState(pos)));
	}

	private static BlockPos blockBehindPlayer(ServerPlayer player, int distance) {
		Vec3 backward = player.getLookAngle().scale(-distance);
		return BlockPos.containing(player.getX() + backward.x, player.getY(), player.getZ() + backward.z);
	}

	private static BlockPos blockAheadOfPlayer(ServerPlayer player, int distance) {
		Vec3 forward = player.getLookAngle().scale(distance);
		return BlockPos.containing(player.getX() + forward.x, player.getY(), player.getZ() + forward.z);
	}

	private static boolean isInsideBase(ServerPlayer player) {
		BlockPos center = player.blockPosition();
		boolean shelter = !player.level().canSeeSky(center) || player.level().getMaxLocalRawBrightness(center) >= 10;
		if (!shelter) {
			return false;
		}

		for (int x = -6; x <= 6; x++) {
			for (int y = -2; y <= 2; y++) {
				for (int z = -6; z <= 6; z++) {
					BlockState state = player.level().getBlockState(center.offset(x, y, z));
					if (state.is(Blocks.CHEST)
							|| state.is(Blocks.TRAPPED_CHEST)
							|| state.is(Blocks.BARREL)
							|| state.is(Blocks.CRAFTING_TABLE)
							|| state.is(Blocks.FURNACE)
							|| state.is(Blocks.BLAST_FURNACE)
							|| state.is(Blocks.SMOKER)
							|| state.is(Blocks.RED_BED)
							|| state.is(Blocks.WHITE_BED)
							|| state.is(Blocks.BLACK_BED)
							|| state.is(Blocks.CAMPFIRE)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static Vec3 findWindowWatcherPosition(ServerPlayer player) {
		BlockPos center = player.blockPosition();
		for (int x = -6; x <= 6; x++) {
			for (int y = -2; y <= 2; y++) {
				for (int z = -6; z <= 6; z++) {
					BlockPos window = center.offset(x, y, z);
					BlockState state = player.level().getBlockState(window);
					if (!state.is(Blocks.GLASS)
							&& !state.is(Blocks.GLASS_PANE)
							&& !state.is(Blocks.BLACK_STAINED_GLASS)
							&& !state.is(Blocks.BLACK_STAINED_GLASS_PANE)
							&& !state.is(Blocks.TINTED_GLASS)) {
						continue;
					}
					for (Direction direction : Direction.Plane.HORIZONTAL) {
						BlockPos inside = window.relative(direction.getOpposite());
						BlockPos outsideFeet = window.relative(direction);
						BlockPos outsideHead = outsideFeet.above();
						if (center.distManhattan(inside) > center.distManhattan(outsideFeet)) {
							continue;
						}
						if (!player.level().getBlockState(outsideFeet).blocksMotion()
								&& !player.level().getBlockState(outsideHead).blocksMotion()) {
							return new Vec3(outsideFeet.getX() + 0.5D, outsideFeet.getY(), outsideFeet.getZ() + 0.5D);
						}
					}
				}
			}
		}
		return null;
	}

	private static boolean roll(RandomSource random, float chance) {
		return random.nextFloat() < chance;
	}

	private static int jitter(int baseTicks, RandomSource random) {
		int variance = Math.max(1, baseTicks / 4);
		return Math.max(20, baseTicks - variance + random.nextInt(variance * 2 + 1));
	}

	private record FalseWorldIllusion(BlockPos pos, long expireTick) {
	}
}
