package com.sanitycraft.client.menu;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screens.TitleScreen;

@Environment(EnvType.CLIENT)
public final class SanityCraftMenuController {
	private static boolean registered;

	private SanityCraftMenuController() {
	}

	public static void register() {
		if (registered) {
			return;
		}
		registered = true;
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null || client.level != null || client.screen == null) {
				return;
			}
			if (client.screen.getClass() == TitleScreen.class) {
				client.setScreen(new SanityCraftTitleScreen());
			}
		});
	}
}
