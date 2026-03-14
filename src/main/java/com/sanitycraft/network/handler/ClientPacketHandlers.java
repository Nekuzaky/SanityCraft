package com.sanitycraft.network.handler;

import com.sanitycraft.client.hud.ClientSanityState;
import com.sanitycraft.client.events.SanityClientEventState;
import com.sanitycraft.client.menu.SanityCraftMenuEffects;
import com.sanitycraft.network.packet.ClientboundFalseFeedbackPacket;
import com.sanitycraft.network.packet.ClientboundHudDistortionPacket;
import com.sanitycraft.network.packet.ClientboundMenuTestPacket;
import com.sanitycraft.network.packet.ClientboundSanityEventPacket;
import com.sanitycraft.network.packet.ClientboundScarePulsePacket;
import com.sanitycraft.network.packet.ClientboundSanitySyncPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

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
				ClientboundFalseFeedbackPacket.TYPE,
				(payload, context) -> context.client().execute(() -> {
					if (context.client().player != null) {
						context.client().player.displayClientMessage(
								Component.literal(payload.message()).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
								false);
					}
				}));
		ClientPlayNetworking.registerGlobalReceiver(
				ClientboundHudDistortionPacket.TYPE,
				(payload, context) -> context.client().execute(() -> ClientSanityState.triggerHudDistortion(
						payload.durationTicks(),
						payload.intensity(),
						payload.fakeDamageFlash())));
		ClientPlayNetworking.registerGlobalReceiver(
				ClientboundSanitySyncPacket.TYPE,
				(payload, context) -> context.client().execute(() -> ClientSanityState.setSanity(payload.sanity())));
		ClientPlayNetworking.registerGlobalReceiver(
				ClientboundScarePulsePacket.TYPE,
				(payload, context) -> context.client().execute(() -> ClientSanityState.triggerScarePulse(payload.durationTicks(), payload.intensity())));
		ClientPlayNetworking.registerGlobalReceiver(
				ClientboundMenuTestPacket.TYPE,
				(payload, context) -> context.client().execute(() -> SanityCraftMenuEffects.triggerDebug(payload.test())));
		ClientPlayNetworking.registerGlobalReceiver(
				ClientboundSanityEventPacket.TYPE,
				(payload, context) -> context.client().execute(() -> SanityClientEventState.handleEvent(payload, context.client())));
	}
}
