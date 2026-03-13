package com.sanitycraft.sanity;

import com.sanitycraft.data.config.SanityCraftConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class SanityAudioContextRules {
	private static final int HOUSE_SCAN_RADIUS = 8;
	private static final int HOUSE_SCAN_HEIGHT = 3;
	private static final int CHEST_SCAN_HEIGHT = 2;

	private SanityAudioContextRules() {
	}

	public static AudioDecision evaluate(ServerPlayer player, SanityAudioEvent event, RandomSource random, SanityCraftConfig config) {
		ServerLevel level = player.level();
		BlockPos origin = player.blockPosition();
		boolean overworld = level.dimension().equals(Level.OVERWORLD);
		boolean skyExposed = level.canSeeSky(origin);
		boolean night = isNight(level);
		boolean caveLike = !skyExposed && origin.getY() < level.getSeaLevel() + 2;
		boolean forestOutdoors = overworld && skyExposed && isForestBiome(level, origin);
		BlockAnchor houseAnchor = findNearestHouseAnchor(level, origin);
		BlockAnchor containerAnchor = event == SanityAudioEvent.CHEST_SOUND
				? findNearestContainer(level, origin, config.events.containerAudioRadius)
				: null;

		return switch (event) {
			case PHANTOM_SOUND -> {
				if (!overworld) {
					yield AudioDecision.rejected("not_overworld");
				}
				if (!night) {
					yield AudioDecision.rejected("daytime");
				}
				if (!skyExposed) {
					yield AudioDecision.rejected("not_sky_exposed");
				}
				yield AudioDecision.accepted(overheadSource(player, random), "night_sky_exposed", null);
			}
			case CAVE_SOUND -> {
				if (!caveLike) {
					yield AudioDecision.rejected(skyExposed ? "sky_exposed" : "not_underground");
				}
				yield AudioDecision.accepted(nearbySource(player, random, 4.0D, 8.0D, 0.7D), "underground_enclosed", null);
			}
			case HOUSE_SOUND -> {
				if (houseAnchor == null) {
					yield AudioDecision.rejected("no_structure_anchor");
				}
				yield AudioDecision.accepted(centerOf(houseAnchor.pos()), "structure_anchor=" + houseAnchor.label(), houseAnchor);
			}
			case FOREST_SOUND -> {
				if (!forestOutdoors) {
					yield AudioDecision.rejected("not_forest_outdoors");
				}
				yield AudioDecision.accepted(outdoorSource(player, random, 6.0D, 12.0D), "forest_outdoors", null);
			}
			case CHEST_SOUND -> {
				if (containerAnchor == null) {
					yield AudioDecision.rejected("no_nearby_container");
				}
				yield AudioDecision.accepted(centerOf(containerAnchor.pos()), "container=" + containerAnchor.label(), containerAnchor);
			}
			case BREATHING -> AudioDecision.accepted(closeBehindSource(player, random), "close_personal", null);
		};
	}

	private static boolean isNight(ServerLevel level) {
		long timeOfDay = level.getDayTime() % 24000L;
		return timeOfDay >= 13000L && timeOfDay <= 23000L;
	}

	private static boolean isForestBiome(ServerLevel level, BlockPos pos) {
		return level.getBiome(pos).is(BiomeTags.IS_FOREST)
				|| level.getBiome(pos).is(BiomeTags.IS_TAIGA)
				|| level.getBiome(pos).is(BiomeTags.IS_JUNGLE);
	}

	private static BlockAnchor findNearestContainer(ServerLevel level, BlockPos origin, int radius) {
		int safeRadius = Math.max(4, radius);
		BlockAnchor best = null;
		double bestDistance = Double.MAX_VALUE;
		for (int x = -safeRadius; x <= safeRadius; x++) {
			for (int y = -CHEST_SCAN_HEIGHT; y <= CHEST_SCAN_HEIGHT; y++) {
				for (int z = -safeRadius; z <= safeRadius; z++) {
					BlockPos pos = origin.offset(x, y, z);
					if (!level.hasChunkAt(pos)) {
						continue;
					}
					BlockState state = level.getBlockState(pos);
					if (!isContainerBlock(state)) {
						continue;
					}
					double distance = origin.distSqr(pos);
					if (distance >= bestDistance) {
						continue;
					}
					bestDistance = distance;
					best = new BlockAnchor(pos.immutable(), state, blockLabel(state));
				}
			}
		}
		return best;
	}

	private static BlockAnchor findNearestHouseAnchor(ServerLevel level, BlockPos origin) {
		BlockAnchor best = null;
		double bestDistance = Double.MAX_VALUE;
		for (int x = -HOUSE_SCAN_RADIUS; x <= HOUSE_SCAN_RADIUS; x++) {
			for (int y = -HOUSE_SCAN_HEIGHT; y <= HOUSE_SCAN_HEIGHT; y++) {
				for (int z = -HOUSE_SCAN_RADIUS; z <= HOUSE_SCAN_RADIUS; z++) {
					BlockPos pos = origin.offset(x, y, z);
					if (!level.hasChunkAt(pos)) {
						continue;
					}
					BlockState state = level.getBlockState(pos);
					if (!isHouseAnchor(state)) {
						continue;
					}
					double distance = origin.distSqr(pos);
					if (distance >= bestDistance) {
						continue;
					}
					bestDistance = distance;
					best = new BlockAnchor(pos.immutable(), state, blockLabel(state));
				}
			}
		}
		return best;
	}

	private static boolean isContainerBlock(BlockState state) {
		return state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST) || state.is(Blocks.BARREL);
	}

	private static boolean isHouseAnchor(BlockState state) {
		return state.is(BlockTags.WOODEN_DOORS)
				|| state.is(BlockTags.WOODEN_TRAPDOORS)
				|| state.is(BlockTags.PLANKS)
				|| state.is(BlockTags.LOGS_THAT_BURN)
				|| state.is(BlockTags.WOODEN_STAIRS)
				|| state.is(BlockTags.WOODEN_SLABS)
				|| state.is(BlockTags.WOODEN_FENCES)
				|| state.is(BlockTags.FENCE_GATES)
				|| state.is(Blocks.CHEST)
				|| state.is(Blocks.TRAPPED_CHEST)
				|| state.is(Blocks.BARREL)
				|| state.is(Blocks.CRAFTING_TABLE)
				|| state.is(Blocks.FURNACE)
				|| state.is(Blocks.BLAST_FURNACE)
				|| state.is(Blocks.SMOKER)
				|| state.is(Blocks.GLASS)
				|| state.is(Blocks.GLASS_PANE)
				|| state.is(Blocks.TINTED_GLASS)
				|| state.is(Blocks.BLACK_STAINED_GLASS)
				|| state.is(Blocks.BLACK_STAINED_GLASS_PANE)
				|| state.is(Blocks.LANTERN)
				|| state.is(Blocks.CHAIN)
				|| state.is(Blocks.RED_BED)
				|| state.is(Blocks.WHITE_BED)
				|| state.is(Blocks.BLACK_BED);
	}

	private static String blockLabel(BlockState state) {
		return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
	}

	private static Vec3 centerOf(BlockPos pos) {
		return new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
	}

	private static Vec3 overheadSource(ServerPlayer player, RandomSource random) {
		double angle = random.nextDouble() * Math.PI * 2.0D;
		double distance = 7.0D + random.nextDouble() * 5.0D;
		double x = player.getX() + Math.cos(angle) * distance;
		double z = player.getZ() + Math.sin(angle) * distance;
		double y = player.getY() + 5.5D + random.nextDouble() * 3.0D;
		return new Vec3(x, y, z);
	}

	private static Vec3 nearbySource(ServerPlayer player, RandomSource random, double minDistance, double maxDistance, double verticalRange) {
		double angle = random.nextDouble() * Math.PI * 2.0D;
		double distance = minDistance + random.nextDouble() * Math.max(0.5D, maxDistance - minDistance);
		double x = player.getX() + Math.cos(angle) * distance;
		double z = player.getZ() + Math.sin(angle) * distance;
		double y = player.getY() + (random.nextDouble() - 0.5D) * verticalRange;
		return new Vec3(x, y, z);
	}

	private static Vec3 outdoorSource(ServerPlayer player, RandomSource random, double minDistance, double maxDistance) {
		double angle = random.nextDouble() * Math.PI * 2.0D;
		double distance = minDistance + random.nextDouble() * Math.max(0.5D, maxDistance - minDistance);
		double x = player.getX() + Math.cos(angle) * distance;
		double z = player.getZ() + Math.sin(angle) * distance;
		return new Vec3(x, player.getY() + 0.2D + random.nextDouble() * 0.6D, z);
	}

	private static Vec3 closeBehindSource(ServerPlayer player, RandomSource random) {
		Vec3 look = player.getLookAngle();
		Vec3 forward = new Vec3(look.x, 0.0D, look.z);
		if (forward.lengthSqr() < 1.0E-4D) {
			forward = new Vec3(0.0D, 0.0D, 1.0D);
		} else {
			forward = forward.normalize();
		}
		Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
		double distance = 1.3D + random.nextDouble() * 0.9D;
		double sideOffset = (random.nextDouble() - 0.5D) * 0.7D;
		return player.position()
				.subtract(forward.scale(distance))
				.add(right.scale(sideOffset))
				.add(0.0D, 0.16D + random.nextDouble() * 0.45D, 0.0D);
	}

	public record AudioDecision(boolean accepted, Vec3 sourcePos, String reason, BlockAnchor anchor) {
		public static AudioDecision accepted(Vec3 sourcePos, String reason, BlockAnchor anchor) {
			return new AudioDecision(true, sourcePos, reason, anchor);
		}

		public static AudioDecision rejected(String reason) {
			return new AudioDecision(false, Vec3.ZERO, reason, null);
		}
	}

	public record BlockAnchor(BlockPos pos, BlockState state, String label) {
	}
}
