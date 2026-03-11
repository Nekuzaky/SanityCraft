package net.nekuzaky.sanitycraft.sanity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class SanityManager {
	private static final Map<UUID, PlayerSanityComponent> PLAYER_SANITY = new ConcurrentHashMap<>();
	private static SanityConfig config = new SanityConfig();

	private SanityManager() {
	}

	public static void initialize() {
		config = SanityConfig.loadOrCreate();
	}

	public static SanityConfig getConfig() {
		return config;
	}

	public static PlayerSanityComponent get(ServerPlayer player) {
		return PLAYER_SANITY.computeIfAbsent(player.getUUID(), uuid -> new PlayerSanityComponent());
	}

	public static void remove(ServerPlayer player) {
		PLAYER_SANITY.remove(player.getUUID());
	}

	public static void copy(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
		PlayerSanityComponent source = get(oldPlayer);
		PlayerSanityComponent target = get(newPlayer);
		target.setSanity(source.getSanity());
	}

	public static void tick(ServerPlayer player) {
		PlayerSanityComponent component = get(player);
		component.tickCooldowns();
		if (!component.shouldUpdate(config.getUpdateIntervalTicks())) {
			return;
		}

		int before = component.getSanity();
		int delta = computeDelta(player, config);
		component.addSanity(delta);
		SanityEffects.apply(player, component, config);
		if (component.getSanity() != before) {
			SanityNetworking.sync(player, component.getSanity());
		}
	}

	private static int computeDelta(ServerPlayer player, SanityConfig config) {
		ServerLevel level = player.level();
		BlockPos pos = player.blockPosition();

		int delta = 0;
		if (isDark(level, pos)) {
			delta -= config.darknessLoss;
		}
		if (isCave(level, pos)) {
			delta -= config.caveLoss;
		}
		if (isNearHostile(player, config.hostileRadius)) {
			delta -= config.hostileLoss;
		}
		if (level.isThundering()) {
			delta -= config.thunderLoss;
		}
		if (isDeepDark(level, pos)) {
			delta -= config.deepDarkLoss;
		}

		if (player.isSleeping()) {
			delta += config.sleepGain;
		}
		if (isNearVillage(player, config.villageRadius)) {
			delta += config.villageGain;
		}
		if (isNearLight(level, pos, config.lightRadius)) {
			delta += config.lightGain;
		}
		if (isNearMusic(level, pos, config.musicRadius)) {
			delta += config.musicGain;
		}

		return delta;
	}

	private static boolean isDark(ServerLevel level, BlockPos pos) {
		return level.getBrightness(LightLayer.BLOCK, pos) <= 7 && level.getBrightness(LightLayer.SKY, pos) <= 7;
	}

	private static boolean isCave(ServerLevel level, BlockPos pos) {
		return !level.canSeeSky(pos) && pos.getY() < level.getSeaLevel() - 5;
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
}
