package com.sanitycraft.registry;

import com.sanitycraft.SanityCraft;
import com.sanitycraft.item.consumable.PillItem;
import com.sanitycraft.item.custom.MentalShieldTotemItem;
import java.util.function.Function;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;

public final class ModItems {
	public static final Item STALKER_SPAWN_EGG = register("stalker_spawn_egg", properties -> new SpawnEggItem(ModEntities.STALKER, properties));
	public static final Item BLOODY_CREEPER_SPAWN_EGG = register("bloody_creeper_spawn_egg", properties -> new SpawnEggItem(ModEntities.BLOODY_CREEPER, properties));
	public static final Item OBSERVER_SPAWN_EGG = register("observer_spawn_egg", properties -> new SpawnEggItem(ModEntities.OBSERVER, properties));
	public static final Item PILL = register("pill", properties -> new PillItem(properties.stacksTo(16).rarity(Rarity.UNCOMMON)));
	public static final Item MENTAL_SHIELD_TOTEM =
			register("mentalshieldtotem", properties -> new MentalShieldTotemItem(properties.stacksTo(1).rarity(Rarity.RARE)));

	private ModItems() {
	}

	public static void register() {
	}

	@SuppressWarnings("unchecked")
	private static <T extends Item> T register(String path, Function<Item.Properties, ? extends T> factory) {
		return (T) Items.registerItem(ResourceKey.create(Registries.ITEM, SanityCraft.id(path)), (Function<Item.Properties, Item>) factory);
	}
}
