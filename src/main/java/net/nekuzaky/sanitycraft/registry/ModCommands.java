package net.nekuzaky.sanitycraft.registry;

import net.nekuzaky.sanitycraft.command.SanityCommand;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public final class ModCommands {
	private ModCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext, environment) -> {
			SanityCommand.register(dispatcher, commandBuildContext, environment);
		});
	}
}
