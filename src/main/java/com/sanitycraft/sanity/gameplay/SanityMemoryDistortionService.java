package com.sanitycraft.sanity.gameplay;

import com.sanitycraft.data.config.SanityCraftConfig;
import com.sanitycraft.network.sync.ClientEffectSyncService;
import com.sanitycraft.sanity.SanityComponent;
import com.sanitycraft.sanity.SanityThresholds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;

public final class SanityMemoryDistortionService {
	private static final String[] GENERIC_LINES = {
			"You were already here.",
			"You checked this room already.",
			"That sound happened earlier.",
			"You forgot what changed.",
			"You are repeating yourself."
	};
	private static final int MEMORY_COOLDOWN = 20 * 18;

	private SanityMemoryDistortionService() {
	}

	public static void tickPlayer(ServerPlayer player, SanityComponent component, SanityThresholds.Stage stage) {
		if (component.getSanity() >= 30 || !component.isCooldownReady(SanityComponent.Cooldown.MEMORY_DISTORTION)) {
			return;
		}

		RandomSource random = player.getRandom();
		float chance = stage == SanityThresholds.Stage.COLLAPSE ? 0.038F : 0.024F;
		if (!SanityGameplayUtil.roll(random, chance)) {
			return;
		}
		if (!component.tryConsumeEffectBudget(SanityCraftConfig.get(), 1)) {
			return;
		}

		ClientEffectSyncService.sendFalseFeedback(player, selectMessage(player, random));
		component.setCooldown(SanityComponent.Cooldown.MEMORY_DISTORTION, SanityGameplayUtil.jitter(MEMORY_COOLDOWN, random, 20 * 12));
	}

	private static String selectMessage(ServerPlayer player, RandomSource random) {
		List<String> contextual = new ArrayList<>();
		if (hasNearbyChest(player)) {
			contextual.add("You opened this chest earlier.");
		}
		long timeOfDay = player.level().getDayTime() % 24000L;
		if (!player.level().canSeeSky(player.blockPosition()) && timeOfDay >= 13000L && timeOfDay <= 23000L) {
			contextual.add("You meant to sleep before this.");
		}
		if (player.getFoodData().getFoodLevel() < 8) {
			contextual.add("You already stopped to eat.");
		}
		contextual.add(GENERIC_LINES[random.nextInt(GENERIC_LINES.length)]);
		return contextual.get(random.nextInt(contextual.size()));
	}

	private static boolean hasNearbyChest(ServerPlayer player) {
		BlockPos center = player.blockPosition();
		for (int x = -6; x <= 6; x++) {
			for (int y = -2; y <= 2; y++) {
				for (int z = -6; z <= 6; z++) {
					BlockPos pos = center.offset(x, y, z);
					if (!player.level().hasChunkAt(pos)) {
						continue;
					}
					if (player.level().getBlockState(pos).is(Blocks.CHEST)
							|| player.level().getBlockState(pos).is(Blocks.TRAPPED_CHEST)
							|| player.level().getBlockState(pos).is(Blocks.BARREL)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
