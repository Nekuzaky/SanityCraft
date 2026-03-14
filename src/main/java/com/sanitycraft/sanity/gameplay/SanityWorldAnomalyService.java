package com.sanitycraft.sanity.gameplay;

import com.sanitycraft.data.config.SanityCraftConfig;
import com.sanitycraft.sanity.SanityComponent;
import com.sanitycraft.sanity.SanityDebug;
import com.sanitycraft.sanity.SanityThresholds;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class SanityWorldAnomalyService {
	private static final Map<UUID, List<WorldIllusion>> ACTIVE_ILLUSIONS = new ConcurrentHashMap<>();
	private static final int ANOMALY_COOLDOWN = 20 * 18;
	private static final int SHORT_DURATION = 20 * 4;
	private static final int STRUCTURE_DURATION = 20 * 6;

	private SanityWorldAnomalyService() {
	}

	public static void tickPlayer(ServerPlayer player, SanityComponent component, SanityThresholds.Stage stage, long gameTime) {
		expireIllusions(player, gameTime);
		if (component.getSanity() >= 50) {
			clearPlayer(player);
			return;
		}
		if (!component.isCooldownReady(SanityComponent.Cooldown.WORLD_ANOMALY)) {
			return;
		}

		RandomSource random = player.getRandom();
		float chance = switch (stage) {
			case DISTURBED -> 0.010F;
			case FRACTURED -> 0.014F;
			case COLLAPSE -> 0.018F;
			default -> 0.0F;
		};
		if (!SanityGameplayUtil.roll(random, chance)) {
			return;
		}
		if (!component.tryConsumeEffectBudget(SanityCraftConfig.get(), 1)) {
			return;
		}

		String result = triggerRandomAnomaly(player, gameTime, random, false);
		if (result != null) {
			component.setCooldown(SanityComponent.Cooldown.WORLD_ANOMALY, SanityGameplayUtil.jitter(ANOMALY_COOLDOWN, random, 20 * 12));
			SanityDebug.logEvent(player, "world_anomaly type=" + result);
		}
	}

	public static String debugTrigger(ServerPlayer player) {
		String result = triggerRandomAnomaly(player, player.level().getGameTime(), player.getRandom(), true);
		return result == null ? "No anomaly could be placed here." : "Triggered anomaly: " + result;
	}

	public static void clearPlayer(ServerPlayer player) {
		List<WorldIllusion> illusions = ACTIVE_ILLUSIONS.remove(player.getUUID());
		if (illusions == null) {
			return;
		}
		for (WorldIllusion illusion : illusions) {
			revertIllusion(player, illusion.pos());
		}
	}

	public static void clearAll() {
		ACTIVE_ILLUSIONS.clear();
	}

	private static String triggerRandomAnomaly(ServerPlayer player, long gameTime, RandomSource random, boolean forced) {
		int start = random.nextInt(4);
		for (int offset = 0; offset < 4; offset++) {
			int index = (start + offset) % 4;
			String result = switch (index) {
				case 0 -> triggerTorchOutage(player, gameTime, random);
				case 1 -> triggerDoorClose(player, gameTime, random);
				case 2 -> triggerBranchShift(player, gameTime, random);
				default -> triggerDistantMarker(player, gameTime, random, forced);
			};
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	private static String triggerTorchOutage(ServerPlayer player, long gameTime, RandomSource random) {
		List<BlockPos> candidates = new ArrayList<>();
		BlockPos center = player.blockPosition();
		for (int x = -12; x <= 12; x++) {
			for (int y = -4; y <= 5; y++) {
				for (int z = -12; z <= 12; z++) {
					BlockPos pos = center.offset(x, y, z);
					if (!player.level().hasChunkAt(pos)) {
						continue;
					}
					BlockState state = player.level().getBlockState(pos);
					if (!isLightSourceCandidate(state)) {
						continue;
					}
					if (SanityGameplayUtil.isPlayerFacing(player, SanityGameplayUtil.centerOf(pos), 0.982D)) {
						continue;
					}
					candidates.add(pos.immutable());
				}
			}
		}
		if (candidates.isEmpty()) {
			return null;
		}

		BlockPos target = candidates.get(random.nextInt(candidates.size()));
		BlockState current = player.level().getBlockState(target);
		BlockState fake = extinguishedVariant(current);
		return sendFalseBlock(player, target, fake, gameTime + SHORT_DURATION + random.nextInt(30)) ? "torch_outage" : null;
	}

	private static String triggerDoorClose(ServerPlayer player, long gameTime, RandomSource random) {
		List<BlockPos> candidates = new ArrayList<>();
		BlockPos center = player.blockPosition();
		var look = player.getLookAngle().normalize();
		for (int x = -8; x <= 8; x++) {
			for (int y = -3; y <= 3; y++) {
				for (int z = -8; z <= 8; z++) {
					BlockPos pos = center.offset(x, y, z);
					if (!player.level().hasChunkAt(pos)) {
						continue;
					}
					BlockState state = player.level().getBlockState(pos);
					if (!state.hasProperty(BlockStateProperties.OPEN) || !Boolean.TRUE.equals(state.getValue(BlockStateProperties.OPEN))) {
						continue;
					}
					var toDoor = SanityGameplayUtil.centerOf(pos).subtract(player.position()).normalize();
					if (look.dot(toDoor) > -0.10D) {
						continue;
					}
					candidates.add(pos.immutable());
				}
			}
		}
		if (candidates.isEmpty()) {
			return null;
		}

		BlockPos target = candidates.get(random.nextInt(candidates.size()));
		BlockState current = player.level().getBlockState(target);
		BlockState fake = current.setValue(BlockStateProperties.OPEN, false);
		return sendFalseBlock(player, target, fake, gameTime + SHORT_DURATION + random.nextInt(20)) ? "door_closed" : null;
	}

	private static String triggerBranchShift(ServerPlayer player, long gameTime, RandomSource random) {
		BlockPos center = player.blockPosition();
		List<BlockPos> leaves = new ArrayList<>();
		List<BlockPos> logs = new ArrayList<>();
		for (int x = -14; x <= 14; x++) {
			for (int y = -5; y <= 8; y++) {
				for (int z = -14; z <= 14; z++) {
					BlockPos pos = center.offset(x, y, z);
					if (!player.level().hasChunkAt(pos) || SanityGameplayUtil.isPlayerFacing(player, SanityGameplayUtil.centerOf(pos), 0.984D)) {
						continue;
					}
					BlockState state = player.level().getBlockState(pos);
					if (state.is(BlockTags.LEAVES)) {
						leaves.add(pos.immutable());
					} else if (state.is(BlockTags.LOGS)) {
						logs.add(pos.immutable());
					}
				}
			}
		}

		if (!leaves.isEmpty() && random.nextBoolean()) {
			BlockPos target = leaves.get(random.nextInt(leaves.size()));
			return sendFalseBlock(player, target, Blocks.AIR.defaultBlockState(), gameTime + SHORT_DURATION + random.nextInt(20)) ? "branch_missing" : null;
		}
		if (logs.isEmpty()) {
			return null;
		}

		BlockPos base = logs.get(random.nextInt(logs.size()));
		for (BlockPos offset : new BlockPos[] { base.north(), base.south(), base.east(), base.west(), base.above() }) {
			if (!player.level().getBlockState(offset).isAir()) {
				continue;
			}
			if (sendFalseBlock(player, offset, Blocks.OAK_LEAVES.defaultBlockState(), gameTime + SHORT_DURATION + random.nextInt(30))) {
				return "branch_shift";
			}
		}
		return null;
	}

	private static String triggerDistantMarker(ServerPlayer player, long gameTime, RandomSource random, boolean forced) {
		var spawn = SanityGameplayUtil.findRingSpawn(
				player,
				random,
				forced ? 16.0D : 20.0D,
				forced ? 28.0D : 34.0D,
				12,
				pos -> !SanityGameplayUtil.isPlayerFacing(player, SanityGameplayUtil.centerOf(pos), 0.975D));
		if (spawn == null) {
			return null;
		}

		BlockPos anchor = BlockPos.containing(spawn);
		boolean sent = false;
		sent |= sendFalseBlock(player, anchor, Blocks.COBBLED_DEEPSLATE.defaultBlockState(), gameTime + STRUCTURE_DURATION);
		sent |= sendFalseBlock(player, anchor.above(), Blocks.COBBLED_DEEPSLATE.defaultBlockState(), gameTime + STRUCTURE_DURATION);
		sent |= sendFalseBlock(player, anchor.above(2), Blocks.COBBLED_DEEPSLATE.defaultBlockState(), gameTime + STRUCTURE_DURATION);
		if (random.nextBoolean()) {
			sent |= sendFalseBlock(player, anchor.offset(1, 1, 0), Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState(), gameTime + STRUCTURE_DURATION);
		} else {
			sent |= sendFalseBlock(player, anchor.offset(0, 1, 1), Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState(), gameTime + STRUCTURE_DURATION);
		}
		return sent ? "distant_marker" : null;
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

	private static void expireIllusions(ServerPlayer player, long gameTime) {
		List<WorldIllusion> illusions = ACTIVE_ILLUSIONS.get(player.getUUID());
		if (illusions == null) {
			return;
		}
		Iterator<WorldIllusion> iterator = illusions.iterator();
		while (iterator.hasNext()) {
			WorldIllusion illusion = iterator.next();
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
		ACTIVE_ILLUSIONS.computeIfAbsent(player.getUUID(), ignored -> new ArrayList<>()).add(new WorldIllusion(pos.immutable(), expireTick));
		return true;
	}

	private static void revertIllusion(ServerPlayer player, BlockPos pos) {
		if (!player.level().hasChunkAt(pos)) {
			return;
		}
		player.connection.send(new ClientboundBlockUpdatePacket(pos, player.level().getBlockState(pos)));
	}

	private record WorldIllusion(BlockPos pos, long expireTick) {
	}
}
