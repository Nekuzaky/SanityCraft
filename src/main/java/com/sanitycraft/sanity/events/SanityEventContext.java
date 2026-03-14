package com.sanitycraft.sanity.events;

import com.sanitycraft.data.config.SanityCraftConfig;
import com.sanitycraft.network.sync.ClientEffectSyncService;
import com.sanitycraft.sanity.SanityComponent;
import com.sanitycraft.sanity.SanityDebug;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public final class SanityEventContext {
	private final ServerPlayer player;
	private final SanityComponent component;
	private final SanityCraftConfig config;
	private final long gameTime;
	private final RandomSource random;
	private final SanityEventStage stage;
	private final boolean bypassRateLimit;

	SanityEventContext(ServerPlayer player, SanityComponent component, SanityCraftConfig config, long gameTime) {
		this(player, component, config, gameTime, false);
	}

	SanityEventContext(ServerPlayer player, SanityComponent component, SanityCraftConfig config, long gameTime, boolean bypassRateLimit) {
		this.player = player;
		this.component = component;
		this.config = config;
		this.gameTime = gameTime;
		this.random = player.getRandom();
		this.stage = SanityEventStage.resolve(component.getSanity());
		this.bypassRateLimit = bypassRateLimit;
	}

	public ServerPlayer player() {
		return player;
	}

	public SanityComponent component() {
		return component;
	}

	public SanityCraftConfig config() {
		return config;
	}

	public long gameTime() {
		return gameTime;
	}

	public RandomSource random() {
		return random;
	}

	public int sanity() {
		return component.getSanity();
	}

	public SanityEventStage stage() {
		return stage;
	}

	public boolean sendClientEvent(String eventId, BlockPos anchor, int durationTicks, int intensity, int variant, String text, String source) {
		return ClientEffectSyncService.sendSanityEvent(
				player,
				eventId,
				anchor,
				durationTicks,
				intensity,
				variant,
				text,
				source,
				bypassRateLimit);
	}

	public boolean sendClientEvent(String eventId, BlockPos anchor, int durationTicks, int intensity, int variant, String text, String source, boolean bypassRateLimit) {
		return ClientEffectSyncService.sendSanityEvent(
				player,
				eventId,
				anchor,
				durationTicks,
				intensity,
				variant,
				text,
				source,
				bypassRateLimit);
	}

	public boolean sendFalseBlock(BlockPos pos, BlockState fakeState, int durationTicks) {
		return SanityEventManager.sendFalseBlock(player, pos, fakeState, gameTime + Math.max(1, durationTicks));
	}

	public void log(String details) {
		SanityDebug.logEvent(player, "sanity_event " + details);
	}
}
