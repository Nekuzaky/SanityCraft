package com.sanitycraft.client.menu;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ClientMenuBridge {
	private static boolean registered;

	private ClientMenuBridge() {
	}

	public static void register() {
		if (registered) {
			return;
		}
		registered = true;
		SanityCraftMenuController.register();
	}
}
