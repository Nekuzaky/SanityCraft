package com.sanitycraft.sanity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sanitycraft.SanityCraft;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

public final class SanityPersistence {
	private static final String LEGACY_FILE_NAME = "sanitycraft_sanity.json";

	private SanityPersistence() {
	}

	public static int load(ServerPlayer player) {
		MinecraftServer server = player.getServer();
		if (server == null) {
			return SanityThresholds.DEFAULT_SANITY;
		}
		return getStore(server).get(player.getUUID());
	}

	public static void save(ServerPlayer player, int sanity) {
		MinecraftServer server = player.getServer();
		if (server == null) {
			return;
		}
		getStore(server).set(player.getUUID(), sanity);
	}

	private static SanitySavedData getStore(MinecraftServer server) {
		SanitySavedData store = server.overworld().getDataStorage().computeIfAbsent(SanitySavedData.TYPE);
		ensureLegacyMigrated(server, store);
		return store;
	}

	private static void ensureLegacyMigrated(MinecraftServer server, SanitySavedData store) {
		if (store.legacyMigrated()) {
			return;
		}

		Path legacyPath = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(LEGACY_FILE_NAME);
		if (Files.exists(legacyPath)) {
			try {
				JsonObject root = JsonParser.parseString(Files.readString(legacyPath)).getAsJsonObject();
				var migrated = new java.util.HashMap<UUID, Integer>();
				for (String key : root.keySet()) {
					try {
						migrated.put(UUID.fromString(key), root.get(key).getAsInt());
					} catch (Exception ignored) {
					}
				}
				store.importLegacy(migrated);
			} catch (Exception exception) {
				SanityCraft.LOGGER.error("Failed to migrate legacy sanity data from {}", legacyPath, exception);
			}
		}

		store.markLegacyMigrated();
	}
}
