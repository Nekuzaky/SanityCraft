package com.sanitycraft.sanity.events;

import com.sanitycraft.data.config.SanityCraftConfig;
import com.sanitycraft.sanity.SanityComponent;
import com.sanitycraft.sanity.SanityGameplayUtilBridge;
import com.sanitycraft.sanity.SanitySignatureEventService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public final class SanityEventManager {
	private static final Map<UUID, Map<String, Long>> COOLDOWNS = new ConcurrentHashMap<>();
	private static final Map<UUID, List<FalseBlockIllusion>> ACTIVE_FALSE_BLOCKS = new ConcurrentHashMap<>();

	private SanityEventManager() {
	}

	public static void tickPlayer(ServerPlayer player, SanityComponent component, SanityCraftConfig config, long gameTime) {
		expireFalseBlocks(player, gameTime);
		if (SanitySignatureEventService.isSilentWorldActive(player)) {
			return;
		}
		if (component.hasHallucinationShield() || component.getSanity() > 70) {
			return;
		}
		if (component.getSanity() > 0) {
			SanityCollapseCombinationManager.clearPlayer(player);
		}
		if (component.getSanity() <= 0 && SanityCollapseCombinationManager.tickPlayer(player, component, config, gameTime)) {
			return;
		}

		SanityEventContext context = new SanityEventContext(player, component, config, gameTime);
		List<SanityEvent> events = SanityEventRegistry.events();
		if (events.isEmpty()) {
			return;
		}

		int start = context.random().nextInt(events.size());
		for (int offset = 0; offset < events.size(); offset++) {
			SanityEvent event = events.get((start + offset) % events.size());
			if (!event.canTrigger(context) || isOnCooldown(player, event.id(), gameTime)) {
				continue;
			}

			float chance = scaledChance(event, context);
			if (context.random().nextFloat() >= chance) {
				continue;
			}
			if (!component.tryConsumeEffectBudget(config, event.budgetCost())) {
				return;
			}
			if (event.trigger(context)) {
				long cooldownUntil = gameTime + SanityGameplayUtilBridge.jitter(event.cooldownTicks(), context.random(), Math.max(20, event.cooldownTicks() / 2));
				COOLDOWNS.computeIfAbsent(player.getUUID(), ignored -> new ConcurrentHashMap<>()).put(event.id(), cooldownUntil);
				context.log("id=" + event.id() + " duration=" + event.durationTicks() + " sanity=" + context.sanity());
				break;
			}
		}
	}

	public static String debugTrigger(ServerPlayer player, String eventId) {
		SanityEvent event = SanityEventRegistry.get(eventId);
		if (event == null) {
			return "Unknown sanity event: " + eventId;
		}

		SanityEventContext context = new SanityEventContext(player, com.sanitycraft.sanity.SanityManager.get(player), SanityCraftConfig.get(), player.level().getGameTime());
		boolean triggered = event.trigger(context);
		return triggered ? "Triggered sanity event: " + event.id() : "Could not trigger sanity event: " + event.id();
	}

	public static boolean triggerEvent(ServerPlayer player, SanityComponent component, SanityCraftConfig config, long gameTime, String eventId, boolean bypassRateLimit) {
		SanityEvent event = SanityEventRegistry.get(eventId);
		if (event == null) {
			return false;
		}
		return event.trigger(new SanityEventContext(player, component, config, gameTime, bypassRateLimit));
	}

	public static void clearPlayer(ServerPlayer player) {
		COOLDOWNS.remove(player.getUUID());
		SanitySignatureEventService.clearPlayer(player);
		SanityCollapseCombinationManager.clearPlayer(player);
		List<FalseBlockIllusion> illusions = ACTIVE_FALSE_BLOCKS.remove(player.getUUID());
		if (illusions == null) {
			return;
		}
		for (FalseBlockIllusion illusion : illusions) {
			revertFalseBlock(player, illusion.pos());
		}
	}

	public static void clearAll() {
		COOLDOWNS.clear();
		ACTIVE_FALSE_BLOCKS.clear();
		SanitySignatureEventService.clearAll();
		SanityCollapseCombinationManager.clearAll();
	}

	static boolean sendFalseBlock(ServerPlayer player, BlockPos pos, BlockState fakeState, long expireTick) {
		if (!player.level().hasChunkAt(pos)) {
			return false;
		}
		player.connection.send(new ClientboundBlockUpdatePacket(pos, fakeState));
		ACTIVE_FALSE_BLOCKS.computeIfAbsent(player.getUUID(), ignored -> new ArrayList<>()).add(new FalseBlockIllusion(pos.immutable(), expireTick));
		return true;
	}

	private static boolean isOnCooldown(ServerPlayer player, String eventId, long gameTime) {
		return COOLDOWNS.getOrDefault(player.getUUID(), Map.of()).getOrDefault(eventId, 0L) > gameTime;
	}

	private static float scaledChance(SanityEvent event, SanityEventContext context) {
		float pressure = Mth.clamp((event.sanityRequirement() - context.sanity()) / 30.0F, 0.0F, 1.35F);
		float stageBoost = switch (context.stage()) {
			case STABLE -> 0.0F;
			case UNEASY -> 0.85F;
			case DISTURBED -> 1.0F;
			case FRACTURED -> 1.18F;
			case COLLAPSE -> 1.36F;
		};
		return Mth.clamp(event.triggerChance() * (0.82F + stageBoost + pressure * 0.55F), 0.0F, 0.25F);
	}

	private static void expireFalseBlocks(ServerPlayer player, long gameTime) {
		List<FalseBlockIllusion> illusions = ACTIVE_FALSE_BLOCKS.get(player.getUUID());
		if (illusions == null) {
			return;
		}
		Iterator<FalseBlockIllusion> iterator = illusions.iterator();
		while (iterator.hasNext()) {
			FalseBlockIllusion illusion = iterator.next();
			if (illusion.expireTick() > gameTime) {
				continue;
			}
			revertFalseBlock(player, illusion.pos());
			iterator.remove();
		}
		if (illusions.isEmpty()) {
			ACTIVE_FALSE_BLOCKS.remove(player.getUUID());
		}
	}

	private static void revertFalseBlock(ServerPlayer player, BlockPos pos) {
		if (!player.level().hasChunkAt(pos)) {
			return;
		}
		player.connection.send(new ClientboundBlockUpdatePacket(pos, player.level().getBlockState(pos)));
	}

	private record FalseBlockIllusion(BlockPos pos, long expireTick) {
	}
}
