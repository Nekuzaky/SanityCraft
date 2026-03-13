package com.sanitycraft.sanity;

import com.sanitycraft.SanityCraft;
import com.sanitycraft.data.config.SanityCraftConfig;
import com.sanitycraft.network.sync.SanitySyncService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class SanityTickService {
	private SanityTickService() {
	}

	public static void tickServer(MinecraftServer server) {
		SanityCraftConfig config = SanityCraftConfig.get();
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			tickPlayer(player, config);
		}
	}

	private static void tickPlayer(ServerPlayer player, SanityCraftConfig config) {
		SanityComponent component = SanityManager.get(player);
		long gameTime = player.level().getGameTime();
		component.tickRuntime();
		SanityDebug.logTickOwnerConfirmation(player, component, gameTime);
		if (component.shouldEvaluate(gameTime, config.ticking.updateIntervalTicks)) {
			SanityTriggers triggers = SanityTriggerSampler.sampleTriggers(player, config);
			SanityRecovery recovery = SanityTriggerSampler.sampleRecovery(player, config);
			int delta = SanityCalculator.calculateDelta(component, triggers, recovery, config);
			if (delta != 0) {
				SanityManager.addSanity(player, delta);
			}
		}
		SanityEffectService.tickPlayer(player, component, config, gameTime);
		SanitySyncService.syncIfNeeded(player, component, gameTime, config);
	}
}
