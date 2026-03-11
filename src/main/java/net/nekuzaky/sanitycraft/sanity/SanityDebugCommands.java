package net.nekuzaky.sanitycraft.sanity;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SanityDebugCommands {
	private SanityDebugCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, context, environment) -> dispatcher.register(
				Commands.literal("sanity")
						.requires(source -> source.hasPermission(2))
						.executes(ctx -> {
							ServerPlayer player = ctx.getSource().getPlayerOrException();
							int sanity = SanityManager.get(player).getSanity();
							SanityStage stage = SanityStageResolver.resolve(sanity);
							ctx.getSource().sendSuccess(() -> Component.literal("Sanity: " + sanity + " (" + stage + ")"), false);
							return 1;
						})
						.then(Commands.literal("get")
								.executes(ctx -> {
									ServerPlayer player = ctx.getSource().getPlayerOrException();
									int sanity = SanityManager.get(player).getSanity();
									SanityStage stage = SanityStageResolver.resolve(sanity);
									ctx.getSource().sendSuccess(() -> Component.literal("Sanity: " + sanity + " (" + stage + ")"), false);
									return 1;
								})
								.then(Commands.argument("target", net.minecraft.commands.arguments.EntityArgument.player())
										.executes(ctx -> {
											ServerPlayer target = net.minecraft.commands.arguments.EntityArgument.getPlayer(ctx, "target");
											int sanity = SanityManager.get(target).getSanity();
											SanityStage stage = SanityStageResolver.resolve(sanity);
											ctx.getSource().sendSuccess(() -> Component.literal(target.getName().getString() + " sanity: " + sanity + " (" + stage + ")"), false);
											return 1;
										})))
						.then(Commands.literal("set")
								.then(Commands.argument("value", IntegerArgumentType.integer(PlayerSanityComponent.MIN_SANITY, PlayerSanityComponent.MAX_SANITY))
										.executes(ctx -> {
											ServerPlayer player = ctx.getSource().getPlayerOrException();
											int value = IntegerArgumentType.getInteger(ctx, "value");
											SanityManager.setSanity(player, value);
											ctx.getSource().sendSuccess(() -> Component.literal("Set sanity to " + value), true);
											return 1;
										})
										.then(Commands.argument("target", net.minecraft.commands.arguments.EntityArgument.player())
												.executes(ctx -> {
													int value = IntegerArgumentType.getInteger(ctx, "value");
													ServerPlayer target = net.minecraft.commands.arguments.EntityArgument.getPlayer(ctx, "target");
													SanityManager.setSanity(target, value);
													ctx.getSource().sendSuccess(() -> Component.literal("Set " + target.getName().getString() + " sanity to " + value), true);
													return 1;
												}))))
						.then(Commands.literal("add")
								.then(Commands.argument("delta", IntegerArgumentType.integer(-100, 100))
										.executes(ctx -> {
											ServerPlayer player = ctx.getSource().getPlayerOrException();
											int delta = IntegerArgumentType.getInteger(ctx, "delta");
											SanityManager.addSanity(player, delta);
											ctx.getSource().sendSuccess(() -> Component.literal("Adjusted sanity by " + delta), true);
											return 1;
										})))
						.then(Commands.literal("reloadconfig")
								.executes(ctx -> {
									SanityManager.initialize();
									ctx.getSource().sendSuccess(() -> Component.literal("Sanity config reloaded."), true);
									return 1;
								}))));
	}
}
