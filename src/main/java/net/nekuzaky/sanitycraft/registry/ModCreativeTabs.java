package net.nekuzaky.sanitycraft.registry;

import net.nekuzaky.sanitycraft.SanitycraftMod;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

public final class ModCreativeTabs {
	public static final ResourceKey<CreativeModeTab> TAB_SANITY_CRAFT = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(SanitycraftMod.MODID, "sanity_craft"));

	private ModCreativeTabs() {
	}

	public static void register() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_SANITY_CRAFT,
				FabricItemGroup.builder().title(Component.translatable("item_group.sanitycraft.sanity_craft")).icon(() -> new ItemStack(ModItems.STALKER_SPAWN_EGG)).displayItems((parameters, tabData) -> {
					tabData.accept(ModItems.PILL);
					tabData.accept(ModItems.MENTAL_SHIELD_TOTEM);
				}).build());
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(tabData -> {
			tabData.accept(ModItems.STALKER_SPAWN_EGG);
			tabData.accept(ModItems.BLOODY_CREEPER_SPAWN_EGG);
		});
	}
}
