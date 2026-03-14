package com.sanitycraft.client.render.entity;

import com.sanitycraft.entity.observer.ObserverEntity;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class ObserverRenderer extends MobRenderer<ObserverEntity, ObserverRenderState, EndermanModel<ObserverRenderState>> {
	private static final ResourceLocation OBSERVER_TEXTURE = ResourceLocation.parse("sanitycraft:textures/entities/the_observer.png");

	public ObserverRenderer(EntityRendererProvider.Context context) {
		super(context, new EndermanModel<>(context.bakeLayer(ModelLayers.ENDERMAN)), 0.5F);
	}

	@Override
	public ObserverRenderState createRenderState() {
		return new ObserverRenderState();
	}

	@Override
	public void extractRenderState(ObserverEntity entity, ObserverRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.isCreepy = false;
		state.carriedBlock = null;
		state.texture = resolveTexture(entity);
	}

	@Override
	public ResourceLocation getTextureLocation(ObserverRenderState state) {
		return state.texture;
	}

	private ResourceLocation resolveTexture(ObserverEntity entity) {
		if (!entity.isDoppelganger()) {
			return OBSERVER_TEXTURE;
		}

		Minecraft minecraft = Minecraft.getInstance();
		UUID ownerUuid = entity.getOwnerUuid();
		if (ownerUuid == null) {
			return DefaultPlayerSkin.getDefaultTexture();
		}
		if (minecraft.getConnection() != null) {
			PlayerInfo playerInfo = minecraft.getConnection().getPlayerInfo(ownerUuid);
			if (playerInfo != null) {
				return playerInfo.getSkin().texture();
			}
		}
		PlayerSkin skin = minecraft.getSkinManager().getInsecureSkin(entity.createOwnerProfile());
		return skin.texture();
	}
}
