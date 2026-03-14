package com.sanitycraft.client.render;

import com.sanitycraft.client.render.entity.BloodyCreeperRenderer;
import com.sanitycraft.client.render.entity.ObserverRenderer;
import com.sanitycraft.client.render.entity.StalkerRenderer;
import com.sanitycraft.registry.ModEntities;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public final class ClientEntityRenderers {
	private ClientEntityRenderers() {
	}

	public static void register() {
		EntityRendererRegistry.register(ModEntities.STALKER, StalkerRenderer::new);
		EntityRendererRegistry.register(ModEntities.BLOODY_CREEPER, BloodyCreeperRenderer::new);
		EntityRendererRegistry.register(ModEntities.OBSERVER, ObserverRenderer::new);
	}
}
