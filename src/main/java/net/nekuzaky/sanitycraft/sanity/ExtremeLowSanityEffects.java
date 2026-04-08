package net.nekuzaky.sanitycraft.sanity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;

public class ExtremeLowSanityEffects {
	private static final String[] EXTREME_WHISPERS_1 = {
		"PLEASE DON'T LOOK",
		"IT WEARS YOUR FACE",
		"YOUR REFLECTION BLINKS FIRST",
		"COUNT THE SHADOWS",
		"THEY KNOW YOUR NAME",
		"THE LIGHT LIES",
		"NOTHING IS WATCHING YOU... EVERYTHING IS",
		"YOU'VE BEEN SCREAMING FOR HOURS",
		"YOUR SHADOW IS DEEPER THAN IT SHOULD BE",
		"THE CORNERS HAVE EYES"
	};

	private static final String[] EXTREME_WHISPERS_2 = {
		"I LIVE IN YOUR WALLS",
		"YOU CAN'T WAKE UP",
		"DOORS LOCK FROM THE INSIDE",
		"THE NIGHT NEVER ENDS",
		"YOUR FRIENDS AREN'T REAL ANYMORE",
		"SOMETHING WORE THEIR FACE",
		"THE DARKNESS IS BREATHING",
		"YOU'RE NOT ALONE IN HERE",
		"IT LEARNED YOUR VOICE",
		"SMILE OR IT WILL"
	};

	private static final String[] EXTREME_WHISPERS_3 = {
		"TURN AROUND",
		"DON'T BLINK",
		"LISTEN TO WHAT'S BEHIND YOU",
		"YOUR HEARTBEAT ISN'T YOURS",
		"THIS ISN'T MINECRAFT ANYMORE",
		"WAKE UP WAKE UP WAKE UP",
		"THE WORLD IS FOLDING",
		"YOU CAN'T LEAVE",
		"HELP ME",
		"THIS IS YOUR PUNISHMENT"
	};

	public static void triggerExtremeHorror(ServerPlayer player, int sanity, RandomSource random) {
		if (sanity > 15) {
			return;
		}

		int intensity = 1 + random.nextInt(4);
		switch (intensity) {
			case 1 -> triggerMildExtreme(player, random);
			case 2 -> triggerIntenseHorror(player, random);
			case 3 -> triggerSevereHorror(player, random);
			case 4 -> triggerAbsoluteDread(player, random);
		}
	}

	private static void triggerMildExtreme(ServerPlayer player, RandomSource random) {
		String whisper = EXTREME_WHISPERS_1[random.nextInt(EXTREME_WHISPERS_1.length)];
		player.displayClientMessage(Component.literal("§c§k" + whisper + "§r"), true);
		player.playNotifySound(SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 1.0F, 0.2F + random.nextFloat() * 0.15F);
		SanityNetworking.triggerScarePulse(player, 12, 4);
	}

	private static void triggerIntenseHorror(ServerPlayer player, RandomSource random) {
		String whisper = EXTREME_WHISPERS_2[random.nextInt(EXTREME_WHISPERS_2.length)];
		player.displayClientMessage(Component.literal("§4§l§o" + whisper), true);
		player.playNotifySound(SoundEvents.WARDEN_NEARBY_CLOSEST, SoundSource.HOSTILE, 1.0F, 0.1F + random.nextFloat() * 0.1F);
		player.playNotifySound(SoundEvents.ENDERMAN_AMBIENT, SoundSource.HOSTILE, 0.95F, 0.15F + random.nextFloat() * 0.1F);
		SanityNetworking.triggerScarePulse(player, 15, 5);
	}

	private static void triggerSevereHorror(ServerPlayer player, RandomSource random) {
		String whisper = EXTREME_WHISPERS_3[random.nextInt(EXTREME_WHISPERS_3.length)];
		player.displayClientMessage(Component.literal("§0§l§n" + whisper + "§r"), true);
		player.playNotifySound(SoundEvents.WARDEN_NEARBY_CLOSEST, SoundSource.HOSTILE, 1.0F, 0.05F + random.nextFloat() * 0.08F);
		player.playNotifySound(SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 1.0F, 0.1F + random.nextFloat() * 0.08F);
		player.playNotifySound(SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 1.0F, 0.0F);
		SanityNetworking.triggerScarePulse(player, 20, 6);
		SanityJournal.log(player, "I can't remember if I'm still screaming...");
	}

	private static void triggerAbsoluteDread(ServerPlayer player, RandomSource random) {
		String whisper = EXTREME_WHISPERS_1[random.nextInt(EXTREME_WHISPERS_1.length)];
		player.displayClientMessage(Component.literal("§0§l" + whisper), true);
		player.playNotifySound(SoundEvents.WARDEN_NEARBY_CLOSEST, SoundSource.HOSTILE, 1.0F, 0.05F);
		player.playNotifySound(SoundEvents.ENDERMAN_AMBIENT, SoundSource.HOSTILE, 1.0F, 0.0F + random.nextFloat() * 0.05F);
		player.playNotifySound(SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 1.0F, 0.0F);
		SanityNetworking.triggerScarePulse(player, 25, 8);
		SanityJournal.log(player, "I don't want to remember what I just saw.");
	}

	public static void spawnDreadAura(ServerPlayer player, RandomSource random) {
		player.level().sendParticles(ParticleTypes.SMOKE, player.getX(), player.getEyeY(), player.getZ(),
			random.nextIntBetweenInclusive(20, 40),
			0.3 + random.nextDouble() * 0.2,
			0.15,
			0.3 + random.nextDouble() * 0.2,
			0.05);
	}
}
