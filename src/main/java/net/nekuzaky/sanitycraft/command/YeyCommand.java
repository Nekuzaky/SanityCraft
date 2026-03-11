package net.nekuzaky.sanitycraft.command;

import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandBuildContext;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.CommandDispatcher;

public class YeyCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection environment) {
		dispatcher.register(Commands.literal("yey")

				.then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("YEY", StringArgumentType.string()))));
	}
}