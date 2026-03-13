package com.sanitycraft.sanity;

import com.sanitycraft.data.config.SanityCraftConfig;
import net.minecraft.server.level.ServerPlayer;

public final class SanityEffectService {
	private SanityEffectService() {
	}

	public static void tickPlayer(ServerPlayer player, SanityComponent component, SanityCraftConfig config, long gameTime) {
		if (!component.shouldRunEffectTick(gameTime, config.ticking.effectIntervalTicks)) {
			return;
		}

		SanityThresholds.Stage stage = SanityThresholds.resolve(component.getSanity(), config);
		if (component.hasHallucinationShield()) {
			SanityHallucinationService.dispelOwnedHallucinations(player);
			SanityPsychologicalService.clearPlayer(player);
			return;
		}
		if (stage == SanityThresholds.Stage.STABLE) {
			SanityHallucinationService.dispelOwnedHallucinations(player);
			SanityPsychologicalService.clearPlayer(player);
			return;
		}

		SanityAudioDirector.tickPlayer(player, component, stage, config);
		SanityHallucinationService.tickPlayer(player, component, stage, config);
		SanityPsychologicalService.tickPlayer(player, component, stage, gameTime);
		SanityCollapseService.tickPlayer(player, component, stage, config);
	}
}
