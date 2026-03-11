package net.nekuzaky.sanitycraft.sanity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.nekuzaky.sanitycraft.init.SanitycraftModItems;

public class SanityEnvironmentHelper {
	private SanityEnvironmentHelper() {
	}

	public static SanityEnvironmentSnapshot snapshot(ServerPlayer player, SanityConfig config) {
		ServerLevel level = player.level();
		BlockPos pos = player.blockPosition();
		return new SanityEnvironmentSnapshot(
				isDark(level, pos),
				isCave(level, pos),
				isUnderground(level, pos),
				isNearHostile(player, config.hostileRadius),
				isNight(level),
				level.isRainingAt(pos.above()),
				level.isThundering(),
				isDeepDark(level, pos),
				player.isSleeping(),
				isNearVillage(player, config.villageRadius),
				isNearLight(level, pos, config.lightRadius),
				isNearMusic(level, pos, config.musicRadius),
				isInRitualSafeZone(player, level, pos, config),
				isNearAnomalyStructure(level, pos));
	}

	private static boolean isDark(ServerLevel level, BlockPos pos) {
		return level.getBrightness(LightLayer.BLOCK, pos) <= 7 && level.getBrightness(LightLayer.SKY, pos) <= 7;
	}

	private static boolean isCave(ServerLevel level, BlockPos pos) {
		return !level.canSeeSky(pos) && pos.getY() < level.getSeaLevel() - 5;
	}

	private static boolean isUnderground(ServerLevel level, BlockPos pos) {
		return !level.canSeeSky(pos) && pos.getY() < level.getSeaLevel() + 2;
	}

	private static boolean isNight(ServerLevel level) {
		long timeOfDay = level.getDayTime() % 24000L;
		return timeOfDay >= 13000L && timeOfDay <= 23000L;
	}

	private static boolean isDeepDark(ServerLevel level, BlockPos pos) {
		return level.getBiome(pos).is(BiomeTags.HAS_ANCIENT_CITY) || (!level.canSeeSky(pos) && pos.getY() < -20);
	}

	private static boolean isNearHostile(ServerPlayer player, int radius) {
		AABB area = player.getBoundingBox().inflate(Math.max(1, radius));
		return !player.level().getEntitiesOfClass(Monster.class, area, monster -> monster.isAlive()).isEmpty();
	}

	private static boolean isNearVillage(ServerPlayer player, int radius) {
		AABB area = player.getBoundingBox().inflate(Math.max(1, radius));
		return !player.level().getEntitiesOfClass(Villager.class, area, villager -> villager.isAlive()).isEmpty();
	}

	private static boolean isNearLight(ServerLevel level, BlockPos center, int radius) {
		int safeRadius = Math.max(1, radius);
		for (int x = -safeRadius; x <= safeRadius; x++) {
			for (int y = -safeRadius; y <= safeRadius; y++) {
				for (int z = -safeRadius; z <= safeRadius; z++) {
					BlockPos check = center.offset(x, y, z);
					BlockState state = level.getBlockState(check);
					if (state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH) || state.is(Blocks.SOUL_TORCH) || state.is(Blocks.SOUL_WALL_TORCH)) {
						return true;
					}
					if (level.getBrightness(LightLayer.BLOCK, check) >= 11) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean isNearMusic(ServerLevel level, BlockPos center, int radius) {
		int safeRadius = Math.max(1, radius);
		for (int x = -safeRadius; x <= safeRadius; x++) {
			for (int y = -safeRadius; y <= safeRadius; y++) {
				for (int z = -safeRadius; z <= safeRadius; z++) {
					BlockPos check = center.offset(x, y, z);
					BlockState state = level.getBlockState(check);
					if (state.is(Blocks.JUKEBOX) && state.hasProperty(JukeboxBlock.HAS_RECORD) && state.getValue(JukeboxBlock.HAS_RECORD)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean isInRitualSafeZone(ServerPlayer player, ServerLevel level, BlockPos pos, SanityConfig config) {
		if (!config.ritualSafeZoneEnabled) {
			return false;
		}
		if (!isHoldingPill(player.getMainHandItem()) && !isHoldingPill(player.getOffhandItem())) {
			return false;
		}

		int y = pos.getY();
		BlockPos[] ring = new BlockPos[] {
				new BlockPos(pos.getX() + 2, y, pos.getZ()),
				new BlockPos(pos.getX() - 2, y, pos.getZ()),
				new BlockPos(pos.getX(), y, pos.getZ() + 2),
				new BlockPos(pos.getX(), y, pos.getZ() - 2),
				new BlockPos(pos.getX() + 2, y, pos.getZ() + 2),
				new BlockPos(pos.getX() - 2, y, pos.getZ() - 2),
				new BlockPos(pos.getX() + 2, y, pos.getZ() - 2),
				new BlockPos(pos.getX() - 2, y, pos.getZ() + 2)
		};

		int torchCount = 0;
		for (BlockPos check : ring) {
			BlockState state = level.getBlockState(check);
			if (isTorchLike(state)) {
				torchCount++;
				continue;
			}
			BlockState up = level.getBlockState(check.above());
			if (isTorchLike(up)) {
				torchCount++;
			}
		}
		return torchCount >= 6;
	}

	private static boolean isHoldingPill(ItemStack stack) {
		return stack != null && !stack.isEmpty() && stack.getItem() == SanitycraftModItems.PILL;
	}

	private static boolean isTorchLike(BlockState state) {
		return state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH) || state.is(Blocks.SOUL_TORCH) || state.is(Blocks.SOUL_WALL_TORCH) || state.is(Blocks.REDSTONE_TORCH)
				|| state.is(Blocks.REDSTONE_WALL_TORCH);
	}

	private static boolean isNearAnomalyStructure(ServerLevel level, BlockPos center) {
		for (int x = -6; x <= 6; x++) {
			for (int y = -3; y <= 3; y++) {
				for (int z = -6; z <= 6; z++) {
					BlockPos base = center.offset(x, y, z);
					BlockState state = level.getBlockState(base);
					if (!state.is(Blocks.CRYING_OBSIDIAN)) {
						continue;
					}
					BlockState above = level.getBlockState(base.above());
					if (above.is(Blocks.SOUL_FIRE) || above.is(Blocks.SOUL_LANTERN) || above.is(Blocks.RED_CANDLE) || above.is(Blocks.CANDLE)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
