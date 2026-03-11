package net.nekuzaky.sanitycraft.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class PillItem extends Item {
	public PillItem(Item.Properties properties) {
		super(properties.rarity(Rarity.EPIC));
	}
}