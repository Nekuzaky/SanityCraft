package com.sanitycraft.registry;

import com.sanitycraft.command.debug.SanityCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public final class ModCommands {
	private ModCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register(SanityCommand::register);
	}
}
