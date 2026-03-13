package com.sanitycraft.client.render.entity;

import com.sanitycraft.entity.bloodycreeper.BloodyCreeperEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class BloodyCreeperRenderer extends MobRenderer<BloodyCreeperEntity, CreeperRenderState, CreeperModel> {
	private static final ResourceLocation TEXTURE = ResourceLocation.parse("sanitycraft:textures/entities/creeper.png");

	public BloodyCreeperRenderer(EntityRendererProvider.Context context) {
		super(context, new CreeperModel(context.bakeLayer(ModelLayers.CREEPER)), 0.5F);
	}

	@Override
	public CreeperRenderState createRenderState() {
		return new CreeperRenderState();
	}

	@Override
	public ResourceLocation getTextureLocation(CreeperRenderState state) {
		return TEXTURE;
	}
}
