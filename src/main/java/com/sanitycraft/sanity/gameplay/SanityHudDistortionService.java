package com.sanitycraft.sanity.gameplay;

import com.sanitycraft.data.config.SanityCraftConfig;
import com.sanitycraft.network.sync.ClientEffectSyncService;
import com.sanitycraft.sanity.SanityComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

public final class SanityHudDistortionService {
	private static final int HUD_COOLDOWN = 20 * 9;

	private SanityHudDistortionService() {
	}

	public static void tickPlayer(ServerPlayer player, SanityComponent component) {
		if (component.getSanity() >= 10 || !component.isCooldownReady(SanityComponent.Cooldown.HUD_DISTORTION)) {
			return;
		}

		RandomSource random = player.getRandom();
		float chance = component.getSanity() < 5 ? 0.060F : 0.035F;
		if (!SanityGameplayUtil.roll(random, chance)) {
			return;
		}
		if (!component.tryConsumeEffectBudget(SanityCraftConfig.get(), 1)) {
			return;
		}

		int duration = component.getSanity() < 5 ? 48 + random.nextInt(30) : 28 + random.nextInt(22);
		int intensity = component.getSanity() < 5 ? 4 + random.nextInt(2) : 2 + random.nextInt(2);
		boolean fakeDamage = component.getSanity() < 5 && random.nextFloat() < 0.55F;
		if (ClientEffectSyncService.sendHudDistortion(player, duration, intensity, fakeDamage, "low_sanity", false)) {
			component.setCooldown(SanityComponent.Cooldown.HUD_DISTORTION, SanityGameplayUtil.jitter(HUD_COOLDOWN, random, 20 * 5));
		}
	}

	public static void debugTrigger(ServerPlayer player) {
		ClientEffectSyncService.sendHudDistortion(player, 60, 5, true, "debug", true);
	}
}
