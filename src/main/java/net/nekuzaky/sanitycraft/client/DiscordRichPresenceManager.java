package net.nekuzaky.sanitycraft.client;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.nekuzaky.sanitycraft.SanitycraftMod;
import net.nekuzaky.sanitycraft.sanity.SanityConfig;
import net.nekuzaky.sanitycraft.sanity.SanityManager;
import net.nekuzaky.sanitycraft.sanity.SanityStage;
import net.nekuzaky.sanitycraft.sanity.SanityStageResolver;

public class DiscordRichPresenceManager {
	private static boolean registered = false;
	private static DiscordIpcClient ipcClient;
	private static long lastUpdateMs = 0L;
	private static long sessionStartEpoch = System.currentTimeMillis() / 1000L;
	private static long worldStartEpoch = 0L;
	private static String lastDetails = "";
	private static String lastState = "";

	private DiscordRichPresenceManager() {
	}

	public static void register() {
		if (registered) {
			return;
		}
		registered = true;

		SanityConfig config = SanityManager.getConfig();
		if (!config.discordPresenceEnabled) {
			return;
		}
		String appId = config.discordApplicationId == null ? "" : config.discordApplicationId.trim();
		if (appId.isEmpty()) {
			return;
		}

		ipcClient = new DiscordIpcClient(appId);
		ClientTickEvents.END_CLIENT_TICK.register(DiscordRichPresenceManager::tick);
		Runtime.getRuntime().addShutdownHook(new Thread(DiscordRichPresenceManager::shutdown, "Sanitycraft-Discord-RPC-Shutdown"));
	}

	private static void tick(Minecraft client) {
		if (ipcClient == null) {
			return;
		}

		long now = System.currentTimeMillis();
		if (now - lastUpdateMs < 5000L) {
			return;
		}
		lastUpdateMs = now;

		String details;
		String state;
		long startEpoch;

		if (client.player == null || client.level == null) {
			worldStartEpoch = 0L;
			details = "In Main Menu";
			state = "Preparing to lose sanity";
			startEpoch = sessionStartEpoch;
		} else {
			if (worldStartEpoch <= 0L) {
				worldStartEpoch = System.currentTimeMillis() / 1000L;
			}
			int sanity = Mth.clamp(SanityClientState.getSanity(), 0, 100);
			SanityStage stage = SanityStageResolver.resolve(sanity);
			String biome = client.level.getBiome(client.player.blockPosition()).unwrapKey().map(key -> formatName(key.location())).orElse("Unknown");
			String dimension = formatName(client.level.dimension().location());

			details = "Sanity " + sanity + " - " + formatStage(stage);
			state = SanityClientState.isZeroSanityActive() ? "Mind collapse imminent | " + biome : biome + " | " + dimension;
			startEpoch = worldStartEpoch;
		}

		if (details.equals(lastDetails) && state.equals(lastState)) {
			return;
		}
		lastDetails = details;
		lastState = state;
		ipcClient.updateActivity(details, state, startEpoch);
	}

	private static void shutdown() {
		if (ipcClient != null) {
			ipcClient.close();
		}
	}

	private static String formatName(ResourceLocation id) {
		return capitalizeWords(id.getPath().replace('_', ' ').replace('/', ' '));
	}

	private static String formatStage(SanityStage stage) {
		return capitalizeWords(stage.name().toLowerCase(Locale.ROOT).replace('_', ' '));
	}

	private static String capitalizeWords(String raw) {
		String[] parts = raw.split(" ");
		StringBuilder builder = new StringBuilder();
		for (String part : parts) {
			if (part.isBlank()) {
				continue;
			}
			if (!builder.isEmpty()) {
				builder.append(' ');
			}
			builder.append(Character.toUpperCase(part.charAt(0)));
			if (part.length() > 1) {
				builder.append(part.substring(1));
			}
		}
		return builder.isEmpty() ? raw : builder.toString();
	}

	private static class DiscordIpcClient {
		private static final int OP_HANDSHAKE = 0;
		private static final int OP_FRAME = 1;
		private final String applicationId;
		private RandomAccessFile pipe;
		private long nextConnectAttemptMs = 0L;
		private boolean handshakeDone = false;

		private DiscordIpcClient(String applicationId) {
			this.applicationId = applicationId;
		}

		private void updateActivity(String details, String state, long startEpoch) {
			try {
				ensureConnected();
				if (pipe == null || !handshakeDone) {
					return;
				}

				JsonObject payload = new JsonObject();
				payload.addProperty("cmd", "SET_ACTIVITY");
				payload.addProperty("nonce", UUID.randomUUID().toString());

				JsonObject args = new JsonObject();
				args.addProperty("pid", ProcessHandle.current().pid());

				JsonObject activity = new JsonObject();
				activity.addProperty("details", details);
				activity.addProperty("state", state);
				activity.addProperty("instance", false);

				JsonObject timestamps = new JsonObject();
				timestamps.addProperty("start", startEpoch);
				activity.add("timestamps", timestamps);

				JsonObject assets = new JsonObject();
				assets.addProperty("large_image", "sanitycraft_logo");
				assets.addProperty("large_text", "SanityCraft");
				activity.add("assets", assets);

				args.add("activity", activity);
				payload.add("args", args);
				sendPacket(OP_FRAME, payload.toString().getBytes(StandardCharsets.UTF_8));
			} catch (Exception exception) {
				disconnect();
			}
		}

		private void ensureConnected() {
			if (pipe != null && handshakeDone) {
				return;
			}
			long now = System.currentTimeMillis();
			if (now < nextConnectAttemptMs) {
				return;
			}
			nextConnectAttemptMs = now + 10000L;
			disconnect();

			String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
			if (!osName.contains("win")) {
				return;
			}

			for (int i = 0; i < 10; i++) {
				String path = "\\\\.\\pipe\\discord-ipc-" + i;
				try {
					pipe = new RandomAccessFile(path, "rw");
					doHandshake();
					SanitycraftMod.LOGGER.info("Discord IPC connected on {}", path);
					return;
				} catch (Exception ignored) {
					disconnect();
				}
			}
		}

		private void doHandshake() throws IOException {
			JsonObject handshake = new JsonObject();
			handshake.addProperty("v", 1);
			handshake.addProperty("client_id", applicationId);
			sendPacket(OP_HANDSHAKE, handshake.toString().getBytes(StandardCharsets.UTF_8));
			handshakeDone = true;
		}

		private void sendPacket(int op, byte[] payload) throws IOException {
			if (pipe == null) {
				return;
			}
			writeIntLE(op);
			writeIntLE(payload.length);
			pipe.write(payload);
		}

		private void writeIntLE(int value) throws IOException {
			pipe.write(value & 0xFF);
			pipe.write((value >> 8) & 0xFF);
			pipe.write((value >> 16) & 0xFF);
			pipe.write((value >> 24) & 0xFF);
		}

		private void disconnect() {
			handshakeDone = false;
			if (pipe != null) {
				try {
					pipe.close();
				} catch (IOException ignored) {
				}
			}
			pipe = null;
		}

		private void close() {
			disconnect();
		}
	}
}
