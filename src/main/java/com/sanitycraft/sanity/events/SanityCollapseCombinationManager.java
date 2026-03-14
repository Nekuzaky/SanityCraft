package com.sanitycraft.sanity.events;

import com.sanitycraft.data.config.SanityCraftConfig;
import com.sanitycraft.network.sync.ClientEffectSyncService;
import com.sanitycraft.sanity.SanityAudioDirector;
import com.sanitycraft.sanity.SanityComponent;
import com.sanitycraft.sanity.SanityDebug;
import com.sanitycraft.sanity.SanityManager;
import com.sanitycraft.sanity.SanityPsychologicalService;
import com.sanitycraft.sanity.gameplay.SanityObserverService;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public final class SanityCollapseCombinationManager {
	private static final String COMBO_PARALYSIS = "collapse_paralysis";
	private static final String COMBO_PURSUIT = "collapse_pursuit";
	private static final String COMBO_DISBELIEF = "collapse_disbelief";
	private static final String COMBO_BREACH = "collapse_breach";

	private static final Map<UUID, CollapseState> STATES = new ConcurrentHashMap<>();

	private SanityCollapseCombinationManager() {
	}

	public static boolean tickPlayer(ServerPlayer player, SanityComponent component, SanityCraftConfig config, long gameTime) {
		if (component.getSanity() > 0) {
			clearPlayer(player);
			return false;
		}

		CollapseState state = STATES.computeIfAbsent(player.getUUID(), ignored -> new CollapseState());
		if (gameTime < state.quietUntilTick || gameTime < state.nextComboTick) {
			return false;
		}

		RandomSource random = player.getRandom();
		boolean indoors = SanityPsychologicalService.isPlayerInsideBase(player);
		float triggerChance = state.clusterBurstsRemaining > 0 && gameTime <= state.clusterWindowEndTick
				? (indoors ? 0.20F : 0.16F)
				: (indoors ? 0.05F : 0.035F);
		if (random.nextFloat() >= triggerChance) {
			state.nextComboTick = gameTime + (state.clusterBurstsRemaining > 0 ? 30L + random.nextInt(30) : 70L + random.nextInt(80));
			return false;
		}
		if (!component.tryConsumeEffectBudget(config, indoors ? 4 : 3)) {
			state.nextComboTick = gameTime + 80L + random.nextInt(80);
			return false;
		}

		String comboId = chooseCombo(random, state.lastComboId, indoors);
		if (!triggerCombo(player, component, config, gameTime, comboId, false)) {
			state.nextComboTick = gameTime + 50L + random.nextInt(50);
			return false;
		}

		state.lastComboId = comboId;
		if (state.clusterBurstsRemaining > 0 && gameTime <= state.clusterWindowEndTick) {
			state.clusterBurstsRemaining--;
			if (state.clusterBurstsRemaining > 0) {
				state.nextComboTick = gameTime + 30L + random.nextInt(28);
			} else {
				state.clusterWindowEndTick = 0L;
				state.quietUntilTick = gameTime + 20L * (12 + random.nextInt(14));
				state.nextComboTick = state.quietUntilTick;
			}
		} else if (random.nextFloat() < 0.38F) {
			state.clusterBurstsRemaining = 1 + random.nextInt(2);
			state.clusterWindowEndTick = gameTime + 20L * (6 + random.nextInt(6));
			state.nextComboTick = gameTime + 34L + random.nextInt(24);
		} else {
			state.clusterBurstsRemaining = 0;
			state.clusterWindowEndTick = 0L;
			state.quietUntilTick = gameTime + 20L * (14 + random.nextInt(16));
			state.nextComboTick = state.quietUntilTick;
		}
		return true;
	}

	public static String debugTriggerCombo(ServerPlayer player) {
		SanityComponent component = SanityManager.get(player);
		SanityCraftConfig config = SanityCraftConfig.get();
		boolean triggered = triggerCombo(player, component, config, player.level().getGameTime(), COMBO_PARALYSIS, true);
		return triggered ? "Triggered collapse combo: " + COMBO_PARALYSIS : "Could not trigger collapse combo.";
	}

	public static String debugTriggerFalseHud(ServerPlayer player) {
		boolean sent = triggerFalseHud(player, 70, 4, 2, "debug_false_hud", true);
		return sent ? "Triggered collapse false HUD." : "Could not trigger collapse false HUD.";
	}

	public static String debugTriggerFalseSafety(ServerPlayer player) {
		boolean triggered = SanityPsychologicalService.triggerFalseSafety(player, SanityManager.get(player), true, true, "debug_false_safety");
		return triggered ? "Triggered false safety breach." : "Could not trigger false safety breach here.";
	}

	public static String debugTriggerSilence(ServerPlayer player) {
		boolean triggered = triggerSilenceDip(player, 34, 3, "debug_collapse_silence", true);
		return triggered ? "Triggered collapse silence dip." : "Could not trigger collapse silence dip.";
	}

	public static void clearPlayer(ServerPlayer player) {
		STATES.remove(player.getUUID());
	}

	public static void clearAll() {
		STATES.clear();
	}

	private static String chooseCombo(RandomSource random, String lastComboId, boolean indoors) {
		String[] pool = indoors
				? new String[] { COMBO_BREACH, COMBO_PARALYSIS, COMBO_PURSUIT, COMBO_DISBELIEF }
				: new String[] { COMBO_PARALYSIS, COMBO_PURSUIT, COMBO_DISBELIEF };
		String chosen = pool[random.nextInt(pool.length)];
		if (pool.length > 1 && chosen.equals(lastComboId)) {
			chosen = pool[(random.nextInt(pool.length - 1) + 1 + indexOf(pool, chosen)) % pool.length];
		}
		return chosen;
	}

	private static boolean triggerCombo(
			ServerPlayer player,
			SanityComponent component,
			SanityCraftConfig config,
			long gameTime,
			String comboId,
			boolean forced) {
		int successes = switch (comboId) {
			case COMBO_PARALYSIS -> triggerParalysisCombo(player, component, config, gameTime, forced);
			case COMBO_PURSUIT -> triggerPursuitCombo(player, component, config, gameTime, forced);
			case COMBO_BREACH -> triggerBreachCombo(player, component, config, gameTime, forced);
			case COMBO_DISBELIEF -> triggerDisbeliefCombo(player, component, config, gameTime, forced);
			default -> 0;
		};
		if (successes >= 2) {
			SanityDebug.logEvent(player, "collapse_combo id=" + comboId + " parts=" + successes + " forced=" + forced);
			return true;
		}
		return false;
	}

	private static int triggerParalysisCombo(ServerPlayer player, SanityComponent component, SanityCraftConfig config, long gameTime, boolean forced) {
		int successes = 0;
		successes += success(triggerSilenceDip(player, 26, 2, COMBO_PARALYSIS, true));
		successes += success(SanityEventManager.triggerEvent(player, component, config, gameTime, "world_freeze", true));
		successes += success(SanityEventManager.triggerEvent(player, component, config, gameTime, "world_observation", true));
		successes += success(SanityObserverService.spawnCloseCollapseObserver(player, true, COMBO_PARALYSIS));
		if (!forced) {
			successes += success(triggerFalseHud(player, 46, 3, 1, COMBO_PARALYSIS, true));
		}
		return successes;
	}

	private static int triggerPursuitCombo(ServerPlayer player, SanityComponent component, SanityCraftConfig config, long gameTime, boolean forced) {
		int successes = 0;
		successes += success(SanityEventManager.triggerEvent(player, component, config, gameTime, "fake_creeper", true));
		successes += success(SanityAudioDirector.playFalseSafetyBreathing(player, true, COMBO_PURSUIT));
		successes += success(SanityEventManager.triggerEvent(player, component, config, gameTime, "light_flicker", true));
		successes += success(SanityEventManager.triggerEvent(player, component, config, gameTime, "phantom_hotbar_slot", true));
		if (!forced) {
			successes += success(triggerFalseHud(player, 36, 2, 4, COMBO_PURSUIT, true));
		}
		return successes;
	}

	private static int triggerBreachCombo(ServerPlayer player, SanityComponent component, SanityCraftConfig config, long gameTime, boolean forced) {
		int successes = 0;
		successes += success(triggerSilenceDip(player, 22, 2, COMBO_BREACH, true));
		successes += success(SanityPsychologicalService.triggerFalseSafety(player, component, true, forced, COMBO_BREACH));
		successes += success(SanityEventManager.triggerEvent(player, component, config, gameTime, "shadow_runner", true));
		successes += success(triggerFalseHud(player, 52, 4, 5, COMBO_BREACH, true));
		return successes;
	}

	private static int triggerDisbeliefCombo(ServerPlayer player, SanityComponent component, SanityCraftConfig config, long gameTime, boolean forced) {
		int successes = 0;
		successes += success(triggerSilenceDip(player, 30, 3, COMBO_DISBELIEF, true));
		successes += success(SanityObserverService.spawnCloseCollapseObserver(player, true, COMBO_DISBELIEF));
		successes += success(SanityEventManager.triggerEvent(player, component, config, gameTime, "fake_damage", true));
		successes += success(triggerFalseHud(player, 60, 4, 7, COMBO_DISBELIEF, true));
		if (!forced) {
			successes += success(SanityEventManager.triggerEvent(player, component, config, gameTime, "blood_particle", true));
		}
		return successes;
	}

	private static boolean triggerSilenceDip(ServerPlayer player, int durationTicks, int intensity, String source, boolean bypassRateLimit) {
		player.connection.send(new ClientboundStopSoundPacket(null, SoundSource.AMBIENT));
		player.connection.send(new ClientboundStopSoundPacket(null, SoundSource.HOSTILE));
		player.connection.send(new ClientboundStopSoundPacket(null, SoundSource.WEATHER));
		if (intensity >= 3) {
			player.connection.send(new ClientboundStopSoundPacket(null, SoundSource.BLOCKS));
		}
		return ClientEffectSyncService.sendSanityEvent(
				player,
				"collapse_silence",
				player.blockPosition(),
				durationTicks,
				intensity,
				0,
				"",
				source,
				bypassRateLimit);
	}

	private static boolean triggerFalseHud(ServerPlayer player, int durationTicks, int intensity, int variant, String source, boolean bypassRateLimit) {
		boolean sent = ClientEffectSyncService.sendSanityEvent(
				player,
				"collapse_false_hud",
				player.blockPosition(),
				durationTicks,
				intensity,
				variant,
				"",
				source,
				bypassRateLimit);
		if (!sent) {
			return false;
		}
		ClientEffectSyncService.sendHudDistortion(
				player,
				Math.max(16, durationTicks / 2),
				Math.max(2, intensity),
				(variant & 1) == 0,
				source + "_distortion",
				true);
		return true;
	}

	private static int success(boolean value) {
		return value ? 1 : 0;
	}

	private static int indexOf(String[] pool, String value) {
		for (int i = 0; i < pool.length; i++) {
			if (pool[i].equals(value)) {
				return i;
			}
		}
		return 0;
	}

	private static final class CollapseState {
		private long quietUntilTick;
		private long nextComboTick;
		private long clusterWindowEndTick;
		private int clusterBurstsRemaining;
		private String lastComboId = "";
	}
}
