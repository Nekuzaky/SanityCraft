package com.sanitycraft.client;

import com.sanitycraft.client.hud.SanityHudRenderer;
import com.sanitycraft.client.hud.ClientSanityState;
import com.sanitycraft.client.menu.SanityCraftMenuController;
import com.sanitycraft.client.particles.ClientParticles;
import com.sanitycraft.client.render.ClientEntityRenderers;
import com.sanitycraft.network.handler.ClientPacketHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

@Environment(EnvType.CLIENT)
public final class SanityCraftClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPacketHandlers.register();
		ClientTickEvents.END_CLIENT_TICK.register(ClientSanityState::tick);
		SanityHudRenderer.register();
		ClientEntityRenderers.register();
		ClientParticles.register();
		SanityCraftMenuController.register();
	}
}
