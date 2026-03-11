package net.nekuzaky.sanitycraft.sanity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.nekuzaky.sanitycraft.SanitycraftMod;

public class SanityPersistence {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "sanitycraft_sanity.json";
	private static final Map<UUID, Integer> CACHE = new ConcurrentHashMap<>();
	private static Path loadedPath;

	private SanityPersistence() {
	}

	public static synchronized void ensureLoaded(MinecraftServer server) {
		Path path = getSavePath(server);
		if (path.equals(loadedPath)) {
			return;
		}
		loadedPath = path;
		CACHE.clear();
		if (!Files.exists(path)) {
			return;
		}
		try {
			String json = Files.readString(path);
			JsonObject root = JsonParser.parseString(json).getAsJsonObject();
			for (String key : root.keySet()) {
				try {
					UUID uuid = UUID.fromString(key);
					int sanity = root.get(key).getAsInt();
					CACHE.put(uuid, Math.max(PlayerSanityComponent.MIN_SANITY, Math.min(PlayerSanityComponent.MAX_SANITY, sanity)));
				} catch (Exception ignored) {
				}
			}
		} catch (Exception e) {
			SanitycraftMod.LOGGER.error("Failed to load sanity persistence.", e);
		}
	}

	public static int get(ServerPlayer player) {
		ensureLoaded(player.level().getServer());
		return CACHE.getOrDefault(player.getUUID(), PlayerSanityComponent.DEFAULT_SANITY);
	}

	public static synchronized void set(ServerPlayer player, int sanity) {
		ensureLoaded(player.level().getServer());
		CACHE.put(player.getUUID(), Math.max(PlayerSanityComponent.MIN_SANITY, Math.min(PlayerSanityComponent.MAX_SANITY, sanity)));
		save(player.level().getServer());
	}

	public static synchronized void save(MinecraftServer server) {
		if (server == null) {
			return;
		}
		Path path = getSavePath(server);
		try {
			Files.createDirectories(path.getParent());
			JsonObject root = new JsonObject();
			for (Map.Entry<UUID, Integer> entry : CACHE.entrySet()) {
				root.addProperty(entry.getKey().toString(), entry.getValue());
			}
			Files.writeString(path, GSON.toJson(root));
		} catch (IOException e) {
			SanitycraftMod.LOGGER.error("Failed to save sanity persistence.", e);
		}
	}

	private static Path getSavePath(MinecraftServer server) {
		return server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(FILE_NAME);
	}
}
