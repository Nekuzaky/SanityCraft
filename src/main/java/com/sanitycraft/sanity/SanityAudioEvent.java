package com.sanitycraft.sanity;

public enum SanityAudioEvent {
	CHEST_SOUND("chestsound"),
	PHANTOM_SOUND("phantomsound"),
	CAVE_SOUND("cavesound"),
	HOUSE_SOUND("housesound"),
	FOREST_SOUND("forestsound"),
	BREATHING("breathing");

	private final String commandName;

	SanityAudioEvent(String commandName) {
		this.commandName = commandName;
	}

	public String commandName() {
		return commandName;
	}
}
