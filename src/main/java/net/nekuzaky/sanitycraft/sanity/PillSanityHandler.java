package net.nekuzaky.sanitycraft.sanity;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.nekuzaky.sanitycraft.init.SanitycraftModItems;

public class PillSanityHandler {
	private static boolean registered = false;

	private PillSanityHandler() {
	}

	public static void register() {
		if (registered) {
			return;
		}
		registered = true;

		UseItemCallback.EVENT.register((player, level, hand) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (stack.isEmpty() || stack.getItem() != SanitycraftModItems.PILL) {
				return InteractionResult.PASS;
			}

			if (!(player instanceof ServerPlayer serverPlayer)) {
				return InteractionResult.SUCCESS;
			}

			int current = SanityManager.get(serverPlayer).getSanity();
			if (current >= PlayerSanityComponent.MAX_SANITY) {
				return InteractionResult.FAIL;
			}

			SanityConfig config = SanityManager.getConfig();
			SanityManager.addSanity(serverPlayer, Math.max(1, config.pillSanityGain));
			serverPlayer.getCooldowns().addCooldown(stack, Math.max(0, config.pillCooldownTicks));

			if (!serverPlayer.getAbilities().instabuild) {
				stack.shrink(1);
			}

			level.playSound(null, serverPlayer.blockPosition(), SoundEvents.HONEY_DRINK.value(), SoundSource.PLAYERS, 0.8F, 0.9F + serverPlayer.getRandom().nextFloat() * 0.2F);
			return InteractionResult.SUCCESS;
		});
	}
}
