package com.sanitycraft.sanity;

import com.sanitycraft.data.config.SanityCraftConfig;
import java.util.EnumMap;

public final class SanityComponent {
	private static final int EVENT_WINDOW_TICKS = 20 * 60;

	private int sanity = SanityThresholds.DEFAULT_SANITY;
	private int lastSyncedSanity = Integer.MIN_VALUE;
	private long lastSyncGameTime = Long.MIN_VALUE;
	private long nextEvaluationTick = 0L;
	private long nextEffectTick = 0L;
	private final EnumMap<Cooldown, Integer> cooldowns = new EnumMap<>(Cooldown.class);
	private int eventsInWindow;
	private int eventWindowTicks = EVENT_WINDOW_TICKS;
	private int globalEffectCooldownTicks;
	private int hallucinationShieldTicks;
	private boolean forceSync = true;

	public int getSanity() {
		return sanity;
	}

	public boolean shouldEvaluate(long gameTime, int intervalTicks) {
		if (gameTime < nextEvaluationTick) {
			return false;
		}
		nextEvaluationTick = gameTime + Math.max(1, intervalTicks);
		return true;
	}

	public boolean shouldRunEffectTick(long gameTime, int intervalTicks) {
		if (gameTime < nextEffectTick) {
			return false;
		}
		nextEffectTick = gameTime + Math.max(1, intervalTicks);
		return true;
	}

	public void tickRuntime() {
		for (Cooldown cooldown : Cooldown.values()) {
			int ticks = cooldowns.getOrDefault(cooldown, 0);
			if (ticks > 0) {
				cooldowns.put(cooldown, ticks - 1);
			}
		}
		if (globalEffectCooldownTicks > 0) {
			globalEffectCooldownTicks--;
		}
		if (hallucinationShieldTicks > 0) {
			hallucinationShieldTicks--;
		}
		eventWindowTicks--;
		if (eventWindowTicks <= 0) {
			eventWindowTicks = EVENT_WINDOW_TICKS;
			eventsInWindow = 0;
		}
	}

	public SanityUpdate updateSanity(int value, SanityCraftConfig config) {
		int previousSanity = sanity;
		SanityThresholds.Stage previousStage = SanityThresholds.resolve(previousSanity, config);
		int clamped = SanityThresholds.clamp(value);
		sanity = clamped;
		SanityThresholds.Stage currentStage = SanityThresholds.resolve(clamped, config);
		if (previousSanity != clamped || lastSyncedSanity == Integer.MIN_VALUE) {
			forceSync = true;
		}
		return new SanityUpdate(previousSanity, clamped, previousStage, currentStage);
	}

	public void requestImmediateSync() {
		forceSync = true;
	}

	public boolean isCooldownReady(Cooldown cooldown) {
		return cooldowns.getOrDefault(cooldown, 0) <= 0;
	}

	public void setCooldown(Cooldown cooldown, int ticks) {
		cooldowns.put(cooldown, Math.max(0, ticks));
	}

	public boolean tryConsumeEffectBudget(SanityCraftConfig config, int cost) {
		int safeCost = Math.max(1, cost);
		if (globalEffectCooldownTicks > 0) {
			return false;
		}
		if (eventsInWindow + safeCost > Math.max(1, config.events.budgetPerMinute)) {
			return false;
		}
		eventsInWindow += safeCost;
		globalEffectCooldownTicks = Math.max(0, config.events.globalCooldownTicks);
		return true;
	}

	public int getEventsInWindow() {
		return eventsInWindow;
	}

	public int getEventWindowTicks() {
		return Math.max(0, eventWindowTicks);
	}

	public int getGlobalEffectCooldownTicks() {
		return Math.max(0, globalEffectCooldownTicks);
	}

	public void setHallucinationShieldTicks(int ticks) {
		hallucinationShieldTicks = Math.max(hallucinationShieldTicks, Math.max(0, ticks));
	}

	public boolean hasHallucinationShield() {
		return hallucinationShieldTicks > 0;
	}

	public int getHallucinationShieldTicks() {
		return hallucinationShieldTicks;
	}

	public boolean shouldSync(long gameTime, SanityCraftConfig config) {
		if (lastSyncGameTime < 0L) {
			return true;
		}

		long ticksSinceSync = gameTime - lastSyncGameTime;
		if (forceSync && ticksSinceSync >= Math.max(1, config.sync.minimumIntervalTicks)) {
			return true;
		}
		if (Math.abs(sanity - lastSyncedSanity) >= Math.max(1, config.sync.minimumValueChange)
				&& ticksSinceSync >= Math.max(1, config.sync.minimumIntervalTicks)) {
			return true;
		}
		return ticksSinceSync >= Math.max(1, config.sync.maximumSilentTicks);
	}

	public void markSynced(long gameTime) {
		lastSyncedSanity = sanity;
		lastSyncGameTime = gameTime;
		forceSync = false;
	}

	public enum Cooldown {
		AMBIENT_AUDIO,
		FOOTSTEPS,
		BREATHING,
		WHISPERS,
		WORLD_ANOMALY,
		FALSE_EVENT,
		MEMORY_DISTORTION,
		OBSERVER,
		DOPPELGANGER,
		HUD_DISTORTION,
		WORLD_WATCHER,
		OBSERVER_VANISH,
		STALKER,
		BLOODY_CREEPER,
		PARTICLES,
		FLICKER,
		WATCHING_MESSAGE,
		FALSE_BLOCK,
		FALSE_STRUCTURE,
		CHEST_SOUND,
		WINDOW_WATCHER
	}
}
