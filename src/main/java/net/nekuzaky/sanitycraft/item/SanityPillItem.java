package net.nekuzaky.sanitycraft.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.nekuzaky.sanitycraft.sanity.PlayerSanityComponent;
import net.nekuzaky.sanitycraft.sanity.SanityConfig;
import net.nekuzaky.sanitycraft.sanity.SanityManager;

public class SanityPillItem extends Item {
	public SanityPillItem(Item.Properties properties) {
		super(properties.rarity(Rarity.EPIC));
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.SUCCESS;
		}

		ItemStack stack = player.getItemInHand(hand);
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
	}
}
