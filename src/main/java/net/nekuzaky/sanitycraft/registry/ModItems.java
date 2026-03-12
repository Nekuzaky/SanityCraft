package net.nekuzaky.sanitycraft.registry;

import net.nekuzaky.sanitycraft.item.MentalShieldItem;
import net.nekuzaky.sanitycraft.item.SanityPillItem;
import net.nekuzaky.sanitycraft.SanitycraftMod;

import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;

import java.util.function.Function;

public final class ModItems {
	public static final Item STALKER_SPAWN_EGG = register("stalker_spawn_egg", properties -> new SpawnEggItem(ModEntities.STALKER, properties));
	public static final Item BLOODY_CREEPER_SPAWN_EGG = register("bloody_creeper_spawn_egg", properties -> new SpawnEggItem(ModEntities.BLOODY_CREEPER, properties));
	public static final Item PILL = register("pill", SanityPillItem::new);
	public static final Item MENTAL_SHIELD_TOTEM = register("mentalshieldtotem", MentalShieldItem::new);

	private ModItems() {
	}

	public static void register() {
	}

	private static <I extends Item> I register(String name, Function<Item.Properties, ? extends I> supplier) {
		return (I) Items.registerItem(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(SanitycraftMod.MODID, name)), (Function<Item.Properties, Item>) supplier);
	}
}
