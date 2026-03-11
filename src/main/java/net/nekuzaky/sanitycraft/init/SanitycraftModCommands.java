/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.nekuzaky.sanitycraft.init;

import net.nekuzaky.sanitycraft.command.YeyCommand;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class SanitycraftModCommands {
	public static void load() {
		CommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext, environment) -> {
			YeyCommand.register(dispatcher, commandBuildContext, environment);
		});
	}
}