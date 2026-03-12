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

	public int updateIntervalSeconds = 3;
	public int sanityOnRespawnAfterDeath = 100;
	public int horrorEventsPerMinute = 18;
	public int horrorGlobalCooldownMinTicks = 14;
	public int horrorGlobalCooldownMaxTicks = 38;
	public int maxDirectedParticlesPerBurst = 32;
	public int networkEffectPacketsPerMinute = 38;
	public int networkEffectMinSpacingTicks = 3;

	public int darknessLoss = 3;
	public int caveLoss = 2;
	public int hostileLoss = 3;
	public int thunderLoss = 2;
	public int deepDarkLoss = 5;
	public int anomalyLoss = 3;

	public int sleepGain = 2;
	public int villageGain = 1;
	public int lightGain = 1;
	public int musicGain = 2;

	public int hostileRadius = 12;
	public int villageRadius = 28;
	public int lightRadius = 6;
	public int musicRadius = 10;
	public String balanceProfile = "hardcore";
	public boolean contextualDecayEnabled = true;
	public float nightDecayMultiplier = 1.25F;
	public float rainDecayMultiplier = 1.20F;
	public float undergroundDecayMultiplier = 1.35F;
	public int pillSanityGain = 20;
	public int pillCooldownTicks = 60;
	public boolean ambientFogEnabled = true;
	public int ambientFogBaseAlpha = 10;
	public int ambientFogMaxAlpha = 52;
	public boolean dreadFogEnabled = true;
	public float dreadFogIntensity = 1.0F;
	public boolean dreadFogNightBoost = true;
	public boolean cinematicCaveFogEnabled = true;
	public int cinematicCaveFogBonusAlpha = 20;
	public boolean torchRepelsFog = true;
	public float heldTorchFogRepel = 0.35F;
	public float nearbyTorchFogRepel = 0.22F;
	public boolean torchHandLightEnabled = true;
	public int torchHandLightVisionDurationTicks = 60;
	public boolean ambienceDirectorEnabled = true;
	public float ambienceMasterVolume = 1.0F;
	public boolean ambienceHeartbeatEnabled = true;
	public boolean ambienceSoundTrapsEnabled = true;
	public int ambienceMinSanityActive = 90;
	public boolean nearMissEnabled = true;
	public int nearMissMinIntervalTicks = 1200;
	public int nearMissMaxIntervalTicks = 3400;
	public boolean caveMiningHallucinationEnabled = true;
	public int caveMiningMinIntervalTicks = 1800;
	public int caveMiningMaxIntervalTicks = 5200;

	public boolean hallucinationsEnabled = true;
	public boolean narrativeEventsEnabled = true;
	public boolean discordPresenceEnabled = true;
	public String discordApplicationId = "1481286526951358486";
	public boolean streamerSafeMode = false;
	public boolean falseUiEventsEnabled = true;
	public float falseUiEventChance = 0.08F;
	public float uneasyWhisperChance = 0.28F;
	public float narrativeWhisperChance = 0.22F;
	public float narrativeFootstepChance = 0.12F;
	public float narrativeJumpscareChance = 0.025F;
	public boolean stalkerHallucinationEnabled = true;
	public int stalkerSpawnChancePercent = 55;
	public int stalkerLifetimeSeconds = 22;
	public int stalkerMinSpawnDistance = 7;
	public int stalkerMaxSpawnDistance = 15;

	public boolean bloodyCreeperHallucinationEnabled = true;
	public int bloodyCreeperSpawnChancePercent = 33;
	public int bloodyCreeperLifetimeSeconds = 16;
	public boolean ritualSafeZoneEnabled = true;
	public int ritualSafeZoneGain = 2;
	public boolean nightmareSleepEnabled = true;
	public int nightmareSleepThreshold = 45;
	public int nightmareSleepPenalty = 10;
	public int nightmareSleepMinorRecovery = 2;
	public float paranoiaMimicChance = 0.10F;
	public boolean paranoiaMimicEnabled = true;
	public boolean sanityAfflictionsEnabled = true;
	public boolean corruptedLootMomentsEnabled = true;
	public float corruptedLootMomentChance = 0.12F;
	public boolean partyStressLinkEnabled = true;
	public int partyStressRadius = 18;
	public int partyStressThreshold = 35;
	public int partyStressLossPerPlayer = 1;
	public int partyStressMaxLoss = 3;
	public boolean fractureQuestsEnabled = true;
	public int fractureQuestTriggerSanity = 55;
	public int fractureQuestReward = 6;
	public boolean biomePersonalityEnabled = true;
	public float biomePersonalityChance = 0.10F;
	public boolean mentalShieldEnabled = true;
	public int mentalShieldDurationSeconds = 90;
	public float accessibilityVisualIntensity = 1.0F;
	public float accessibilityAudioIntensity = 1.0F;
	public boolean noHardFlashes = false;
	public boolean zeroSanityDeathEnabled = true;
	public int zeroSanityDeathDelaySeconds = 30;

	public static SanityConfig loadOrCreate() {
		SanityConfig config = new SanityConfig();
		if (!Files.exists(CONFIG_PATH)) {
			config.applyBalanceProfile();
			config.save();
			return config;
		}

		try {
			String json = Files.readString(CONFIG_PATH);
			JsonObject object = new JsonParser().parse(json).getAsJsonObject();
			config.updateIntervalSeconds = readInt(object, "updateIntervalSeconds", config.updateIntervalSeconds);
			config.sanityOnRespawnAfterDeath = readInt(object, "sanityOnRespawnAfterDeath", config.sanityOnRespawnAfterDeath);
			config.horrorEventsPerMinute = readInt(object, "horrorEventsPerMinute", config.horrorEventsPerMinute);
			config.horrorGlobalCooldownMinTicks = readInt(object, "horrorGlobalCooldownMinTicks", config.horrorGlobalCooldownMinTicks);
			config.horrorGlobalCooldownMaxTicks = readInt(object, "horrorGlobalCooldownMaxTicks", config.horrorGlobalCooldownMaxTicks);
			config.maxDirectedParticlesPerBurst = readInt(object, "maxDirectedParticlesPerBurst", config.maxDirectedParticlesPerBurst);
			config.networkEffectPacketsPerMinute = readInt(object, "networkEffectPacketsPerMinute", config.networkEffectPacketsPerMinute);
			config.networkEffectMinSpacingTicks = readInt(object, "networkEffectMinSpacingTicks", config.networkEffectMinSpacingTicks);
			config.darknessLoss = readInt(object, "darknessLoss", config.darknessLoss);
			config.caveLoss = readInt(object, "caveLoss", config.caveLoss);
			config.hostileLoss = readInt(object, "hostileLoss", config.hostileLoss);
			config.thunderLoss = readInt(object, "thunderLoss", config.thunderLoss);
			config.deepDarkLoss = readInt(object, "deepDarkLoss", config.deepDarkLoss);
			config.anomalyLoss = readInt(object, "anomalyLoss", config.anomalyLoss);
			config.sleepGain = readInt(object, "sleepGain", config.sleepGain);
			config.villageGain = readInt(object, "villageGain", config.villageGain);
			config.lightGain = readInt(object, "lightGain", config.lightGain);
			config.musicGain = readInt(object, "musicGain", config.musicGain);
			config.hostileRadius = readInt(object, "hostileRadius", config.hostileRadius);
			config.villageRadius = readInt(object, "villageRadius", config.villageRadius);
			config.lightRadius = readInt(object, "lightRadius", config.lightRadius);
			config.musicRadius = readInt(object, "musicRadius", config.musicRadius);
			config.balanceProfile = readString(object, "balanceProfile", config.balanceProfile);
			config.contextualDecayEnabled = readBoolean(object, "contextualDecayEnabled", config.contextualDecayEnabled);
			config.nightDecayMultiplier = readFloat(object, "nightDecayMultiplier", config.nightDecayMultiplier);
			config.rainDecayMultiplier = readFloat(object, "rainDecayMultiplier", config.rainDecayMultiplier);
			config.undergroundDecayMultiplier = readFloat(object, "undergroundDecayMultiplier", config.undergroundDecayMultiplier);
			config.pillSanityGain = readInt(object, "pillSanityGain", config.pillSanityGain);
			config.pillCooldownTicks = readInt(object, "pillCooldownTicks", config.pillCooldownTicks);
			config.ambientFogEnabled = readBoolean(object, "ambientFogEnabled", config.ambientFogEnabled);
			config.ambientFogBaseAlpha = readInt(object, "ambientFogBaseAlpha", config.ambientFogBaseAlpha);
			config.ambientFogMaxAlpha = readInt(object, "ambientFogMaxAlpha", config.ambientFogMaxAlpha);
			config.dreadFogEnabled = readBoolean(object, "dreadFogEnabled", config.dreadFogEnabled);
			config.dreadFogIntensity = readFloat(object, "dreadFogIntensity", config.dreadFogIntensity);
			config.dreadFogNightBoost = readBoolean(object, "dreadFogNightBoost", config.dreadFogNightBoost);
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
			config.discordPresenceEnabled = readBoolean(object, "discordPresenceEnabled", config.discordPresenceEnabled);
			config.discordApplicationId = readString(object, "discordApplicationId", config.discordApplicationId);
			config.streamerSafeMode = readBoolean(object, "streamerSafeMode", config.streamerSafeMode);
			config.falseUiEventsEnabled = readBoolean(object, "falseUiEventsEnabled", config.falseUiEventsEnabled);
			config.falseUiEventChance = readFloat(object, "falseUiEventChance", config.falseUiEventChance);
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
			config.ritualSafeZoneEnabled = readBoolean(object, "ritualSafeZoneEnabled", config.ritualSafeZoneEnabled);
			config.ritualSafeZoneGain = readInt(object, "ritualSafeZoneGain", config.ritualSafeZoneGain);
			config.nightmareSleepEnabled = readBoolean(object, "nightmareSleepEnabled", config.nightmareSleepEnabled);
			config.nightmareSleepThreshold = readInt(object, "nightmareSleepThreshold", config.nightmareSleepThreshold);
			config.nightmareSleepPenalty = readInt(object, "nightmareSleepPenalty", config.nightmareSleepPenalty);
			config.nightmareSleepMinorRecovery = readInt(object, "nightmareSleepMinorRecovery", config.nightmareSleepMinorRecovery);
			config.paranoiaMimicEnabled = readBoolean(object, "paranoiaMimicEnabled", config.paranoiaMimicEnabled);
			config.paranoiaMimicChance = readFloat(object, "paranoiaMimicChance", config.paranoiaMimicChance);
			config.sanityAfflictionsEnabled = readBoolean(object, "sanityAfflictionsEnabled", config.sanityAfflictionsEnabled);
			config.corruptedLootMomentsEnabled = readBoolean(object, "corruptedLootMomentsEnabled", config.corruptedLootMomentsEnabled);
			config.corruptedLootMomentChance = readFloat(object, "corruptedLootMomentChance", config.corruptedLootMomentChance);
			config.partyStressLinkEnabled = readBoolean(object, "partyStressLinkEnabled", config.partyStressLinkEnabled);
			config.partyStressRadius = readInt(object, "partyStressRadius", config.partyStressRadius);
			config.partyStressThreshold = readInt(object, "partyStressThreshold", config.partyStressThreshold);
			config.partyStressLossPerPlayer = readInt(object, "partyStressLossPerPlayer", config.partyStressLossPerPlayer);
			config.partyStressMaxLoss = readInt(object, "partyStressMaxLoss", config.partyStressMaxLoss);
			config.fractureQuestsEnabled = readBoolean(object, "fractureQuestsEnabled", config.fractureQuestsEnabled);
			config.fractureQuestTriggerSanity = readInt(object, "fractureQuestTriggerSanity", config.fractureQuestTriggerSanity);
			config.fractureQuestReward = readInt(object, "fractureQuestReward", config.fractureQuestReward);
			config.biomePersonalityEnabled = readBoolean(object, "biomePersonalityEnabled", config.biomePersonalityEnabled);
			config.biomePersonalityChance = readFloat(object, "biomePersonalityChance", config.biomePersonalityChance);
			config.mentalShieldEnabled = readBoolean(object, "mentalShieldEnabled", config.mentalShieldEnabled);
			config.mentalShieldDurationSeconds = readInt(object, "mentalShieldDurationSeconds", config.mentalShieldDurationSeconds);
			config.accessibilityVisualIntensity = readFloat(object, "accessibilityVisualIntensity", config.accessibilityVisualIntensity);
			config.accessibilityAudioIntensity = readFloat(object, "accessibilityAudioIntensity", config.accessibilityAudioIntensity);
			config.noHardFlashes = readBoolean(object, "noHardFlashes", config.noHardFlashes);
			config.zeroSanityDeathEnabled = readBoolean(object, "zeroSanityDeathEnabled", config.zeroSanityDeathEnabled);
			config.zeroSanityDeathDelaySeconds = readInt(object, "zeroSanityDeathDelaySeconds", config.zeroSanityDeathDelaySeconds);
			config.applyBalanceProfile();
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
		object.addProperty("horrorEventsPerMinute", horrorEventsPerMinute);
		object.addProperty("horrorGlobalCooldownMinTicks", horrorGlobalCooldownMinTicks);
		object.addProperty("horrorGlobalCooldownMaxTicks", horrorGlobalCooldownMaxTicks);
		object.addProperty("maxDirectedParticlesPerBurst", maxDirectedParticlesPerBurst);
		object.addProperty("networkEffectPacketsPerMinute", networkEffectPacketsPerMinute);
		object.addProperty("networkEffectMinSpacingTicks", networkEffectMinSpacingTicks);
		object.addProperty("darknessLoss", darknessLoss);
		object.addProperty("caveLoss", caveLoss);
		object.addProperty("hostileLoss", hostileLoss);
		object.addProperty("thunderLoss", thunderLoss);
		object.addProperty("deepDarkLoss", deepDarkLoss);
		object.addProperty("anomalyLoss", anomalyLoss);
		object.addProperty("sleepGain", sleepGain);
		object.addProperty("villageGain", villageGain);
		object.addProperty("lightGain", lightGain);
		object.addProperty("musicGain", musicGain);
		object.addProperty("hostileRadius", hostileRadius);
		object.addProperty("villageRadius", villageRadius);
		object.addProperty("lightRadius", lightRadius);
		object.addProperty("musicRadius", musicRadius);
		object.addProperty("balanceProfile", balanceProfile);
		object.addProperty("contextualDecayEnabled", contextualDecayEnabled);
		object.addProperty("nightDecayMultiplier", nightDecayMultiplier);
		object.addProperty("rainDecayMultiplier", rainDecayMultiplier);
		object.addProperty("undergroundDecayMultiplier", undergroundDecayMultiplier);
		object.addProperty("pillSanityGain", pillSanityGain);
		object.addProperty("pillCooldownTicks", pillCooldownTicks);
		object.addProperty("ambientFogEnabled", ambientFogEnabled);
		object.addProperty("ambientFogBaseAlpha", ambientFogBaseAlpha);
		object.addProperty("ambientFogMaxAlpha", ambientFogMaxAlpha);
		object.addProperty("dreadFogEnabled", dreadFogEnabled);
		object.addProperty("dreadFogIntensity", dreadFogIntensity);
		object.addProperty("dreadFogNightBoost", dreadFogNightBoost);
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
		object.addProperty("discordPresenceEnabled", discordPresenceEnabled);
		object.addProperty("discordApplicationId", discordApplicationId);
		object.addProperty("streamerSafeMode", streamerSafeMode);
		object.addProperty("falseUiEventsEnabled", falseUiEventsEnabled);
		object.addProperty("falseUiEventChance", falseUiEventChance);
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
		object.addProperty("ritualSafeZoneEnabled", ritualSafeZoneEnabled);
		object.addProperty("ritualSafeZoneGain", ritualSafeZoneGain);
		object.addProperty("nightmareSleepEnabled", nightmareSleepEnabled);
		object.addProperty("nightmareSleepThreshold", nightmareSleepThreshold);
		object.addProperty("nightmareSleepPenalty", nightmareSleepPenalty);
		object.addProperty("nightmareSleepMinorRecovery", nightmareSleepMinorRecovery);
		object.addProperty("paranoiaMimicEnabled", paranoiaMimicEnabled);
		object.addProperty("paranoiaMimicChance", paranoiaMimicChance);
		object.addProperty("sanityAfflictionsEnabled", sanityAfflictionsEnabled);
		object.addProperty("corruptedLootMomentsEnabled", corruptedLootMomentsEnabled);
		object.addProperty("corruptedLootMomentChance", corruptedLootMomentChance);
		object.addProperty("partyStressLinkEnabled", partyStressLinkEnabled);
		object.addProperty("partyStressRadius", partyStressRadius);
		object.addProperty("partyStressThreshold", partyStressThreshold);
		object.addProperty("partyStressLossPerPlayer", partyStressLossPerPlayer);
		object.addProperty("partyStressMaxLoss", partyStressMaxLoss);
		object.addProperty("fractureQuestsEnabled", fractureQuestsEnabled);
		object.addProperty("fractureQuestTriggerSanity", fractureQuestTriggerSanity);
		object.addProperty("fractureQuestReward", fractureQuestReward);
		object.addProperty("biomePersonalityEnabled", biomePersonalityEnabled);
		object.addProperty("biomePersonalityChance", biomePersonalityChance);
		object.addProperty("mentalShieldEnabled", mentalShieldEnabled);
		object.addProperty("mentalShieldDurationSeconds", mentalShieldDurationSeconds);
		object.addProperty("accessibilityVisualIntensity", accessibilityVisualIntensity);
		object.addProperty("accessibilityAudioIntensity", accessibilityAudioIntensity);
		object.addProperty("noHardFlashes", noHardFlashes);
		object.addProperty("zeroSanityDeathEnabled", zeroSanityDeathEnabled);
		object.addProperty("zeroSanityDeathDelaySeconds", zeroSanityDeathDelaySeconds);
		return object;
	}

	private void applyBalanceProfile() {
		String profile = balanceProfile == null ? "custom" : balanceProfile.trim().toLowerCase();
		if ("light".equals(profile)) {
			updateIntervalSeconds = 5;
			darknessLoss = 1;
			caveLoss = 1;
			hostileLoss = 1;
			thunderLoss = 1;
			deepDarkLoss = 2;
			anomalyLoss = 1;
			sleepGain = 5;
			villageGain = 3;
			lightGain = 3;
			musicGain = 4;
			nightDecayMultiplier = 1.05F;
			rainDecayMultiplier = 1.05F;
			undergroundDecayMultiplier = 1.10F;
			horrorEventsPerMinute = 10;
			horrorGlobalCooldownMinTicks = 24;
			horrorGlobalCooldownMaxTicks = 60;
			maxDirectedParticlesPerBurst = 16;
			networkEffectPacketsPerMinute = 22;
			networkEffectMinSpacingTicks = 5;
			stalkerSpawnChancePercent = 20;
			bloodyCreeperSpawnChancePercent = 10;
			nearMissMinIntervalTicks = 2600;
			nearMissMaxIntervalTicks = 6200;
			caveMiningMinIntervalTicks = 3600;
			caveMiningMaxIntervalTicks = 8600;
		} else if ("medium".equals(profile)) {
			updateIntervalSeconds = 4;
			darknessLoss = 2;
			caveLoss = 1;
			hostileLoss = 2;
			thunderLoss = 1;
			deepDarkLoss = 3;
			anomalyLoss = 2;
			sleepGain = 4;
			villageGain = 2;
			lightGain = 2;
			musicGain = 3;
			nightDecayMultiplier = 1.15F;
			rainDecayMultiplier = 1.10F;
			undergroundDecayMultiplier = 1.25F;
			horrorEventsPerMinute = 14;
			horrorGlobalCooldownMinTicks = 18;
			horrorGlobalCooldownMaxTicks = 52;
			maxDirectedParticlesPerBurst = 24;
			networkEffectPacketsPerMinute = 30;
			networkEffectMinSpacingTicks = 4;
			stalkerSpawnChancePercent = 35;
			bloodyCreeperSpawnChancePercent = 20;
			nearMissMinIntervalTicks = 1800;
			nearMissMaxIntervalTicks = 5200;
			caveMiningMinIntervalTicks = 2600;
			caveMiningMaxIntervalTicks = 7600;
		} else if ("hardcore".equals(profile)) {
			updateIntervalSeconds = 3;
			darknessLoss = 3;
			caveLoss = 2;
			hostileLoss = 3;
			thunderLoss = 2;
			deepDarkLoss = 5;
			anomalyLoss = 3;
			sleepGain = 2;
			villageGain = 1;
			lightGain = 1;
			musicGain = 2;
			nightDecayMultiplier = 1.25F;
			rainDecayMultiplier = 1.20F;
			undergroundDecayMultiplier = 1.35F;
			horrorEventsPerMinute = 18;
			horrorGlobalCooldownMinTicks = 14;
			horrorGlobalCooldownMaxTicks = 38;
			maxDirectedParticlesPerBurst = 32;
			networkEffectPacketsPerMinute = 38;
			networkEffectMinSpacingTicks = 3;
			stalkerSpawnChancePercent = 55;
			bloodyCreeperSpawnChancePercent = 33;
			nearMissMinIntervalTicks = 1200;
			nearMissMaxIntervalTicks = 3400;
			caveMiningMinIntervalTicks = 1800;
			caveMiningMaxIntervalTicks = 5200;
		}
	}

	public boolean applyNamedProfile(String profileName) {
		if (profileName == null) {
			return false;
		}
		String profile = profileName.trim().toLowerCase();
		if (!"light".equals(profile) && !"medium".equals(profile) && !"hardcore".equals(profile) && !"custom".equals(profile)) {
			return false;
		}
		balanceProfile = profile;
		applyBalanceProfile();
		save();
		return true;
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

	private static String readString(JsonObject object, String key, String fallback) {
		return object.has(key) ? object.get(key).getAsString() : fallback;
	}
}
