package com.sanitycraft.network.handler;

import com.sanitycraft.client.hud.ClientSanityState;
import com.sanitycraft.client.menu.SanityCraftMenuEffects;
import com.sanitycraft.network.packet.ClientboundMenuTestPacket;
import com.sanitycraft.network.packet.ClientboundScarePulsePacket;
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
		ClientPlayNetworking.registerGlobalReceiver(
				ClientboundScarePulsePacket.TYPE,
				(payload, context) -> context.client().execute(() -> ClientSanityState.triggerScarePulse(payload.durationTicks(), payload.intensity())));
		ClientPlayNetworking.registerGlobalReceiver(
				ClientboundMenuTestPacket.TYPE,
				(payload, context) -> context.client().execute(() -> SanityCraftMenuEffects.triggerDebug(payload.test())));
	}
}
