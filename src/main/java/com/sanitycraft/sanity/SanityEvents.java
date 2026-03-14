package com.sanitycraft.sanity;

import com.sanitycraft.network.sync.ClientEffectSyncService;
import com.sanitycraft.sanity.events.SanityEventManager;
import com.sanitycraft.sanity.gameplay.SanityObserverService;
import com.sanitycraft.sanity.gameplay.SanityWorldAnomalyService;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;

public final class SanityEvents {
	private static boolean registered;

	private SanityEvents() {
	}

	public static void register() {
		if (registered) {
			return;
		}
		registered = true;

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			SanityManager.load(handler.player);
			server.execute(() -> {
				var component = SanityManager.get(handler.player);
				long gameTime = handler.player.level().getGameTime();
				com.sanitycraft.network.sync.SanitySyncService.syncNow(handler.player, component, gameTime);
			});
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			SanityManager.save(handler.player);
			SanityHallucinationService.dispelOwnedHallucinations(handler.player);
			SanityObserverService.clearPlayer(handler.player);
			SanityWorldAnomalyService.clearPlayer(handler.player);
			SanityPsychologicalService.clearPlayer(handler.player);
			SanityPsychologicalService.clearPlayerHistory(handler.player);
			SanitySignatureEventService.clearPlayer(handler.player);
			SanitySignatureEventService.clearPlayerHistory(handler.player);
			SanityEventManager.clearPlayer(handler.player);
			ClientEffectSyncService.clearPlayer(handler.player);
			SanityDebug.clearPlayer(handler.player);
			SanityManager.remove(handler.player);
		});

		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			if (alive) {
				SanityManager.copy(oldPlayer, newPlayer);
			} else {
				SanityManager.setSanity(newPlayer, com.sanitycraft.data.config.SanityCraftConfig.get().general.respawnSanity);
			}
			com.sanitycraft.network.sync.SanitySyncService.syncNow(newPlayer, SanityManager.get(newPlayer), newPlayer.level().getGameTime());
		});

		ServerTickEvents.END_SERVER_TICK.register(SanityTickService::tickServer);
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (world.isClientSide() || !(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
				return;
			}
			SanityPsychologicalService.recordBrokenBlock(serverPlayer, pos, state, world.getGameTime());
		});
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (world.isClientSide() || hand != InteractionHand.MAIN_HAND || !(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
				return InteractionResult.PASS;
			}

			BlockPos clickedPos = hitResult.getBlockPos();
			if (serverPlayer.getItemInHand(hand).getItem() instanceof BlockItem) {
				BlockPos targetPos = clickedPos.relative(hitResult.getDirection());
				SanityPsychologicalService.recordPotentialPlacement(serverPlayer, targetPos, world.getBlockState(targetPos), world.getGameTime());
			}

			var clickedState = world.getBlockState(clickedPos);
			if (clickedState.getBlock() instanceof DoorBlock || clickedState.getBlock() instanceof TrapDoorBlock || clickedState.getBlock() instanceof FenceGateBlock) {
				SanityPsychologicalService.recordDoorInteraction(serverPlayer, clickedPos, clickedState, world.getGameTime());
			}

			return InteractionResult.PASS;
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			ClientEffectSyncService.clearAll();
			SanityWorldAnomalyService.clearAll();
			SanityPsychologicalService.clearAll();
			SanitySignatureEventService.clearAll();
			SanityEventManager.clearAll();
			SanityDebug.clearAll();
			SanityManager.clearAll();
		});
	}
}
