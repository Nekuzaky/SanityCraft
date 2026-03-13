package com.sanitycraft.data.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sanitycraft.SanityCraft;
import com.sanitycraft.sanity.SanityDifficultyProfile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;

public final class SanityCraftConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("sanitycraft.json");
	private static SanityCraftConfig instance = defaults();

	public SanityDifficultyProfile profile = SanityDifficultyProfile.BALANCED;
	public Ticking ticking = new Ticking();
	public General general = new General();
	public Thresholds thresholds = new Thresholds();
	public Decay decay = new Decay();
	public Recovery recovery = new Recovery();
	public Sync sync = new Sync();
	public Events events = new Events();
	public Visuals visuals = new Visuals();
	public Multiplayer multiplayer = new Multiplayer();

	public static SanityCraftConfig get() {
		return instance;
	}

	public static SanityCraftConfig loadOrCreate() {
		if (!Files.exists(CONFIG_PATH)) {
			instance = defaults();
			instance.save();
			return instance;
		}

		try {
			SanityCraftConfig loaded = GSON.fromJson(Files.readString(CONFIG_PATH), SanityCraftConfig.class);
			instance = loaded == null ? defaults() : loaded.sanitize();
		} catch (Exception exception) {
			SanityCraft.LOGGER.error("Failed to load sanitycraft config, using defaults.", exception);
			instance = defaults();
		}
		return instance;
	}

	public void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(CONFIG_PATH, GSON.toJson(sanitize()));
		} catch (IOException exception) {
			SanityCraft.LOGGER.error("Failed to save sanitycraft config.", exception);
		}
	}

	private SanityCraftConfig sanitize() {
		if (profile == null) {
			profile = SanityDifficultyProfile.BALANCED;
		}
		if (general == null) {
			general = new General();
		}
		if (ticking == null) {
			ticking = new Ticking();
		}
		if (thresholds == null) {
			thresholds = new Thresholds();
		}
		if (decay == null) {
			decay = new Decay();
		}
		if (recovery == null) {
			recovery = new Recovery();
		}
		if (sync == null) {
			sync = new Sync();
		}
		if (events == null) {
			events = new Events();
		}
		if (visuals == null) {
			visuals = new Visuals();
		}
		if (multiplayer == null) {
			multiplayer = new Multiplayer();
		}
		ticking.updateIntervalTicks = Math.max(1, ticking.updateIntervalTicks);
		ticking.effectIntervalTicks = Math.max(1, ticking.effectIntervalTicks);
		general.respawnSanity = Mth.clamp(general.respawnSanity, 0, 100);
		if (thresholds.stableMin < thresholds.uneasyMin
				|| thresholds.uneasyMin < thresholds.disturbedMin
				|| thresholds.disturbedMin < thresholds.fracturedMin
				|| thresholds.fracturedMin < 1
				|| thresholds.stableMin > 100) {
			thresholds = new Thresholds();
		} else if (thresholds.stableMin == 76 && thresholds.uneasyMin == 51 && thresholds.disturbedMin == 26 && thresholds.fracturedMin == 11) {
			thresholds = new Thresholds();
		}
		decay.darknessLightThreshold = Mth.clamp(decay.darknessLightThreshold, 0, 15);
		decay.hostileRadius = Math.max(1, decay.hostileRadius);
		decay.isolationRadius = Math.max(1, decay.isolationRadius);
		decay.lowHealthThreshold = Mth.clamp(decay.lowHealthThreshold, 0.05F, 1.0F);
		decay.lowFoodThreshold = Mth.clamp(decay.lowFoodThreshold, 1, 20);
		decay.darknessLoss = Math.max(0, decay.darknessLoss);
		decay.hostileLoss = Math.max(0, decay.hostileLoss);
		decay.isolationLoss = Math.max(0, decay.isolationLoss);
		decay.nightLoss = Math.max(0, decay.nightLoss);
		decay.undergroundLoss = Math.max(0, decay.undergroundLoss);
		decay.lowHealthLoss = Math.max(0, decay.lowHealthLoss);
		decay.lowFoodLoss = Math.max(0, decay.lowFoodLoss);
		decay.weatherLoss = Math.max(0, decay.weatherLoss);
		decay.disturbedStagePenalty = Math.max(0, decay.disturbedStagePenalty);
		decay.fracturedStagePenalty = Math.max(0, decay.fracturedStagePenalty);
		decay.collapseStagePenalty = Math.max(0, decay.collapseStagePenalty);
		recovery.lightLevelThreshold = Mth.clamp(recovery.lightLevelThreshold, 0, 15);
		recovery.lightRecovery = Math.max(0, recovery.lightRecovery);
		recovery.dayRecovery = Math.max(0, recovery.dayRecovery);
		recovery.safeZoneRecovery = Math.max(0, recovery.safeZoneRecovery);
		recovery.campfireRadius = Math.max(1, recovery.campfireRadius);
		recovery.pillRestore = Math.max(1, recovery.pillRestore);
		recovery.pillCooldownTicks = Math.max(0, recovery.pillCooldownTicks);
		recovery.mentalShieldRestore = Mth.clamp(recovery.mentalShieldRestore, 0, 100);
		recovery.mentalShieldCooldownTicks = Math.max(0, recovery.mentalShieldCooldownTicks);
		recovery.mentalShieldDurationTicks = Math.max(0, recovery.mentalShieldDurationTicks);
		sync.minimumValueChange = Math.max(1, sync.minimumValueChange);
		sync.minimumIntervalTicks = Math.max(1, sync.minimumIntervalTicks);
		sync.maximumSilentTicks = Math.max(sync.minimumIntervalTicks, sync.maximumSilentTicks);
		events.globalCooldownTicks = Math.max(0, events.globalCooldownTicks);
		events.budgetPerMinute = Math.max(1, events.budgetPerMinute);
		events.fakeAudioChance = Mth.clamp(events.fakeAudioChance, 0.0F, 1.0F);
		events.falseFeedbackChance = Mth.clamp(events.falseFeedbackChance, 0.0F, 1.0F);
		events.uneasyAmbientChance = Mth.clamp(events.uneasyAmbientChance, 0.0F, 1.0F);
		events.uneasyFootstepChance = Mth.clamp(events.uneasyFootstepChance, 0.0F, 1.0F);
		events.disturbedFootstepChance = Mth.clamp(events.disturbedFootstepChance, 0.0F, 1.0F);
		events.disturbedHallucinationChance = Mth.clamp(events.disturbedHallucinationChance, 0.0F, 1.0F);
		events.fracturedBreathingChance = Mth.clamp(events.fracturedBreathingChance, 0.0F, 1.0F);
		events.fracturedFlickerChance = Mth.clamp(events.fracturedFlickerChance, 0.0F, 1.0F);
		events.collapseHallucinationChance = Mth.clamp(events.collapseHallucinationChance, 0.0F, 1.0F);
		events.bloodParticleChance = Mth.clamp(events.bloodParticleChance, 0.0F, 1.0F);
		events.ambientCooldownTicks = Math.max(1, events.ambientCooldownTicks);
		events.footstepCooldownTicks = Math.max(1, events.footstepCooldownTicks);
		events.breathingCooldownTicks = Math.max(1, events.breathingCooldownTicks);
		events.stalkerCooldownTicks = Math.max(1, events.stalkerCooldownTicks);
		events.bloodyCreeperCooldownTicks = Math.max(1, events.bloodyCreeperCooldownTicks);
		events.particleCooldownTicks = Math.max(1, events.particleCooldownTicks);
		events.flickerCooldownTicks = Math.max(1, events.flickerCooldownTicks);
		events.stalkerLifetimeTicks = Math.max(20, events.stalkerLifetimeTicks);
		events.bloodyCreeperLifetimeTicks = Math.max(20, events.bloodyCreeperLifetimeTicks);
		events.privateHallucinationRadius = Math.max(1, events.privateHallucinationRadius);
		events.containerAudioRadius = Mth.clamp(events.containerAudioRadius, 4, 32);
		multiplayer.maxEffectPacketsPerSecond = Math.max(1, multiplayer.maxEffectPacketsPerSecond);
		return this;
	}

	private static SanityCraftConfig defaults() {
		return new SanityCraftConfig().sanitize();
	}

	public static final class Ticking {
		public int updateIntervalTicks = 40;
		public int effectIntervalTicks = 10;
	}

	public static final class General {
		public boolean debugLogging = false;
		public int respawnSanity = 100;
	}

	public static final class Thresholds {
		public int stableMin = 71;
		public int uneasyMin = 50;
		public int disturbedMin = 30;
		public int fracturedMin = 10;
	}

	public static final class Decay {
		public int darknessLightThreshold = 7;
		public int hostileRadius = 12;
		public int isolationRadius = 20;
		public float lowHealthThreshold = 0.40F;
		public int lowFoodThreshold = 6;
		public int darknessLoss = 2;
		public int hostileLoss = 2;
		public int isolationLoss = 1;
		public int nightLoss = 1;
		public int undergroundLoss = 1;
		public int lowHealthLoss = 1;
		public int lowFoodLoss = 1;
		public int weatherLoss = 1;
		public int disturbedStagePenalty = 1;
		public int fracturedStagePenalty = 2;
		public int collapseStagePenalty = 3;
	}

	public static final class Recovery {
		public int lightRecovery = 1;
		public int dayRecovery = 1;
		public int pillRestore = 18;
		public int pillCooldownTicks = 40;
		public int safeZoneRecovery = 2;
		public int lightLevelThreshold = 12;
		public int campfireRadius = 4;
		public int mentalShieldRestore = 100;
		public int mentalShieldCooldownTicks = 80;
		public int mentalShieldDurationTicks = 20 * 30;
	}

	public static final class Sync {
		public int minimumValueChange = 1;
		public int minimumIntervalTicks = 20;
		public int maximumSilentTicks = 100;
	}

	public static final class Events {
		public int globalCooldownTicks = 80;
		public int budgetPerMinute = 10;
		public float fakeAudioChance = 0.10F;
		public float falseFeedbackChance = 0.05F;
		public float uneasyAmbientChance = 0.07F;
		public float uneasyFootstepChance = 0.03F;
		public float disturbedFootstepChance = 0.08F;
		public float disturbedHallucinationChance = 0.05F;
		public float fracturedBreathingChance = 0.08F;
		public float fracturedFlickerChance = 0.06F;
		public float collapseHallucinationChance = 0.12F;
		public float bloodParticleChance = 0.08F;
		public int ambientCooldownTicks = 120;
		public int footstepCooldownTicks = 100;
		public int breathingCooldownTicks = 80;
		public int stalkerCooldownTicks = 240;
		public int bloodyCreeperCooldownTicks = 280;
		public int particleCooldownTicks = 70;
		public int flickerCooldownTicks = 80;
		public int stalkerLifetimeTicks = 20 * 12;
		public int bloodyCreeperLifetimeTicks = 20 * 6;
		public int privateHallucinationRadius = 24;
		public int containerAudioRadius = 12;
	}

	public static final class Visuals {
		public float intensity = 1.0F;
		public boolean streamerSafeMode = false;
	}

	public static final class Multiplayer {
		public boolean privateHallucinations = true;
		public int maxEffectPacketsPerSecond = 4;
	}
}
