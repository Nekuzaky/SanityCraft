package com.sanitycraft.registry;

import com.sanitycraft.SanityCraft;
import com.sanitycraft.item.consumable.PillItem;
import com.sanitycraft.item.custom.MentalShieldTotemItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public final class ModItems {
	public static final Item PILL = register("pill", new PillItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
	public static final Item MENTAL_SHIELD_TOTEM =
			register("mentalshieldtotem", new MentalShieldTotemItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

	private ModItems() {
	}

	public static void register() {
	}

	private static <T extends Item> T register(String path, T item) {
		return Registry.register(BuiltInRegistries.ITEM, SanityCraft.id(path), item);
	}
}
