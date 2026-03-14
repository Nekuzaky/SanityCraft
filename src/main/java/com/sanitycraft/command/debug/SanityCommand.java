package com.sanitycraft.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.sanitycraft.network.sync.ClientEffectSyncService;
import com.sanitycraft.network.sync.MenuTestType;
import com.sanitycraft.sanity.SanityAudioEvent;
import com.sanitycraft.sanity.SanityDebug;
import com.sanitycraft.sanity.SanityHallucinationService;
import com.sanitycraft.sanity.SanityManager;
import com.sanitycraft.sanity.SanityAudioDirector;
import com.sanitycraft.sanity.SanityThresholds;
import com.sanitycraft.sanity.events.SanityCollapseCombinationManager;
import com.sanitycraft.sanity.events.SanityEventManager;
import com.sanitycraft.sanity.gameplay.SanityDoppelgangerService;
import com.sanitycraft.sanity.gameplay.SanityFalseEventService;
import com.sanitycraft.sanity.gameplay.SanityHudDistortionService;
import com.sanitycraft.sanity.gameplay.SanityObserverService;
import com.sanitycraft.sanity.gameplay.SanityWorldAnomalyService;
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
						.then(Commands.literal("recover")
								.executes(context -> set(context.getSource(), context.getSource().getPlayerOrException(), SanityThresholds.MAX_SANITY)))
						.then(Commands.literal("collapse")
								.executes(context -> set(context.getSource(), context.getSource().getPlayerOrException(), SanityThresholds.MIN_SANITY)))
						.then(Commands.literal("debug")
								.then(Commands.literal("on")
										.executes(context -> toggleDebug(context.getSource(), context.getSource().getPlayerOrException(), true)))
								.then(Commands.literal("off")
										.executes(context -> toggleDebug(context.getSource(), context.getSource().getPlayerOrException(), false)))
								.then(Commands.literal("anomaly")
										.executes(context -> debugAnomaly(context.getSource(), context.getSource().getPlayerOrException())))
								.then(Commands.literal("observer")
										.executes(context -> debugObserver(context.getSource(), context.getSource().getPlayerOrException())))
								.then(Commands.literal("false_event")
										.executes(context -> debugFalseEvent(context.getSource(), context.getSource().getPlayerOrException())))
								.then(Commands.literal("event")
										.then(Commands.argument("id", StringArgumentType.word())
												.executes(context -> debugEvent(
														context.getSource(),
														context.getSource().getPlayerOrException(),
														StringArgumentType.getString(context, "id")))))
								.then(Commands.literal("collapse_combo")
										.executes(context -> debugCollapseCombo(context.getSource(), context.getSource().getPlayerOrException())))
								.then(Commands.literal("false_hud")
										.executes(context -> debugFalseHud(context.getSource(), context.getSource().getPlayerOrException())))
								.then(Commands.literal("false_safety")
										.executes(context -> debugFalseSafety(context.getSource(), context.getSource().getPlayerOrException())))
								.then(Commands.literal("collapse_silence")
										.executes(context -> debugCollapseSilence(context.getSource(), context.getSource().getPlayerOrException())))
								.then(Commands.literal("doppelganger")
										.executes(context -> debugDoppelganger(context.getSource(), context.getSource().getPlayerOrException())))
								.then(Commands.literal("hud")
										.executes(context -> debugHud(context.getSource(), context.getSource().getPlayerOrException())))
								.then(Commands.literal("silent_world")
										.executes(context -> debugNamedEvent(context.getSource(), context.getSource().getPlayerOrException(), "silent_world")))
								.then(Commands.literal("impossible_return")
										.executes(context -> debugNamedEvent(context.getSource(), context.getSource().getPlayerOrException(), "impossible_return")))
								.then(Commands.literal("echo_step")
										.executes(context -> debugNamedEvent(context.getSource(), context.getSource().getPlayerOrException(), "echo_step")))
								.then(Commands.literal("wrong_sun")
										.executes(context -> debugNamedEvent(context.getSource(), context.getSource().getPlayerOrException(), "wrong_sun")))
								.then(Commands.literal("almost_mob")
										.executes(context -> debugNamedEvent(context.getSource(), context.getSource().getPlayerOrException(), "almost_mob")))
								.then(Commands.literal("memory_whisper")
										.executes(context -> debugNamedEvent(context.getSource(), context.getSource().getPlayerOrException(), "memory_whisper"))))
						.then(Commands.literal("audio_debug")
								.then(Commands.literal("on")
										.executes(context -> toggleAudioDebug(context.getSource(), context.getSource().getPlayerOrException(), true)))
								.then(Commands.literal("off")
										.executes(context -> toggleAudioDebug(context.getSource(), context.getSource().getPlayerOrException(), false))))
						.then(Commands.literal("force_stage")
								.then(Commands.literal("stable").executes(context -> forceStage(context.getSource(), context.getSource().getPlayerOrException(), "stable", 100)))
								.then(Commands.literal("uneasy").executes(context -> forceStage(context.getSource(), context.getSource().getPlayerOrException(), "uneasy", 60)))
								.then(Commands.literal("disturbed").executes(context -> forceStage(context.getSource(), context.getSource().getPlayerOrException(), "disturbed", 40)))
								.then(Commands.literal("fractured").executes(context -> forceStage(context.getSource(), context.getSource().getPlayerOrException(), "fractured", 20)))
								.then(Commands.literal("collapse").executes(context -> forceStage(context.getSource(), context.getSource().getPlayerOrException(), "collapse", 5))))
						.then(Commands.literal("force")
								.then(Commands.literal("hallucination")
										.executes(context -> forceHallucination(context.getSource(), context.getSource().getPlayerOrException())))
								.then(Commands.literal("scarepulse")
										.executes(context -> forceScarePulse(context.getSource(), context.getSource().getPlayerOrException())))
								.then(Commands.literal("stalker")
										.executes(context -> forceStalker(context.getSource(), context.getSource().getPlayerOrException())))
								.then(Commands.literal("bloodycreeper")
										.executes(context -> forceBloodyCreeper(context.getSource(), context.getSource().getPlayerOrException())))
								.then(Commands.literal("chestsound")
										.executes(context -> forceAudioEvent(context.getSource(), context.getSource().getPlayerOrException(), SanityAudioEvent.CHEST_SOUND)))
								.then(Commands.literal("phantomsound")
										.executes(context -> forceAudioEvent(context.getSource(), context.getSource().getPlayerOrException(), SanityAudioEvent.PHANTOM_SOUND)))
								.then(Commands.literal("cavesound")
										.executes(context -> forceAudioEvent(context.getSource(), context.getSource().getPlayerOrException(), SanityAudioEvent.CAVE_SOUND)))
								.then(Commands.literal("housesound")
										.executes(context -> forceAudioEvent(context.getSource(), context.getSource().getPlayerOrException(), SanityAudioEvent.HOUSE_SOUND)))
								.then(Commands.literal("ambience")
										.executes(context -> forceAmbience(context.getSource(), context.getSource().getPlayerOrException()))))
						.then(Commands.literal("menu_test")
								.then(Commands.literal("flicker")
										.executes(context -> menuTest(context.getSource(), context.getSource().getPlayerOrException(), MenuTestType.FLICKER)))
								.then(Commands.literal("phantom_button")
										.executes(context -> menuTest(context.getSource(), context.getSource().getPlayerOrException(), MenuTestType.PHANTOM_BUTTON)))
								.then(Commands.literal("distortion")
										.executes(context -> menuTest(context.getSource(), context.getSource().getPlayerOrException(), MenuTestType.DISTORTION)))
								.then(Commands.literal("text_lie")
										.executes(context -> menuTest(context.getSource(), context.getSource().getPlayerOrException(), MenuTestType.TEXT_LIE)))
								.then(Commands.literal("player_name")
										.executes(context -> menuTest(context.getSource(), context.getSource().getPlayerOrException(), MenuTestType.PLAYER_NAME))))
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

	private static int toggleDebug(CommandSourceStack source, ServerPlayer target, boolean enabled) {
		SanityDebug.setEnabled(target, enabled);
		source.sendSuccess(() -> Component.literal("Sanity debug " + (enabled ? "enabled" : "disabled") + " for " + target.getName().getString()), true);
		return 1;
	}

	private static int toggleAudioDebug(CommandSourceStack source, ServerPlayer target, boolean enabled) {
		SanityDebug.setAudioDebugEnabled(target, enabled);
		source.sendSuccess(() -> Component.literal("Sanity audio debug " + (enabled ? "enabled" : "disabled") + " for " + target.getName().getString()), true);
		return 1;
	}

	private static int forceStage(CommandSourceStack source, ServerPlayer target, String stageName, int sanity) {
		SanityManager.setSanity(target, sanity);
		source.sendSuccess(() -> Component.literal("Forced stage " + stageName + " (" + SanityManager.get(target).getSanity() + ")"), true);
		return 1;
	}

	private static int forceHallucination(CommandSourceStack source, ServerPlayer target) {
		String forcedType = SanityHallucinationService.forceHallucination(target);
		source.sendSuccess(() -> Component.literal("Forced hallucination: " + forcedType), true);
		return 1;
	}

	private static int forceScarePulse(CommandSourceStack source, ServerPlayer target) {
		ClientEffectSyncService.sendScarePulse(target, 18, 6, "debug_force_scarepulse", true);
		source.sendSuccess(() -> Component.literal("Forced scare pulse"), true);
		return 1;
	}

	private static int forceStalker(CommandSourceStack source, ServerPlayer target) {
		SanityHallucinationService.forceStalker(target);
		source.sendSuccess(() -> Component.literal("Forced stalker hallucination"), true);
		return 1;
	}

	private static int forceBloodyCreeper(CommandSourceStack source, ServerPlayer target) {
		SanityHallucinationService.forceBloodyCreeper(target);
		source.sendSuccess(() -> Component.literal("Forced bloody creeper hallucination"), true);
		return 1;
	}

	private static int forceAmbience(CommandSourceStack source, ServerPlayer target) {
		SanityAudioDirector.forceAmbience(target);
		source.sendSuccess(() -> Component.literal("Forced ambience event"), true);
		return 1;
	}

	private static int forceAudioEvent(CommandSourceStack source, ServerPlayer target, SanityAudioEvent event) {
		String result = SanityAudioDirector.forceAudioEvent(target, event);
		source.sendSuccess(() -> Component.literal(result), true);
		return 1;
	}

	private static int menuTest(CommandSourceStack source, ServerPlayer target, MenuTestType testType) {
		ClientEffectSyncService.sendMenuTest(target, testType);
		source.sendSuccess(() -> Component.literal("Triggered menu test: " + testType.commandName()), true);
		return 1;
	}

	private static int debugAnomaly(CommandSourceStack source, ServerPlayer target) {
		String result = SanityWorldAnomalyService.debugTrigger(target);
		source.sendSuccess(() -> Component.literal(result), true);
		return 1;
	}

	private static int debugObserver(CommandSourceStack source, ServerPlayer target) {
		String result = SanityObserverService.debugTrigger(target);
		source.sendSuccess(() -> Component.literal(result), true);
		return 1;
	}

	private static int debugFalseEvent(CommandSourceStack source, ServerPlayer target) {
		String result = SanityFalseEventService.debugTrigger(target);
		source.sendSuccess(() -> Component.literal(result), true);
		return 1;
	}

	private static int debugDoppelganger(CommandSourceStack source, ServerPlayer target) {
		String result = SanityDoppelgangerService.debugTrigger(target);
		source.sendSuccess(() -> Component.literal(result), true);
		return 1;
	}

	private static int debugEvent(CommandSourceStack source, ServerPlayer target, String eventId) {
		String result = SanityEventManager.debugTrigger(target, eventId);
		source.sendSuccess(() -> Component.literal(result), true);
		return 1;
	}

	private static int debugCollapseCombo(CommandSourceStack source, ServerPlayer target) {
		String result = SanityCollapseCombinationManager.debugTriggerCombo(target);
		source.sendSuccess(() -> Component.literal(result), true);
		return 1;
	}

	private static int debugFalseHud(CommandSourceStack source, ServerPlayer target) {
		String result = SanityCollapseCombinationManager.debugTriggerFalseHud(target);
		source.sendSuccess(() -> Component.literal(result), true);
		return 1;
	}

	private static int debugFalseSafety(CommandSourceStack source, ServerPlayer target) {
		String result = SanityCollapseCombinationManager.debugTriggerFalseSafety(target);
		source.sendSuccess(() -> Component.literal(result), true);
		return 1;
	}

	private static int debugCollapseSilence(CommandSourceStack source, ServerPlayer target) {
		String result = SanityCollapseCombinationManager.debugTriggerSilence(target);
		source.sendSuccess(() -> Component.literal(result), true);
		return 1;
	}

	private static int debugHud(CommandSourceStack source, ServerPlayer target) {
		SanityHudDistortionService.debugTrigger(target);
		source.sendSuccess(() -> Component.literal("Triggered hud distortion"), true);
		return 1;
	}

	private static int debugNamedEvent(CommandSourceStack source, ServerPlayer target, String eventId) {
		String result = SanityEventManager.debugTrigger(target, eventId);
		source.sendSuccess(() -> Component.literal(result), true);
		return 1;
	}
}
