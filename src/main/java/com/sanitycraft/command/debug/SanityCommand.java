package com.sanitycraft.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.sanitycraft.sanity.SanityDebug;
import com.sanitycraft.sanity.SanityManager;
import com.sanitycraft.sanity.SanityThresholds;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class SanityCommand {
	private SanityCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection selection) {
		dispatcher.register(
				Commands.literal("sanity")
						.requires(source -> source.hasPermission(2))
						.executes(context -> show(context.getSource(), context.getSource().getPlayerOrException()))
						.then(Commands.literal("get")
								.executes(context -> show(context.getSource(), context.getSource().getPlayerOrException()))
								.then(Commands.argument("target", EntityArgument.player())
										.executes(context -> show(context.getSource(), EntityArgument.getPlayer(context, "target")))))
						.then(Commands.literal("set")
								.then(Commands.argument("value", IntegerArgumentType.integer(SanityThresholds.MIN_SANITY, SanityThresholds.MAX_SANITY))
										.executes(context -> set(
												context.getSource(),
												context.getSource().getPlayerOrException(),
												IntegerArgumentType.getInteger(context, "value")))
										.then(Commands.argument("target", EntityArgument.player())
												.executes(context -> set(
														context.getSource(),
														EntityArgument.getPlayer(context, "target"),
														IntegerArgumentType.getInteger(context, "value"))))))
						.then(Commands.literal("add")
								.then(Commands.argument("amount", IntegerArgumentType.integer(-100, 100))
										.executes(context -> add(
												context.getSource(),
												context.getSource().getPlayerOrException(),
												IntegerArgumentType.getInteger(context, "amount")))))
						.then(Commands.literal("stable")
								.executes(context -> set(context.getSource(), context.getSource().getPlayerOrException(), SanityThresholds.MAX_SANITY)))
						.then(Commands.literal("collapse")
								.executes(context -> set(context.getSource(), context.getSource().getPlayerOrException(), SanityThresholds.MIN_SANITY)))
						.then(Commands.literal("director")
								.then(Commands.literal("status")
										.executes(context -> {
											ServerPlayer player = context.getSource().getPlayerOrException();
											context.getSource().sendSuccess(() -> SanityDebug.directorStatus(SanityManager.get(player)), false);
											return 1;
										}))));
	}

	private static int show(CommandSourceStack source, ServerPlayer target) {
		source.sendSuccess(() -> SanityDebug.describe(target, SanityManager.get(target)), false);
		return 1;
	}

	private static int set(CommandSourceStack source, ServerPlayer target, int value) {
		SanityManager.setSanity(target, value);
		source.sendSuccess(() -> Component.literal("Set sanity to " + SanityManager.get(target).getSanity()), true);
		return 1;
	}

	private static int add(CommandSourceStack source, ServerPlayer target, int amount) {
		SanityManager.addSanity(target, amount);
		source.sendSuccess(() -> Component.literal("Adjusted sanity by " + amount), true);
		return 1;
	}
}
