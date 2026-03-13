package com.sanitycraft.item.consumable;

import com.sanitycraft.data.config.SanityCraftConfig;
import com.sanitycraft.sanity.SanityManager;
import com.sanitycraft.sanity.SanityThresholds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class PillItem extends Item {
	public PillItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.SUCCESS;
		}

		if (SanityManager.get(serverPlayer).getSanity() >= SanityThresholds.MAX_SANITY) {
			return InteractionResult.FAIL;
		}

		ItemStack stack = player.getItemInHand(hand);
		int restore = Math.max(1, SanityCraftConfig.get().recovery.pillRestore);
		SanityManager.addSanity(serverPlayer, restore);
		serverPlayer.getCooldowns().addCooldown(stack, 40);
		if (!serverPlayer.getAbilities().instabuild) {
			stack.shrink(1);
		}
		level.playSound(null, serverPlayer.blockPosition(), SoundEvents.HONEY_DRINK.value(), SoundSource.PLAYERS, 0.7F, 0.95F);
		return InteractionResult.SUCCESS;
	}
}
