package com.sanitycraft.client;

import com.sanitycraft.network.handler.ClientPacketHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class SanityCraftClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPacketHandlers.register();
	}
}
