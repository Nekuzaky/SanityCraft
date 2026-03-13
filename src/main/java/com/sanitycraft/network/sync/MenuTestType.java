package com.sanitycraft.network.sync;

public enum MenuTestType {
	FLICKER("flicker"),
	PHANTOM_BUTTON("phantom_button"),
	DISTORTION("distortion"),
	TEXT_LIE("text_lie"),
	PLAYER_NAME("player_name");

	private final String commandName;

	MenuTestType(String commandName) {
		this.commandName = commandName;
	}

	public String commandName() {
		return commandName;
	}

	public int id() {
		return ordinal();
	}

	public static MenuTestType byId(int id) {
		MenuTestType[] values = values();
		if (id < 0 || id >= values.length) {
			return DISTORTION;
		}
		return values[id];
	}
}
