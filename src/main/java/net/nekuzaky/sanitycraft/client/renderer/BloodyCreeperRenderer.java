package net.nekuzaky.sanitycraft.client.renderer;

import net.nekuzaky.sanitycraft.entity.BloodyCreeperEntity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.CreeperModel;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
public class BloodyCreeperRenderer extends MobRenderer<BloodyCreeperEntity, CreeperRenderState, CreeperModel> {
	private BloodyCreeperEntity entity = null;

	public BloodyCreeperRenderer(EntityRendererProvider.Context context) {
		super(context, new CreeperModel(context.bakeLayer(ModelLayers.CREEPER)), 0.5f);
	}

	@Override
	public CreeperRenderState createRenderState() {
		return new CreeperRenderState();
	}

	@Override
	public void extractRenderState(BloodyCreeperEntity entity, CreeperRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		this.entity = entity;
	}

	@Override
	public ResourceLocation getTextureLocation(CreeperRenderState state) {
		return ResourceLocation.parse("sanitycraft:textures/entities/creeper.png");
	}
}