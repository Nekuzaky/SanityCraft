/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.nekuzaky.sanitycraft.init;

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

public class SanitycraftModTabs {
	public static ResourceKey<CreativeModeTab> TAB_SANITY_CRAFT = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(SanitycraftMod.MODID, "sanity_craft"));

	public static void load() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_SANITY_CRAFT,
				FabricItemGroup.builder().title(Component.translatable("item_group.sanitycraft.sanity_craft")).icon(() -> new ItemStack(SanitycraftModItems.STALKER_SPAWN_EGG)).displayItems((parameters, tabData) -> {
					tabData.accept(SanitycraftModItems.PILL);
				}).build());
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(tabData -> {
			tabData.accept(SanitycraftModItems.STALKER_SPAWN_EGG);
			tabData.accept(SanitycraftModItems.BLOODY_CREEPER_SPAWN_EGG);
		});
	}
}