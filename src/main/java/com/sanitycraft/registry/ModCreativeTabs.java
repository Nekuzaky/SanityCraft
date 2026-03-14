package com.sanitycraft.registry;

import com.sanitycraft.SanityCraft;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTabs {
	public static final ResourceKey<CreativeModeTab> SANITY_CRAFT_TAB_KEY =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, SanityCraft.id("sanity_craft"));

	private ModCreativeTabs() {
	}

	public static void register() {
		Registry.register(
				BuiltInRegistries.CREATIVE_MODE_TAB,
				SANITY_CRAFT_TAB_KEY,
				FabricItemGroup.builder()
						.title(Component.translatable("item_group.sanitycraft.sanity_craft"))
						.icon(() -> new ItemStack(ModItems.STALKER_SPAWN_EGG))
						.displayItems((parameters, output) -> {
							output.accept(ModItems.OBSERVER_SPAWN_EGG);
							output.accept(ModItems.STALKER_SPAWN_EGG);
							output.accept(ModItems.BLOODY_CREEPER_SPAWN_EGG);
							output.accept(ModItems.PILL);
							output.accept(ModItems.MENTAL_SHIELD_TOTEM);
						})
						.build());
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(output -> {
			output.accept(ModItems.OBSERVER_SPAWN_EGG);
			output.accept(ModItems.STALKER_SPAWN_EGG);
			output.accept(ModItems.BLOODY_CREEPER_SPAWN_EGG);
		});
	}
}
