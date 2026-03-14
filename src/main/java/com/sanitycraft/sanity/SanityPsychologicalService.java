package com.sanitycraft.sanity;

import com.sanitycraft.data.config.SanityCraftConfig;
import com.sanitycraft.entity.stalker.StalkerEntity;
import com.sanitycraft.sanity.events.SanityEventContext;
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
	private static final int MAX_RECENT_CHANGES = 40;
	private static final int IMPOSSIBLE_RETURN_MAX_AGE = 20 * 60 * 4;
	private static final int IMPOSSIBLE_RETURN_MIN_AGE = 20 * 8;
	private static final int IMPOSSIBLE_RETURN_DEPART_DISTANCE = 18;
	private static final int IMPOSSIBLE_RETURN_REVISIT_DISTANCE = 8;
	private static final int IMPOSSIBLE_RETURN_GROUP_RADIUS = 6;
	private static final int IMPOSSIBLE_RETURN_REPLAY_COOLDOWN = 20 * 35;
	private static final int PENDING_CHANGE_DELAY_TICKS = 2;
	private static final String[] MEMORY_WHISPER_KEYS = {
			"sanitycraft.memory_whisper.already_here",
			"sanitycraft.memory_whisper.heard_that_before",
			"sanitycraft.memory_whisper.left_something_behind",
			"sanitycraft.memory_whisper.this_path_knows_you",
			"sanitycraft.memory_whisper.you_closed_that"
	};
	private static final Map<UUID, List<RecentWorldChange>> RECENT_WORLD_CHANGES = new ConcurrentHashMap<>();
	private static final Map<UUID, List<PendingWorldChange>> PENDING_WORLD_CHANGES = new ConcurrentHashMap<>();

	private SanityPsychologicalService() {
	}

	public static void tickPlayer(ServerPlayer player, SanityComponent component, SanityThresholds.Stage stage, long gameTime) {
		expireIllusions(player, gameTime);
		tickRecentWorldChanges(player, gameTime);
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
				triggerFalseSafety(player, component, random, false, false, "false_safety");
			}
			case COLLAPSE -> {
				maybeSendWatchingMessage(player, component, random, 0.060F);
				maybePlayChestSounds(player, component, random, 0.060F);
				maybeFakeBlockBehindPlayer(player, component, gameTime, random, 0.085F);
				maybeFakeStructure(player, component, gameTime, random, 0.070F);
				triggerFalseSafety(player, component, random, true, false, "false_safety");
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

	public static void clearPlayerHistory(ServerPlayer player) {
		RECENT_WORLD_CHANGES.remove(player.getUUID());
		PENDING_WORLD_CHANGES.remove(player.getUUID());
	}

	public static void clearAll() {
		ACTIVE_ILLUSIONS.clear();
		RECENT_WORLD_CHANGES.clear();
		PENDING_WORLD_CHANGES.clear();
	}

	public static boolean isPlayerInsideBase(ServerPlayer player) {
		return isInsideBase(player);
	}

	public static boolean triggerFalseSafety(ServerPlayer player, SanityComponent component, boolean collapse, boolean forced, String source) {
		return triggerFalseSafety(player, component, player.getRandom(), collapse, forced, source);
	}

	public static void recordBrokenBlock(ServerPlayer player, BlockPos pos, BlockState previousState, long gameTime) {
		recordWorldChange(player, pos, previousState, Blocks.AIR.defaultBlockState(), gameTime);
	}

	public static void recordPotentialPlacement(ServerPlayer player, BlockPos pos, BlockState previousState, long gameTime) {
		if (!player.level().hasChunkAt(pos)) {
			return;
		}
		PENDING_WORLD_CHANGES.computeIfAbsent(player.getUUID(), ignored -> new ArrayList<>())
				.add(new PendingWorldChange(pos.immutable(), previousState, gameTime + PENDING_CHANGE_DELAY_TICKS));
	}

	public static void recordDoorInteraction(ServerPlayer player, BlockPos pos, BlockState previousState, long gameTime) {
		recordPotentialPlacement(player, pos, previousState, gameTime);
	}

	public static boolean hasImpossibleReturnCandidate(ServerPlayer player, long gameTime) {
		return !findImpossibleReturnCluster(player, gameTime, player.getRandom()).isEmpty();
	}

	public static boolean triggerImpossibleReturn(SanityEventContext context, int durationTicks) {
		List<RecentWorldChange> cluster = findImpossibleReturnCluster(context.player(), context.gameTime(), context.random());
		if (cluster.isEmpty()) {
			return false;
		}

		int duration = Math.max(20, durationTicks + context.random().nextInt(16));
		boolean sent = false;
		for (RecentWorldChange change : cluster) {
			if (!context.player().level().getBlockState(change.pos()).equals(change.actualState())) {
				continue;
			}
			sent |= context.sendFalseBlock(change.pos(), change.rememberedState(), duration);
			change.markReplayed(context.gameTime());
		}
		return sent;
	}

	public static boolean triggerMemoryWhisper(SanityEventContext context, int durationTicks) {
		String key = MEMORY_WHISPER_KEYS[context.random().nextInt(MEMORY_WHISPER_KEYS.length)];
		return context.sendClientEvent("memory_whisper", context.player().blockPosition(), durationTicks, 1, context.random().nextInt(), key, "memory_whisper");
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

	private static boolean triggerFalseSafety(
			ServerPlayer player,
			SanityComponent component,
			RandomSource random,
			boolean collapse,
			boolean forced,
			String source) {
		if (!forced && !isInsideBase(player)) {
			return false;
		}

		boolean triggered = false;
		if (forced) {
			triggered |= SanityAudioDirector.playFalseChest(player, source + "_chest");
		}
		if ((forced || component.isCooldownReady(SanityComponent.Cooldown.BREATHING))
				&& (forced || roll(random, collapse ? 0.085F : 0.045F))
				&& (forced || component.tryConsumeEffectBudget(SanityCraftConfig.get(), 1))) {
			triggered |= SanityAudioDirector.playFalseSafetyBreathing(player, collapse, source + "_breathing");
			if (!forced) {
				component.setCooldown(SanityComponent.Cooldown.BREATHING, jitter(20 * 7, random));
			}
		}
		if (!forced && (!component.isCooldownReady(SanityComponent.Cooldown.WINDOW_WATCHER) || !roll(random, collapse ? 0.080F : 0.050F))) {
			return triggered;
		}
		if (!forced && !component.tryConsumeEffectBudget(SanityCraftConfig.get(), 2)) {
			return triggered;
		}

		Vec3 watcherPos = findWindowWatcherPosition(player);
		if (watcherPos != null) {
			if (SanityHallucinationService.spawnWatcherStalker(player, watcherPos, collapse ? 20 * 8 : 20 * 6, source + "_window")) {
				triggered = true;
				if (!forced) {
					component.setCooldown(SanityComponent.Cooldown.WINDOW_WATCHER, jitter(WINDOW_WATCHER_COOLDOWN, random));
				}
			}
			return triggered;
		}

		BlockPos fallback = blockAheadOfPlayer(player, 5 + random.nextInt(3));
		StalkerEntity stalker = ModEntities.STALKER.create(player.level(), EntitySpawnReason.EVENT);
		if (stalker == null) {
			return triggered;
		}
		stalker.setPos(fallback.getX() + 0.5D, fallback.getY(), fallback.getZ() + 0.5D);
		stalker.configureHallucination(player, collapse ? 20 * 8 : 20 * 6, true);
		player.level().addFreshEntity(stalker);
		if (!forced) {
			component.setCooldown(SanityComponent.Cooldown.WINDOW_WATCHER, jitter(WINDOW_WATCHER_COOLDOWN, random));
		}
		SanityDebug.logHallucinationSuccess(player, "window_shadow", stalker.position(), source + "_fallback");
		return true;
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

	private static void tickRecentWorldChanges(ServerPlayer player, long gameTime) {
		resolvePendingWorldChanges(player, gameTime);

		List<RecentWorldChange> changes = RECENT_WORLD_CHANGES.get(player.getUUID());
		if (changes == null || changes.isEmpty()) {
			return;
		}

		Iterator<RecentWorldChange> iterator = changes.iterator();
		while (iterator.hasNext()) {
			RecentWorldChange change = iterator.next();
			if (gameTime - change.createdTick() > IMPOSSIBLE_RETURN_MAX_AGE) {
				iterator.remove();
				continue;
			}

			BlockState actual = player.level().getBlockState(change.pos());
			if (!actual.equals(change.actualState())) {
				if (actual.equals(change.rememberedState())) {
					iterator.remove();
					continue;
				}
				change.updateActualState(actual);
			}

			double distanceSq = centerOf(change.pos()).distanceToSqr(player.position());
			if (distanceSq >= IMPOSSIBLE_RETURN_DEPART_DISTANCE * IMPOSSIBLE_RETURN_DEPART_DISTANCE) {
				change.markDeparted();
			}
		}

		if (changes.isEmpty()) {
			RECENT_WORLD_CHANGES.remove(player.getUUID());
		}
	}

	private static void resolvePendingWorldChanges(ServerPlayer player, long gameTime) {
		List<PendingWorldChange> pendingChanges = PENDING_WORLD_CHANGES.get(player.getUUID());
		if (pendingChanges == null || pendingChanges.isEmpty()) {
			return;
		}

		Iterator<PendingWorldChange> iterator = pendingChanges.iterator();
		while (iterator.hasNext()) {
			PendingWorldChange pending = iterator.next();
			if (gameTime < pending.resolveTick() || !player.level().hasChunkAt(pending.pos())) {
				continue;
			}

			BlockState actual = player.level().getBlockState(pending.pos());
			if (!actual.equals(pending.previousState())) {
				recordWorldChange(player, pending.pos(), pending.previousState(), actual, gameTime);
			}
			iterator.remove();
		}

		if (pendingChanges.isEmpty()) {
			PENDING_WORLD_CHANGES.remove(player.getUUID());
		}
	}

	private static void recordWorldChange(ServerPlayer player, BlockPos pos, BlockState rememberedState, BlockState actualState, long gameTime) {
		if (rememberedState.equals(actualState) || !player.level().hasChunkAt(pos)) {
			return;
		}

		List<RecentWorldChange> changes = RECENT_WORLD_CHANGES.computeIfAbsent(player.getUUID(), ignored -> new ArrayList<>());
		changes.removeIf(change -> change.pos().equals(pos));
		changes.add(0, new RecentWorldChange(pos.immutable(), rememberedState, actualState, gameTime));
		while (changes.size() > MAX_RECENT_CHANGES) {
			changes.remove(changes.size() - 1);
		}
	}

	private static List<RecentWorldChange> findImpossibleReturnCluster(ServerPlayer player, long gameTime, RandomSource random) {
		List<RecentWorldChange> changes = RECENT_WORLD_CHANGES.get(player.getUUID());
		if (changes == null || changes.isEmpty()) {
			return List.of();
		}

		List<RecentWorldChange> eligible = new ArrayList<>();
		double revisitDistanceSq = IMPOSSIBLE_RETURN_REVISIT_DISTANCE * IMPOSSIBLE_RETURN_REVISIT_DISTANCE;
		for (RecentWorldChange change : changes) {
			if (!change.departed()
					|| gameTime - change.createdTick() < IMPOSSIBLE_RETURN_MIN_AGE
					|| gameTime - change.lastReplayTick() < IMPOSSIBLE_RETURN_REPLAY_COOLDOWN
					|| !player.level().getBlockState(change.pos()).equals(change.actualState())) {
				continue;
			}

			if (centerOf(change.pos()).distanceToSqr(player.position()) <= revisitDistanceSq) {
				eligible.add(change);
			}
		}

		if (eligible.isEmpty()) {
			return List.of();
		}

		RecentWorldChange anchor = eligible.get(random.nextInt(eligible.size()));
		List<RecentWorldChange> cluster = new ArrayList<>();
		for (RecentWorldChange change : eligible) {
			if (change.pos().distManhattan(anchor.pos()) <= IMPOSSIBLE_RETURN_GROUP_RADIUS) {
				cluster.add(change);
			}
			if (cluster.size() >= 6) {
				break;
			}
		}
		return cluster;
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

	private static Vec3 centerOf(BlockPos pos) {
		return new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
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

	private record PendingWorldChange(BlockPos pos, BlockState previousState, long resolveTick) {
	}

	private static final class RecentWorldChange {
		private final BlockPos pos;
		private final BlockState rememberedState;
		private BlockState actualState;
		private final long createdTick;
		private boolean departed;
		private long lastReplayTick = Long.MIN_VALUE / 4;

		private RecentWorldChange(BlockPos pos, BlockState rememberedState, BlockState actualState, long createdTick) {
			this.pos = pos;
			this.rememberedState = rememberedState;
			this.actualState = actualState;
			this.createdTick = createdTick;
		}

		private BlockPos pos() {
			return pos;
		}

		private BlockState rememberedState() {
			return rememberedState;
		}

		private BlockState actualState() {
			return actualState;
		}

		private long createdTick() {
			return createdTick;
		}

		private boolean departed() {
			return departed;
		}

		private long lastReplayTick() {
			return lastReplayTick;
		}

		private void updateActualState(BlockState newActualState) {
			this.actualState = newActualState;
		}

		private void markDeparted() {
			this.departed = true;
		}

		private void markReplayed(long gameTime) {
			this.lastReplayTick = gameTime;
		}
	}
}
