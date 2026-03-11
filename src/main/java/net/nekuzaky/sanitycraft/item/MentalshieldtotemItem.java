package net.nekuzaky.sanitycraft.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

public class MentalshieldtotemItem extends Item {
	public MentalshieldtotemItem(Item.Properties properties) {
		super(properties.rarity(Rarity.UNCOMMON));
	}

	@Override
	public boolean isFoil(ItemStack itemstack) {
		return true;
	}
}