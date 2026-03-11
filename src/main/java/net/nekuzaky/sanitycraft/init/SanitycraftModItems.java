/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.nekuzaky.sanitycraft.init;

import net.nekuzaky.sanitycraft.SanitycraftMod;

import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;

import java.util.function.Function;

public class SanitycraftModItems {
	public static Item STALKER_SPAWN_EGG;

	public static void load() {
		STALKER_SPAWN_EGG = register("stalker_spawn_egg", properties -> new SpawnEggItem(SanitycraftModEntities.STALKER, properties));
	}

	// Start of user code block custom items
	// End of user code block custom items
	private static <I extends Item> I register(String name, Function<Item.Properties, ? extends I> supplier) {
		return (I) Items.registerItem(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(SanitycraftMod.MODID, name)), (Function<Item.Properties, Item>) supplier);
	}
}