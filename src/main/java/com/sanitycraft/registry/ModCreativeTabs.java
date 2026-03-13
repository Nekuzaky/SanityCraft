package com.sanitycraft.registry;

import com.sanitycraft.SanityCraft;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTabs {
	public static final ResourceKey<CreativeModeTab> MAIN_TAB_KEY =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, SanityCraft.id("main"));

	private ModCreativeTabs() {
	}

	public static void register() {
		Registry.register(
				BuiltInRegistries.CREATIVE_MODE_TAB,
				MAIN_TAB_KEY,
				FabricItemGroup.builder()
						.title(Component.translatable("item_group.sanitycraft.sanity_craft"))
						.icon(() -> new ItemStack(ModItems.PILL))
						.displayItems((parameters, output) -> {
							output.accept(ModItems.PILL);
							output.accept(ModItems.MENTAL_SHIELD_TOTEM);
						})
						.build());
	}
}
