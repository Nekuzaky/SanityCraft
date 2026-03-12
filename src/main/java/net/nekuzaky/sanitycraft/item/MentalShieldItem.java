package net.nekuzaky.sanitycraft.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.nekuzaky.sanitycraft.sanity.PlayerSanityComponent;
import net.nekuzaky.sanitycraft.sanity.SanityConfig;
import net.nekuzaky.sanitycraft.sanity.SanityJournal;
import net.nekuzaky.sanitycraft.sanity.SanityManager;

public class MentalShieldItem extends Item {
	public MentalShieldItem(Item.Properties properties) {
		super(properties.rarity(Rarity.UNCOMMON));
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.SUCCESS;
		}

		SanityConfig config = SanityManager.getConfig();
		if (!config.mentalShieldEnabled) {
			return InteractionResult.PASS;
		}

		ItemStack stack = player.getItemInHand(hand);
		var component = SanityManager.get(serverPlayer);
		component.setHallucinationShieldTicks(Math.max(1, config.mentalShieldDurationSeconds) * 20);
		SanityManager.setSanity(serverPlayer, PlayerSanityComponent.MAX_SANITY);
		serverPlayer.displayClientMessage(Component.literal("Mental shield engaged."), true);
		SanityJournal.log(serverPlayer, "I used a mental shield to silence the whispers.");
		if (!serverPlayer.getAbilities().instabuild) {
			stack.shrink(1);
		}
		level.playSound(null, serverPlayer.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8F, 1.0F);
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}
}
