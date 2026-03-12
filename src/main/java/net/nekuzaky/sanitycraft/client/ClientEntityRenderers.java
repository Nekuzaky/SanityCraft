package net.nekuzaky.sanitycraft.client;

import net.nekuzaky.sanitycraft.client.renderer.StalkerRenderer;
import net.nekuzaky.sanitycraft.client.renderer.BloodyCreeperRenderer;
import net.nekuzaky.sanitycraft.registry.ModEntities;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
public final class ClientEntityRenderers {
	private ClientEntityRenderers() {
	}

	public static void register() {
		EntityRendererRegistry.register(ModEntities.STALKER, StalkerRenderer::new);
		EntityRendererRegistry.register(ModEntities.BLOODY_CREEPER, BloodyCreeperRenderer::new);
	}
}
