package net.nekuzaky.sanitycraft.sanity;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class PlayerSanityComponent {
	public static final int MIN_SANITY = 0;
	public static final int MAX_SANITY = 100;
	public static final int DEFAULT_SANITY = 100;

	private int sanity = DEFAULT_SANITY;
	private int updateTicker = 0;
	private int strangeSoundCooldown = 0;
	private int hallucinationCooldown = 0;
	private int ghostCooldown = 0;
	private int whisperCooldown = 0;
	private int jumpscareCooldown = 0;
	private int mimicCooldown = 0;
	private int journalCooldown = 0;
	private int falseUiCooldown = 0;
	private int lootCorruptionCooldown = 0;
	private int horrorGlobalCooldown = 0;
	private int horrorWindowTicks = 20 * 60;
	private int horrorEventsInWindow = 0;
	private int ritualFeedbackCooldown = 0;
	private boolean sleepingLastTick = false;
	private boolean ritualSafeZoneLastTick = false;
	private int zeroSanityTicks = 0;
	private int hallucinationShieldTicks = 0;

	public int getSanity() {
		return sanity;
	}

	public void setSanity(int sanity) {
		this.sanity = Mth.clamp(sanity, MIN_SANITY, MAX_SANITY);
		if (this.sanity > MIN_SANITY) {
			zeroSanityTicks = 0;
		}
	}

	public int addSanity(int delta) {
		int old = sanity;
		setSanity(sanity + delta);
		return sanity - old;
	}

	public boolean shouldUpdate(int intervalTicks) {
		updateTicker++;
		return updateTicker % Math.max(1, intervalTicks) == 0;
	}

	public void tickCooldowns() {
		if (strangeSoundCooldown > 0) {
			strangeSoundCooldown--;
		}
		if (hallucinationCooldown > 0) {
			hallucinationCooldown--;
		}
		if (ghostCooldown > 0) {
			ghostCooldown--;
		}
		if (whisperCooldown > 0) {
			whisperCooldown--;
		}
		if (jumpscareCooldown > 0) {
			jumpscareCooldown--;
		}
		if (mimicCooldown > 0) {
			mimicCooldown--;
		}
		if (journalCooldown > 0) {
			journalCooldown--;
		}
		if (falseUiCooldown > 0) {
			falseUiCooldown--;
		}
		if (lootCorruptionCooldown > 0) {
			lootCorruptionCooldown--;
		}
		if (horrorGlobalCooldown > 0) {
			horrorGlobalCooldown--;
		}
		if (ritualFeedbackCooldown > 0) {
			ritualFeedbackCooldown--;
		}
		horrorWindowTicks--;
		if (horrorWindowTicks <= 0) {
			horrorWindowTicks = 20 * 60;
			horrorEventsInWindow = 0;
		}
		if (hallucinationShieldTicks > 0) {
			hallucinationShieldTicks--;
		}
	}

	public boolean canPlayStrangeSound() {
		return strangeSoundCooldown <= 0;
	}

	public boolean canPlayHallucination() {
		return hallucinationCooldown <= 0;
	}

	public boolean canSpawnGhost() {
		return ghostCooldown <= 0;
	}

	public void resetStrangeSoundCooldown(RandomSource random) {
		strangeSoundCooldown = random.nextIntBetweenInclusive(60, 140);
	}

	public void resetHallucinationCooldown(RandomSource random) {
		hallucinationCooldown = random.nextIntBetweenInclusive(40, 90);
	}

	public void resetGhostCooldown(RandomSource random) {
		ghostCooldown = random.nextIntBetweenInclusive(80, 140);
	}

	public boolean canWhisper() {
		return whisperCooldown <= 0;
	}

	public void resetWhisperCooldown(RandomSource random) {
		whisperCooldown = random.nextIntBetweenInclusive(40, 120);
	}

	public boolean canJumpscare() {
		return jumpscareCooldown <= 0;
	}

	public void resetJumpscareCooldown(RandomSource random) {
		jumpscareCooldown = random.nextIntBetweenInclusive(600, 1600);
	}

	public boolean canPlayMimic() {
		return mimicCooldown <= 0;
	}

	public void resetMimicCooldown(RandomSource random) {
		mimicCooldown = random.nextIntBetweenInclusive(120, 260);
	}

	public boolean canWriteJournal() {
		return journalCooldown <= 0;
	}

	public void resetJournalCooldown(RandomSource random) {
		journalCooldown = random.nextIntBetweenInclusive(180, 320);
	}

	public boolean markSleepStateAndCheckWake(boolean sleepingNow) {
		boolean woke = sleepingLastTick && !sleepingNow;
		sleepingLastTick = sleepingNow;
		return woke;
	}

	public boolean canTriggerFalseUi() {
		return falseUiCooldown <= 0;
	}

	public void resetFalseUiCooldown(RandomSource random) {
		falseUiCooldown = random.nextIntBetweenInclusive(120, 260);
	}

	public boolean canTriggerLootCorruption() {
		return lootCorruptionCooldown <= 0;
	}

	public void resetLootCorruptionCooldown(RandomSource random) {
		lootCorruptionCooldown = random.nextIntBetweenInclusive(160, 320);
	}

	public boolean tryConsumeHorrorEventBudget(SanityConfig config, RandomSource random, int cost) {
		int safeCost = Math.max(1, cost);
		if (horrorGlobalCooldown > 0) {
			return false;
		}
		int maxPerMinute = Math.max(1, config.horrorEventsPerMinute);
		if (horrorEventsInWindow + safeCost > maxPerMinute) {
			return false;
		}
		horrorEventsInWindow += safeCost;

		int minCooldown = Math.max(0, config.horrorGlobalCooldownMinTicks);
		int maxCooldown = Math.max(minCooldown, config.horrorGlobalCooldownMaxTicks);
		horrorGlobalCooldown = minCooldown + (maxCooldown > minCooldown ? random.nextInt(maxCooldown - minCooldown + 1) : 0);
		return true;
	}

	public int getHorrorEventsInWindow() {
		return horrorEventsInWindow;
	}

	public int getHorrorWindowTicksRemaining() {
		return Math.max(0, horrorWindowTicks);
	}

	public int getHorrorGlobalCooldown() {
		return Math.max(0, horrorGlobalCooldown);
	}

	public boolean markRitualSafeZone(boolean inSafeZone) {
		boolean entered = inSafeZone && !ritualSafeZoneLastTick;
		ritualSafeZoneLastTick = inSafeZone;
		return entered;
	}

	public boolean canTriggerRitualFeedback() {
		return ritualFeedbackCooldown <= 0;
	}

	public void resetRitualFeedbackCooldown(RandomSource random) {
		ritualFeedbackCooldown = random.nextIntBetweenInclusive(140, 240);
	}

	public boolean tickZeroSanityTimer(int deathDelayTicks) {
		if (sanity <= MIN_SANITY) {
			zeroSanityTicks++;
			return zeroSanityTicks >= Math.max(1, deathDelayTicks);
		}
		zeroSanityTicks = 0;
		return false;
	}

	public void resetZeroSanityTimer() {
		zeroSanityTicks = 0;
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
}
