package net.nekuzaky.sanitycraft.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.nekuzaky.sanitycraft.sanity.SanityJumpscarePayload;
import net.nekuzaky.sanitycraft.sanity.SanityScarePulsePayload;
import net.nekuzaky.sanitycraft.sanity.SanitySyncPayload;

public final class ClientBootstrap {
	private ClientBootstrap() {
	}

	public static void register() {
		ClientEntityRenderers.register();
		ClientParticles.register();
		ClientPlayNetworking.registerGlobalReceiver(SanitySyncPayload.TYPE,
				(payload, context) -> context.client().execute(() -> SanityClientState.setSanity(payload.sanity())));
		ClientPlayNetworking.registerGlobalReceiver(SanityJumpscarePayload.TYPE,
				(payload, context) -> context.client().execute(() -> SanityClientState.triggerJumpscare(payload.variant(), payload.durationTicks())));
		ClientPlayNetworking.registerGlobalReceiver(SanityScarePulsePayload.TYPE,
				(payload, context) -> context.client().execute(() -> SanityClientState.triggerScarePulse(payload.durationTicks(), payload.intensity())));
		SanityHudRenderer.register();
		HorrorAmbienceDirector.register();
		MainMenuHorrorOverlay.register();
		DiscordRichPresenceManager.register();
	}
}
