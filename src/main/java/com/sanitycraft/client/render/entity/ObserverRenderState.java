package com.sanitycraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class ObserverRenderState extends EndermanRenderState {
	public ResourceLocation texture = ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");
}
