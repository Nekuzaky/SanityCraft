package com.sanitycraft.sanity.gameplay;

import com.sanitycraft.data.config.SanityCraftConfig;
import com.sanitycraft.entity.observer.ObserverEntity;
import com.sanitycraft.sanity.SanityComponent;
import com.sanitycraft.sanity.SanityThresholds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

public final class SanityDoppelgangerService {
	private static final int DOPPELGANGER_COOLDOWN = 20 * 34;

	private SanityDoppelgangerService() {
	}

	public static void tickPlayer(ServerPlayer player, SanityComponent component, SanityThresholds.Stage stage) {
		if (component.getSanity() >= 20) {
			SanityObserverService.clearMode(player, ObserverEntity.HallucinationMode.DOPPELGANGER);
			return;
		}
		if (!component.isCooldownReady(SanityComponent.Cooldown.DOPPELGANGER)) {
			return;
		}

		RandomSource random = player.getRandom();
		float chance = stage == SanityThresholds.Stage.COLLAPSE ? 0.010F : 0.006F;
		if (!SanityGameplayUtil.roll(random, chance)) {
			return;
		}
		if (!component.tryConsumeEffectBudget(SanityCraftConfig.get(), 2)) {
			return;
		}
		if (SanityObserverService.spawnHallucination(player, ObserverEntity.HallucinationMode.DOPPELGANGER, false, "runtime")) {
			component.setCooldown(SanityComponent.Cooldown.DOPPELGANGER, SanityGameplayUtil.jitter(DOPPELGANGER_COOLDOWN, random, 20 * 20));
		}
	}

	public static String debugTrigger(ServerPlayer player) {
		return SanityObserverService.spawnHallucination(player, ObserverEntity.HallucinationMode.DOPPELGANGER, true, "debug")
				? "Triggered doppelganger."
				: "Could not place a doppelganger here.";
	}
}
