/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.nekuzaky.sanitycraft.init;

import net.nekuzaky.sanitycraft.client.renderer.StalkerRenderer;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
public class SanitycraftModEntityRenderers {
	public static void clientLoad() {
		EntityRendererRegistry.register(SanitycraftModEntities.STALKER, StalkerRenderer::new);
	}
}