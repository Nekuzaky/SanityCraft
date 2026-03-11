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

	public int getSanity() {
		return sanity;
	}

	public void setSanity(int sanity) {
		this.sanity = Mth.clamp(sanity, MIN_SANITY, MAX_SANITY);
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
}
