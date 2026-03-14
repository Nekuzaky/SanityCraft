package com.sanitycraft.sanity;

import com.sanitycraft.data.config.SanityCraftConfig;
import com.sanitycraft.sanity.gameplay.SanityDoppelgangerService;
import com.sanitycraft.sanity.gameplay.SanityFalseEventService;
import com.sanitycraft.sanity.gameplay.SanityHudDistortionService;
import com.sanitycraft.sanity.gameplay.SanityMemoryDistortionService;
import com.sanitycraft.sanity.gameplay.SanityObserverService;
import com.sanitycraft.sanity.gameplay.SanityWorldAnomalyService;
import com.sanitycraft.sanity.gameplay.SanityWorldWatcherService;
import com.sanitycraft.sanity.events.SanityEventManager;
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
			SanityObserverService.clearPlayer(player);
			SanityWorldAnomalyService.clearPlayer(player);
			SanityPsychologicalService.clearPlayer(player);
			SanitySignatureEventService.clearPlayer(player);
			SanityEventManager.clearPlayer(player);
			return;
		}
		if (stage == SanityThresholds.Stage.STABLE) {
			SanityHallucinationService.dispelOwnedHallucinations(player);
			SanityObserverService.clearPlayer(player);
			SanityWorldAnomalyService.clearPlayer(player);
			SanityPsychologicalService.clearPlayer(player);
			SanitySignatureEventService.clearPlayer(player);
			SanityEventManager.clearPlayer(player);
			return;
		}

		SanitySignatureEventService.tickPlayer(player, component, gameTime);
		if (SanitySignatureEventService.isSilentWorldActive(player)) {
			SanityEventManager.tickPlayer(player, component, config, gameTime);
			return;
		}

		SanityAudioDirector.tickPlayer(player, component, stage, config);
		SanityHallucinationService.tickPlayer(player, component, stage, config);
		SanityWorldAnomalyService.tickPlayer(player, component, stage, gameTime);
		SanityFalseEventService.tickPlayer(player, component, stage);
		SanityMemoryDistortionService.tickPlayer(player, component, stage);
		SanityObserverService.tickPlayer(player, component, stage);
		SanityDoppelgangerService.tickPlayer(player, component, stage);
		SanityHudDistortionService.tickPlayer(player, component);
		SanityWorldWatcherService.tickPlayer(player, component, stage);
		SanityCollapseService.tickPlayer(player, component, stage, config);
		SanityEventManager.tickPlayer(player, component, config, gameTime);
	}
}
