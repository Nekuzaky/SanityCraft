package com.sanitycraft.sanity;

public final class SanityComponent {
	private int sanity = SanityThresholds.DEFAULT_SANITY;
	private int lastSyncedSanity = Integer.MIN_VALUE;
	private boolean dirty = true;

	public int getSanity() {
		return sanity;
	}

	public SanityThresholds.Stage getStage() {
		return SanityThresholds.resolve(sanity);
	}

	public void setSanity(int value) {
		int clamped = SanityThresholds.clamp(value);
		if (sanity == clamped && dirty) {
			return;
		}
		sanity = clamped;
		dirty = true;
	}

	public void addSanity(int delta) {
		setSanity(sanity + delta);
	}

	public boolean shouldSync() {
		return dirty || sanity != lastSyncedSanity;
	}

	public void markSynced() {
		dirty = false;
		lastSyncedSanity = sanity;
	}
}
