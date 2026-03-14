package com.sanitycraft.sanity.gameplay;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

final class SanityGameplayUtil {
	private SanityGameplayUtil() {
	}

	static boolean roll(RandomSource random, float chance) {
		return random.nextFloat() < Mth.clamp(chance, 0.0F, 1.0F);
	}

	static int jitter(int baseTicks, RandomSource random, int minimumTicks) {
		int safeBase = Math.max(1, baseTicks);
		int variance = Math.max(1, safeBase / 4);
		return Math.max(minimumTicks, safeBase - variance + random.nextInt(variance * 2 + 1));
	}

	static BlockPos blockBehindPlayer(ServerPlayer player, int distance) {
		Vec3 backward = player.getLookAngle().scale(-distance);
		return BlockPos.containing(player.getX() + backward.x, player.getY(), player.getZ() + backward.z);
	}

	static BlockPos blockAheadOfPlayer(ServerPlayer player, int distance) {
		Vec3 forward = player.getLookAngle().scale(distance);
		return BlockPos.containing(player.getX() + forward.x, player.getY(), player.getZ() + forward.z);
	}

	static Vec3 centerOf(BlockPos pos) {
		return new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
	}

	static boolean isPlayerFacing(ServerPlayer player, Vec3 target, double threshold) {
		Vec3 look = player.getLookAngle().normalize();
		Vec3 toTarget = target.subtract(player.getEyePosition()).normalize();
		return look.dot(toTarget) >= threshold;
	}

	static boolean isDimOrCovered(ServerLevel level, BlockPos pos) {
		return level.getMaxLocalRawBrightness(pos) <= 8 || !level.canSeeSky(pos.above());
	}

	static boolean hasNearbyCover(ServerLevel level, BlockPos pos) {
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

	static Vec3 findStandablePosition(ServerLevel level, BlockPos base) {
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

	static Vec3 findRingSpawn(
			ServerPlayer player,
			RandomSource random,
			double minDistance,
			double maxDistance,
			int attempts,
			Predicate<BlockPos> predicate) {
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
}
