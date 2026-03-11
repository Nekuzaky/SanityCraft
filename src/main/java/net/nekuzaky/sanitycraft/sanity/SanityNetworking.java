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
	}

	public static void sync(ServerPlayer player, int sanity) {
		ServerPlayNetworking.send(player, new SanitySyncPayload(sanity));
	}
}
