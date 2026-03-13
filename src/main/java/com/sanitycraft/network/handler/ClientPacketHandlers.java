package com.sanitycraft.network.handler;

import com.sanitycraft.client.hud.ClientSanityState;
import com.sanitycraft.network.packet.ClientboundSanitySyncPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientPacketHandlers {
	private static boolean registered;

	private ClientPacketHandlers() {
	}

	public static void register() {
		if (registered) {
			return;
		}
		registered = true;
		ClientPlayNetworking.registerGlobalReceiver(
				ClientboundSanitySyncPacket.TYPE,
				(payload, context) -> context.client().execute(() -> ClientSanityState.setSanity(payload.sanity())));
	}
}
