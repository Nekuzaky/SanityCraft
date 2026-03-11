package net.nekuzaky.sanitycraft;

import net.nekuzaky.sanitycraft.init.SanitycraftModParticles;
import net.nekuzaky.sanitycraft.init.SanitycraftModEntityRenderers;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ClientModInitializer;

@Environment(EnvType.CLIENT)
public class SanitycraftModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Start of user code block mod constructor
		// End of user code block mod constructor
		SanitycraftModEntityRenderers.clientLoad();
		SanitycraftModParticles.clientLoad();
		// Start of user code block mod init
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(net.nekuzaky.sanitycraft.sanity.SanitySyncPayload.TYPE,
				(payload, context) -> context.client().execute(() -> net.nekuzaky.sanitycraft.client.SanityClientState.setSanity(payload.sanity())));
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(net.nekuzaky.sanitycraft.sanity.SanityJumpscarePayload.TYPE,
				(payload, context) -> context.client().execute(() -> net.nekuzaky.sanitycraft.client.SanityClientState.triggerJumpscare(payload.variant(), payload.durationTicks())));
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(net.nekuzaky.sanitycraft.sanity.SanityScarePulsePayload.TYPE,
				(payload, context) -> context.client().execute(() -> net.nekuzaky.sanitycraft.client.SanityClientState.triggerScarePulse(payload.durationTicks(), payload.intensity())));
		net.nekuzaky.sanitycraft.client.SanityHudRenderer.register();
		net.nekuzaky.sanitycraft.client.HorrorAmbienceDirector.register();
		net.nekuzaky.sanitycraft.client.MainMenuHorrorOverlay.register();
		net.nekuzaky.sanitycraft.client.DiscordRichPresenceManager.register();
		// End of user code block mod init
	}
	// Start of user code block mod methods
	// End of user code block mod methods
}