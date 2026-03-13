package com.sanitycraft.data.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sanitycraft.SanityCraft;
import com.sanitycraft.sanity.SanityDifficultyProfile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class SanityCraftConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("sanitycraft.json");
	private static SanityCraftConfig instance = defaults();

	public SanityDifficultyProfile profile = SanityDifficultyProfile.BALANCED;
	public General general = new General();
	public Thresholds thresholds = new Thresholds();
	public Recovery recovery = new Recovery();
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
		if (thresholds == null) {
			thresholds = new Thresholds();
		}
		if (recovery == null) {
			recovery = new Recovery();
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
		return this;
	}

	private static SanityCraftConfig defaults() {
		return new SanityCraftConfig().sanitize();
	}

	public static final class General {
		public int updateIntervalTicks = 40;
		public int syncIntervalTicks = 20;
		public boolean debugLogging = false;
	}

	public static final class Thresholds {
		public int stableMin = 76;
		public int uneasyMin = 51;
		public int disturbedMin = 26;
		public int fracturedMin = 11;
	}

	public static final class Recovery {
		public int pillRestore = 18;
		public int mentalShieldRestore = 100;
		public int safeZoneRecovery = 2;
	}

	public static final class Events {
		public int globalCooldownTicks = 80;
		public int budgetPerMinute = 10;
		public float fakeAudioChance = 0.10F;
		public float falseFeedbackChance = 0.05F;
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
