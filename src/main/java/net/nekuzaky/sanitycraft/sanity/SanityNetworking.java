package net.nekuzaky.sanitycraft.sanity;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class SanityNetworking {
	private static boolean initialized = false;

	private SanityNetworking() {
	}

	public static void initialize() {
		if (initialized) {
			return;
		}
		initialized = true;
		PayloadTypeRegistry.playS2C().register(SanitySyncPayload.TYPE, SanitySyncPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(SanityJumpscarePayload.TYPE, SanityJumpscarePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(SanityScarePulsePayload.TYPE, SanityScarePulsePayload.CODEC);
	}

	public static void sync(ServerPlayer player, int sanity) {
		ServerPlayNetworking.send(player, new SanitySyncPayload(sanity));
	}

	public static void triggerJumpscare(ServerPlayer player, int variant, int durationTicks) {
		ServerPlayNetworking.send(player, new SanityJumpscarePayload(variant, durationTicks));
	}

	public static void triggerScarePulse(ServerPlayer player, int durationTicks, int intensity) {
		ServerPlayNetworking.send(player, new SanityScarePulsePayload(Math.max(1, durationTicks), Math.max(1, intensity)));
	}
}
