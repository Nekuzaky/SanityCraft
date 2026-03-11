/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.nekuzaky.sanitycraft.init;

import net.minecraft.world.item.CreativeModeTabs;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public class SanitycraftModTabs {
	public static void load() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(tabData -> {
			tabData.accept(SanitycraftModItems.STALKER_SPAWN_EGG);
		});
	}
}