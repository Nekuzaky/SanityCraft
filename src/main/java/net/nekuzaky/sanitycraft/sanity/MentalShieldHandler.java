package net.nekuzaky.sanitycraft.sanity;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.nekuzaky.sanitycraft.init.SanitycraftModItems;

public class MentalShieldHandler {
	private static boolean registered = false;

	private MentalShieldHandler() {
	}

	public static void register() {
		if (registered) {
			return;
		}
		registered = true;

		UseItemCallback.EVENT.register((player, level, hand) -> {
			SanityConfig config = SanityManager.getConfig();
			if (!config.mentalShieldEnabled) {
				return InteractionResult.PASS;
			}

			if (SanitycraftModItems.MENTALSHIELDTOTEM == null) {
				return InteractionResult.PASS;
			}
			ItemStack stack = player.getItemInHand(hand);
			if (stack.isEmpty() || stack.getItem() != SanitycraftModItems.MENTALSHIELDTOTEM) {
				return InteractionResult.PASS;
			}

			if (!(player instanceof ServerPlayer serverPlayer)) {
				return InteractionResult.SUCCESS;
			}
			PlayerSanityComponent component = SanityManager.get(serverPlayer);
			component.setHallucinationShieldTicks(Math.max(1, config.mentalShieldDurationSeconds) * 20);
			SanityManager.setSanity(serverPlayer, PlayerSanityComponent.MAX_SANITY);
			serverPlayer.displayClientMessage(net.minecraft.network.chat.Component.literal("Mental shield engaged."), true);
			SanityJournal.log(serverPlayer, "I used a mental shield to silence the whispers.");

			if (!serverPlayer.getAbilities().instabuild) {
				stack.shrink(1);
			}
			level.playSound(null, serverPlayer.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8F, 1.0F);
			return InteractionResult.SUCCESS;
		});
	}
}
