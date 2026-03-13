package com.sanitycraft.sanity;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class SanityDebug {
	private SanityDebug() {
	}

	public static Component describe(ServerPlayer player, SanityComponent component) {
		return Component.literal(player.getName().getString() + ": " + component.getSanity() + " (" + component.getStage().name().toLowerCase() + ")");
	}

	public static Component directorStatus(SanityComponent component) {
		return Component.literal("sanity=" + component.getSanity() + " stage=" + component.getStage().name().toLowerCase());
	}
}
