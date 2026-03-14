package com.sanitycraft.client;

import com.sanitycraft.client.events.SanityClientEventState;
import com.sanitycraft.client.hud.SanityHudRenderer;
import com.sanitycraft.client.hud.ClientSanityState;
import com.sanitycraft.client.menu.SanityCraftMenuController;
import com.sanitycraft.client.particles.ClientParticles;
import com.sanitycraft.client.particles.SanityBloodParticleEmitter;
import com.sanitycraft.client.render.ClientEntityRenderers;
import com.sanitycraft.network.handler.ClientPacketHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

@Environment(EnvType.CLIENT)
public final class SanityCraftClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPacketHandlers.register();
		ClientTickEvents.END_CLIENT_TICK.register(ClientSanityState::tick);
		ClientTickEvents.END_CLIENT_TICK.register(SanityClientEventState::tick);
		ClientTickEvents.END_CLIENT_TICK.register(SanityBloodParticleEmitter::tick);
		WorldRenderEvents.AFTER_ENTITIES.register(SanityClientEventState::renderWorld);
		SanityHudRenderer.register();
		ClientEntityRenderers.register();
		ClientParticles.register();
		SanityCraftMenuController.register();
	}
}
