package com.sanitycraft.item.custom;

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

public final class MentalShieldTotemItem extends Item {
	public MentalShieldTotemItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.SUCCESS;
		}

		SanityCraftConfig config = SanityCraftConfig.get();
		ItemStack stack = player.getItemInHand(hand);
		SanityManager.addSanity(serverPlayer, Math.max(0, config.recovery.mentalShieldRestore));
		SanityManager.activateMentalShield(serverPlayer, config.recovery.mentalShieldDurationTicks);
		serverPlayer.getCooldowns().addCooldown(stack, config.recovery.mentalShieldCooldownTicks);
		if (!serverPlayer.getAbilities().instabuild) {
			stack.shrink(1);
		}
		level.playSound(null, serverPlayer.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8F, 1.0F);
		return InteractionResult.SUCCESS;
	}
}
