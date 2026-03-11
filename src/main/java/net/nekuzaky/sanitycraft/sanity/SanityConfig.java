package net.nekuzaky.sanitycraft.sanity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.nekuzaky.sanitycraft.SanitycraftMod;

public class SanityConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("sanitycraft.json");

	public int updateIntervalSeconds = 5;
	public int sanityOnRespawnAfterDeath = 100;

	public int darknessLoss = 2;
	public int caveLoss = 1;
	public int hostileLoss = 2;
	public int thunderLoss = 1;
	public int deepDarkLoss = 3;

	public int sleepGain = 4;
	public int villageGain = 2;
	public int lightGain = 2;
	public int musicGain = 3;

	public int hostileRadius = 12;
	public int villageRadius = 28;
	public int lightRadius = 6;
	public int musicRadius = 10;
	public int pillSanityGain = 20;
	public int pillCooldownTicks = 60;

	public boolean hallucinationsEnabled = true;
	public boolean narrativeEventsEnabled = true;
	public float uneasyWhisperChance = 0.18F;
	public float narrativeWhisperChance = 0.16F;
	public float narrativeFootstepChance = 0.08F;
	public float narrativeJumpscareChance = 0.015F;
	public boolean stalkerHallucinationEnabled = true;
	public int stalkerSpawnChancePercent = 35;
	public int stalkerLifetimeSeconds = 18;
	public int stalkerMinSpawnDistance = 8;
	public int stalkerMaxSpawnDistance = 14;

	public boolean bloodyCreeperHallucinationEnabled = true;
	public int bloodyCreeperSpawnChancePercent = 20;
	public int bloodyCreeperLifetimeSeconds = 14;

	public static SanityConfig loadOrCreate() {
		SanityConfig config = new SanityConfig();
		if (!Files.exists(CONFIG_PATH)) {
			config.save();
			return config;
		}

		try {
			String json = Files.readString(CONFIG_PATH);
			JsonObject object = JsonParser.parseString(json).getAsJsonObject();
			config.updateIntervalSeconds = readInt(object, "updateIntervalSeconds", config.updateIntervalSeconds);
			config.sanityOnRespawnAfterDeath = readInt(object, "sanityOnRespawnAfterDeath", config.sanityOnRespawnAfterDeath);
			config.darknessLoss = readInt(object, "darknessLoss", config.darknessLoss);
			config.caveLoss = readInt(object, "caveLoss", config.caveLoss);
			config.hostileLoss = readInt(object, "hostileLoss", config.hostileLoss);
			config.thunderLoss = readInt(object, "thunderLoss", config.thunderLoss);
			config.deepDarkLoss = readInt(object, "deepDarkLoss", config.deepDarkLoss);
			config.sleepGain = readInt(object, "sleepGain", config.sleepGain);
			config.villageGain = readInt(object, "villageGain", config.villageGain);
			config.lightGain = readInt(object, "lightGain", config.lightGain);
			config.musicGain = readInt(object, "musicGain", config.musicGain);
			config.hostileRadius = readInt(object, "hostileRadius", config.hostileRadius);
			config.villageRadius = readInt(object, "villageRadius", config.villageRadius);
			config.lightRadius = readInt(object, "lightRadius", config.lightRadius);
			config.musicRadius = readInt(object, "musicRadius", config.musicRadius);
			config.pillSanityGain = readInt(object, "pillSanityGain", config.pillSanityGain);
			config.pillCooldownTicks = readInt(object, "pillCooldownTicks", config.pillCooldownTicks);
			config.hallucinationsEnabled = readBoolean(object, "hallucinationsEnabled", config.hallucinationsEnabled);
			config.narrativeEventsEnabled = readBoolean(object, "narrativeEventsEnabled", config.narrativeEventsEnabled);
			config.uneasyWhisperChance = readFloat(object, "uneasyWhisperChance", config.uneasyWhisperChance);
			config.narrativeWhisperChance = readFloat(object, "narrativeWhisperChance", config.narrativeWhisperChance);
			config.narrativeFootstepChance = readFloat(object, "narrativeFootstepChance", config.narrativeFootstepChance);
			config.narrativeJumpscareChance = readFloat(object, "narrativeJumpscareChance", config.narrativeJumpscareChance);
			config.stalkerHallucinationEnabled = readBoolean(object, "stalkerHallucinationEnabled", config.stalkerHallucinationEnabled);
			config.stalkerSpawnChancePercent = readInt(object, "stalkerSpawnChancePercent", config.stalkerSpawnChancePercent);
			config.stalkerLifetimeSeconds = readInt(object, "stalkerLifetimeSeconds", config.stalkerLifetimeSeconds);
			config.stalkerMinSpawnDistance = readInt(object, "stalkerMinSpawnDistance", config.stalkerMinSpawnDistance);
			config.stalkerMaxSpawnDistance = readInt(object, "stalkerMaxSpawnDistance", config.stalkerMaxSpawnDistance);
			config.bloodyCreeperHallucinationEnabled = readBoolean(object, "bloodyCreeperHallucinationEnabled", config.bloodyCreeperHallucinationEnabled);
			config.bloodyCreeperSpawnChancePercent = readInt(object, "bloodyCreeperSpawnChancePercent", config.bloodyCreeperSpawnChancePercent);
			config.bloodyCreeperLifetimeSeconds = readInt(object, "bloodyCreeperLifetimeSeconds", config.bloodyCreeperLifetimeSeconds);
		} catch (Exception e) {
			SanitycraftMod.LOGGER.error("Failed to load sanitycraft config. Using defaults.", e);
		}
		return config;
	}

	public int getUpdateIntervalTicks() {
		return Math.max(1, updateIntervalSeconds) * 20;
	}

	public void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(CONFIG_PATH, GSON.toJson(toJson()));
		} catch (IOException e) {
			SanitycraftMod.LOGGER.error("Failed to save sanitycraft config.", e);
		}
	}

	private JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("updateIntervalSeconds", updateIntervalSeconds);
		object.addProperty("sanityOnRespawnAfterDeath", sanityOnRespawnAfterDeath);
		object.addProperty("darknessLoss", darknessLoss);
		object.addProperty("caveLoss", caveLoss);
		object.addProperty("hostileLoss", hostileLoss);
		object.addProperty("thunderLoss", thunderLoss);
		object.addProperty("deepDarkLoss", deepDarkLoss);
		object.addProperty("sleepGain", sleepGain);
		object.addProperty("villageGain", villageGain);
		object.addProperty("lightGain", lightGain);
		object.addProperty("musicGain", musicGain);
		object.addProperty("hostileRadius", hostileRadius);
		object.addProperty("villageRadius", villageRadius);
		object.addProperty("lightRadius", lightRadius);
		object.addProperty("musicRadius", musicRadius);
		object.addProperty("pillSanityGain", pillSanityGain);
		object.addProperty("pillCooldownTicks", pillCooldownTicks);
		object.addProperty("hallucinationsEnabled", hallucinationsEnabled);
		object.addProperty("narrativeEventsEnabled", narrativeEventsEnabled);
		object.addProperty("uneasyWhisperChance", uneasyWhisperChance);
		object.addProperty("narrativeWhisperChance", narrativeWhisperChance);
		object.addProperty("narrativeFootstepChance", narrativeFootstepChance);
		object.addProperty("narrativeJumpscareChance", narrativeJumpscareChance);
		object.addProperty("stalkerHallucinationEnabled", stalkerHallucinationEnabled);
		object.addProperty("stalkerSpawnChancePercent", stalkerSpawnChancePercent);
		object.addProperty("stalkerLifetimeSeconds", stalkerLifetimeSeconds);
		object.addProperty("stalkerMinSpawnDistance", stalkerMinSpawnDistance);
		object.addProperty("stalkerMaxSpawnDistance", stalkerMaxSpawnDistance);
		object.addProperty("bloodyCreeperHallucinationEnabled", bloodyCreeperHallucinationEnabled);
		object.addProperty("bloodyCreeperSpawnChancePercent", bloodyCreeperSpawnChancePercent);
		object.addProperty("bloodyCreeperLifetimeSeconds", bloodyCreeperLifetimeSeconds);
		return object;
	}

	private static int readInt(JsonObject object, String key, int fallback) {
		return object.has(key) ? object.get(key).getAsInt() : fallback;
	}

	private static boolean readBoolean(JsonObject object, String key, boolean fallback) {
		return object.has(key) ? object.get(key).getAsBoolean() : fallback;
	}

	private static float readFloat(JsonObject object, String key, float fallback) {
		return object.has(key) ? object.get(key).getAsFloat() : fallback;
	}
}
