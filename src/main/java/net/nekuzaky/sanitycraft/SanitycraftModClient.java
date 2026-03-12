package net.nekuzaky.sanitycraft;

import net.nekuzaky.sanitycraft.client.ClientBootstrap;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ClientModInitializer;

@Environment(EnvType.CLIENT)
public class SanitycraftModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientBootstrap.register();
	}
}
