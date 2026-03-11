package net.nekuzaky.sanitycraft.sanity;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SanityDebugCommands {
	private SanityDebugCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, context, environment) -> dispatcher.register(
				Commands.literal("sanity")
						.requires(source -> source.hasPermission(2))
						.executes(ctx -> showSanity(ctx.getSource().getPlayerOrException(), ctx.getSource()))
						.then(Commands.literal("get")
								.executes(ctx -> showSanity(ctx.getSource().getPlayerOrException(), ctx.getSource()))
								.then(Commands.argument("target", EntityArgument.player())
										.executes(ctx -> {
											ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
											return showSanity(target, ctx.getSource());
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
										.then(Commands.argument("target", EntityArgument.player())
												.executes(ctx -> {
													int value = IntegerArgumentType.getInteger(ctx, "value");
													ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
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
								}))
						.then(Commands.literal("profile")
								.then(Commands.literal("light").executes(ctx -> setProfile(ctx, "light")))
								.then(Commands.literal("medium").executes(ctx -> setProfile(ctx, "medium")))
								.then(Commands.literal("hardcore").executes(ctx -> setProfile(ctx, "hardcore")))
								.then(Commands.literal("custom").executes(ctx -> setProfile(ctx, "custom"))))
						.then(Commands.literal("journal")
								.executes(ctx -> {
									ServerPlayer player = ctx.getSource().getPlayerOrException();
									var entries = SanityJournal.recent(player, 8);
									if (entries.isEmpty()) {
										ctx.getSource().sendSuccess(() -> Component.literal("Journal is empty."), false);
										return 1;
									}
									ctx.getSource().sendSuccess(() -> Component.literal("Recent journal entries:"), false);
									for (String line : entries) {
										ctx.getSource().sendSuccess(() -> Component.literal("- " + line), false);
									}
									return 1;
								})
								.then(Commands.literal("clear")
										.executes(ctx -> {
											ServerPlayer player = ctx.getSource().getPlayerOrException();
											SanityJournal.clear(player);
											ctx.getSource().sendSuccess(() -> Component.literal("Journal cleared."), true);
											return 1;
										})))
						.then(Commands.literal("debug")
								.executes(ctx -> debugStatus(ctx.getSource().getPlayerOrException(), ctx.getSource()))
								.then(Commands.literal("status")
										.executes(ctx -> debugStatus(ctx.getSource().getPlayerOrException(), ctx.getSource())))
								.then(Commands.literal("on")
										.executes(ctx -> setDebug(ctx.getSource().getPlayerOrException(), ctx.getSource(), true)))
								.then(Commands.literal("off")
										.executes(ctx -> setDebug(ctx.getSource().getPlayerOrException(), ctx.getSource(), false))))));
	}

	private static int showSanity(ServerPlayer target, CommandSourceStack source) {
		int sanity = SanityManager.get(target).getSanity();
		SanityStage stage = SanityStageResolver.resolve(sanity);
		String owner = source.getEntity() == target ? "Sanity" : target.getName().getString() + " sanity";
		source.sendSuccess(() -> Component.literal(owner + ": " + sanity + " (" + stage + ")"), false);
		return 1;
	}

	private static int setProfile(CommandContext<CommandSourceStack> ctx, String profile) {
		if (!SanityManager.applyProfile(profile)) {
			ctx.getSource().sendFailure(Component.literal("Invalid profile."));
			return 0;
		}
		ctx.getSource().sendSuccess(() -> Component.literal("Applied sanity profile: " + profile), true);
		return 1;
	}

	private static int setDebug(ServerPlayer player, CommandSourceStack source, boolean enabled) {
		if (enabled) {
			SanityDebugState.enable(player);
			source.sendSuccess(() -> Component.literal("Sanity debug enabled."), true);
		} else {
			SanityDebugState.disable(player);
			source.sendSuccess(() -> Component.literal("Sanity debug disabled."), true);
		}
		return debugStatus(player, source);
	}

	private static int debugStatus(ServerPlayer player, CommandSourceStack source) {
		PlayerSanityComponent component = SanityManager.get(player);
		SanityConfig config = SanityManager.getConfig();
		source.sendSuccess(() -> Component.literal("Debug: " + (SanityDebugState.isEnabled(player) ? "ON" : "OFF")), false);
		source.sendSuccess(() -> Component.literal(
				"Event budget: " + component.getHorrorEventsInWindow() + "/" + Math.max(1, config.horrorEventsPerMinute) + " | globalCooldown=" + component.getHorrorGlobalCooldown() + " ticks"),
				false);
		source.sendSuccess(() -> Component.literal("Window reset in " + component.getHorrorWindowTicksRemaining() + " ticks"), false);
		return 1;
	}
}
