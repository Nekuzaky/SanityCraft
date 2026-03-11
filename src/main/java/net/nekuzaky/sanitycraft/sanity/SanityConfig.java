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
	public boolean contextualDecayEnabled = true;
	public float nightDecayMultiplier = 1.15F;
	public float rainDecayMultiplier = 1.10F;
	public float undergroundDecayMultiplier = 1.25F;
	public int pillSanityGain = 20;
	public int pillCooldownTicks = 60;
	public boolean ambientFogEnabled = true;
	public int ambientFogBaseAlpha = 8;
	public int ambientFogMaxAlpha = 42;
	public boolean cinematicCaveFogEnabled = true;
	public int cinematicCaveFogBonusAlpha = 16;
	public boolean torchRepelsFog = true;
	public float heldTorchFogRepel = 0.45F;
	public float nearbyTorchFogRepel = 0.30F;
	public boolean torchHandLightEnabled = true;
	public int torchHandLightVisionDurationTicks = 60;
	public boolean ambienceDirectorEnabled = true;
	public float ambienceMasterVolume = 1.0F;
	public boolean ambienceHeartbeatEnabled = true;
	public boolean ambienceSoundTrapsEnabled = true;
	public int ambienceMinSanityActive = 80;
	public boolean nearMissEnabled = true;
	public int nearMissMinIntervalTicks = 1800;
	public int nearMissMaxIntervalTicks = 5200;
	public boolean caveMiningHallucinationEnabled = true;
	public int caveMiningMinIntervalTicks = 2600;
	public int caveMiningMaxIntervalTicks = 7600;

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
	public boolean zeroSanityDeathEnabled = true;
	public int zeroSanityDeathDelaySeconds = 30;

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
			config.contextualDecayEnabled = readBoolean(object, "contextualDecayEnabled", config.contextualDecayEnabled);
			config.nightDecayMultiplier = readFloat(object, "nightDecayMultiplier", config.nightDecayMultiplier);
			config.rainDecayMultiplier = readFloat(object, "rainDecayMultiplier", config.rainDecayMultiplier);
			config.undergroundDecayMultiplier = readFloat(object, "undergroundDecayMultiplier", config.undergroundDecayMultiplier);
			config.pillSanityGain = readInt(object, "pillSanityGain", config.pillSanityGain);
			config.pillCooldownTicks = readInt(object, "pillCooldownTicks", config.pillCooldownTicks);
			config.ambientFogEnabled = readBoolean(object, "ambientFogEnabled", config.ambientFogEnabled);
			config.ambientFogBaseAlpha = readInt(object, "ambientFogBaseAlpha", config.ambientFogBaseAlpha);
			config.ambientFogMaxAlpha = readInt(object, "ambientFogMaxAlpha", config.ambientFogMaxAlpha);
			config.cinematicCaveFogEnabled = readBoolean(object, "cinematicCaveFogEnabled", config.cinematicCaveFogEnabled);
			config.cinematicCaveFogBonusAlpha = readInt(object, "cinematicCaveFogBonusAlpha", config.cinematicCaveFogBonusAlpha);
			config.torchRepelsFog = readBoolean(object, "torchRepelsFog", config.torchRepelsFog);
			config.heldTorchFogRepel = readFloat(object, "heldTorchFogRepel", config.heldTorchFogRepel);
			config.nearbyTorchFogRepel = readFloat(object, "nearbyTorchFogRepel", config.nearbyTorchFogRepel);
			config.torchHandLightEnabled = readBoolean(object, "torchHandLightEnabled", config.torchHandLightEnabled);
			config.torchHandLightVisionDurationTicks = readInt(object, "torchHandLightVisionDurationTicks", config.torchHandLightVisionDurationTicks);
			config.ambienceDirectorEnabled = readBoolean(object, "ambienceDirectorEnabled", config.ambienceDirectorEnabled);
			config.ambienceMasterVolume = readFloat(object, "ambienceMasterVolume", config.ambienceMasterVolume);
			config.ambienceHeartbeatEnabled = readBoolean(object, "ambienceHeartbeatEnabled", config.ambienceHeartbeatEnabled);
			config.ambienceSoundTrapsEnabled = readBoolean(object, "ambienceSoundTrapsEnabled", config.ambienceSoundTrapsEnabled);
			config.ambienceMinSanityActive = readInt(object, "ambienceMinSanityActive", config.ambienceMinSanityActive);
			config.nearMissEnabled = readBoolean(object, "nearMissEnabled", config.nearMissEnabled);
			config.nearMissMinIntervalTicks = readInt(object, "nearMissMinIntervalTicks", config.nearMissMinIntervalTicks);
			config.nearMissMaxIntervalTicks = readInt(object, "nearMissMaxIntervalTicks", config.nearMissMaxIntervalTicks);
			config.caveMiningHallucinationEnabled = readBoolean(object, "caveMiningHallucinationEnabled", config.caveMiningHallucinationEnabled);
			config.caveMiningMinIntervalTicks = readInt(object, "caveMiningMinIntervalTicks", config.caveMiningMinIntervalTicks);
			config.caveMiningMaxIntervalTicks = readInt(object, "caveMiningMaxIntervalTicks", config.caveMiningMaxIntervalTicks);
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
			config.zeroSanityDeathEnabled = readBoolean(object, "zeroSanityDeathEnabled", config.zeroSanityDeathEnabled);
			config.zeroSanityDeathDelaySeconds = readInt(object, "zeroSanityDeathDelaySeconds", config.zeroSanityDeathDelaySeconds);
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
		object.addProperty("contextualDecayEnabled", contextualDecayEnabled);
		object.addProperty("nightDecayMultiplier", nightDecayMultiplier);
		object.addProperty("rainDecayMultiplier", rainDecayMultiplier);
		object.addProperty("undergroundDecayMultiplier", undergroundDecayMultiplier);
		object.addProperty("pillSanityGain", pillSanityGain);
		object.addProperty("pillCooldownTicks", pillCooldownTicks);
		object.addProperty("ambientFogEnabled", ambientFogEnabled);
		object.addProperty("ambientFogBaseAlpha", ambientFogBaseAlpha);
		object.addProperty("ambientFogMaxAlpha", ambientFogMaxAlpha);
		object.addProperty("cinematicCaveFogEnabled", cinematicCaveFogEnabled);
		object.addProperty("cinematicCaveFogBonusAlpha", cinematicCaveFogBonusAlpha);
		object.addProperty("torchRepelsFog", torchRepelsFog);
		object.addProperty("heldTorchFogRepel", heldTorchFogRepel);
		object.addProperty("nearbyTorchFogRepel", nearbyTorchFogRepel);
		object.addProperty("torchHandLightEnabled", torchHandLightEnabled);
		object.addProperty("torchHandLightVisionDurationTicks", torchHandLightVisionDurationTicks);
		object.addProperty("ambienceDirectorEnabled", ambienceDirectorEnabled);
		object.addProperty("ambienceMasterVolume", ambienceMasterVolume);
		object.addProperty("ambienceHeartbeatEnabled", ambienceHeartbeatEnabled);
		object.addProperty("ambienceSoundTrapsEnabled", ambienceSoundTrapsEnabled);
		object.addProperty("ambienceMinSanityActive", ambienceMinSanityActive);
		object.addProperty("nearMissEnabled", nearMissEnabled);
		object.addProperty("nearMissMinIntervalTicks", nearMissMinIntervalTicks);
		object.addProperty("nearMissMaxIntervalTicks", nearMissMaxIntervalTicks);
		object.addProperty("caveMiningHallucinationEnabled", caveMiningHallucinationEnabled);
		object.addProperty("caveMiningMinIntervalTicks", caveMiningMinIntervalTicks);
		object.addProperty("caveMiningMaxIntervalTicks", caveMiningMaxIntervalTicks);
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
		object.addProperty("zeroSanityDeathEnabled", zeroSanityDeathEnabled);
		object.addProperty("zeroSanityDeathDelaySeconds", zeroSanityDeathDelaySeconds);
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
