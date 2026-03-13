package com.sanitycraft.sanity;

public record SanityUpdate(
		int previousSanity,
		int currentSanity,
		SanityThresholds.Stage previousStage,
		SanityThresholds.Stage currentStage) {
	public boolean changed() {
		return previousSanity != currentSanity;
	}

	public boolean stageChanged() {
		return previousStage != currentStage;
	}

	public int delta() {
		return currentSanity - previousSanity;
	}
}
