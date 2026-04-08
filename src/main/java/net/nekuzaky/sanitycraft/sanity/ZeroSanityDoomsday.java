package net.nekuzaky.sanitycraft.sanity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSources;

public class ZeroSanityDoomsday {
	private static final int DOOMSDAY_DURATION_TICKS = 600; // 30 secondes à 20 ticks/sec
	private static final int CHAOS_EVENT_INTERVAL = 5; // Événement tous les 0.25 secondes

	public static void activateDoomsday(ServerPlayer player) {
		player.displayClientMessage(Component.literal("§0§l§n=== CONSCIOUSNESS COLLAPSE ==="), false);
		player.displayClientMessage(Component.literal("§c§lDOOMSDAY ACTIVATED"), true);
		player.playNotifySound(SoundEvents.WARDEN_NEARBY_CLOSEST, SoundSource.HOSTILE, 1.0F, 0.0F);
		SanityJournal.log(player, "This is it. The end. 30 seconds left.");
	}

	public static void processDoomsday(ServerPlayer player, PlayerSanityComponent component, int ticksSinceDoomsday) {
		RandomSource random = player.getRandom();

		// Chaos événements constants
		if (ticksSinceDoomsday % CHAOS_EVENT_INTERVAL == 0) {
			triggerChaosEvent(player, random, ticksSinceDoomsday);
		}

		// Progression visuelle du DOOM
		int percentRemaining = Math.max(0, ((DOOMSDAY_DURATION_TICKS - ticksSinceDoomsday) * 100) / DOOMSDAY_DURATION_TICKS);

		if (ticksSinceDoomsday % 20 == 0) { // Update toutes les secondes
			player.displayClientMessage(Component.literal("§c§l⏱️ SANITY RECOVERY WINDOW: " + (percentRemaining / 20) + "s"), true);
		}

		// Effets visuels d'apocalypse
		spawnChaosParticles(player, random);

		// MORT CERTAINE après 30 secondes
		if (ticksSinceDoomsday >= DOOMSDAY_DURATION_TICKS) {
			triggerDeath(player);
		}
	}

	private static void triggerChaosEvent(ServerPlayer player, RandomSource random, int ticksSinceDoomsday) {
		int eventType = random.nextInt(12);

		switch (eventType) {
			case 0 -> triggerChaosWhisper(player, random);
			case 1 -> triggerScreenFlash(player, random);
			case 2 -> triggerBlastingSound(player, random);
			case 3 -> triggerParticleStorm(player, random);
			case 4 -> triggerInvertedAudio(player, random);
			case 5 -> triggerRandomJumpscare(player, random);
			case 6 -> triggerDistortedMessage(player, random);
			case 7 -> triggerPitchShift(player, random);
			case 8 -> triggerGlitchEffect(player, random);
			case 9 -> triggerMassiveScream(player, random);
			case 10 -> triggerHeartbeatRush(player, random);
			case 11 -> triggerFinalCountdown(player, ticksSinceDoomsday);
		}
	}

	private static void triggerChaosWhisper(ServerPlayer player, RandomSource random) {
		String[] whispers = {
			"§0§l§oIT'S HERE", "§0§l§oYOU CAN'T ESCAPE", "§0§l§oDIE DIE DIE",
			"§0§l§oTHIS IS FOREVER", "§0§l§oSCREAMING DOESN'T HELP", "§0§l§oGOODBYE"
		};
		player.displayClientMessage(Component.literal(whispers[random.nextInt(whispers.length)]), true);
		player.playNotifySound(SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 1.0F, 0.0F + random.nextFloat() * 0.05F);
	}

	private static void triggerScreenFlash(ServerPlayer player, RandomSource random) {
		player.playNotifySound(SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 1.0F, 0.5F + random.nextFloat() * 0.3F);
		player.displayClientMessage(Component.literal("§f§l§m                                                  §r"), false);
	}

	private static void triggerBlastingSound(ServerPlayer player, RandomSource random) {
		for (int i = 0; i < 3; i++) {
			player.playNotifySound(SoundEvents.WARDEN_NEARBY_CLOSEST, SoundSource.HOSTILE, 1.0F, 0.1F + random.nextFloat() * 0.05F);
		}
	}

	private static void triggerParticleStorm(ServerPlayer player, RandomSource random) {
		player.level().sendParticles(ParticleTypes.SMOKE,
			player.getX() + random.nextGaussian() * 5,
			player.getEyeY() + random.nextGaussian() * 3,
			player.getZ() + random.nextGaussian() * 5,
			random.nextIntBetweenInclusive(50, 100),
			0.5, 0.3, 0.5, 0.1);
	}

	private static void triggerInvertedAudio(ServerPlayer player, RandomSource random) {
		player.playNotifySound(SoundEvents.AMBIENT_CAVE.value(), SoundSource.HOSTILE, 1.0F, 0.0F);
	}

	private static void triggerRandomJumpscare(ServerPlayer player, RandomSource random) {
		int variant = random.nextInt(5);
		SanityNetworking.triggerJumpscare(player, variant, random.nextIntBetweenInclusive(25, 45));
	}

	private static void triggerDistortedMessage(ServerPlayer player, RandomSource random) {
		String[] messages = {
			"§0§k你好§r §0GLITCH§r §0§k你好",
			"§c§o[CORRUPTED]§r",
			"§0§l???INVALID_CONSCIOUSNESS???",
			"§c[ERROR §08439 §cONTOLOGY COLLAPSE]"
		};
		player.displayClientMessage(Component.literal(messages[random.nextInt(messages.length)]), true);
	}

	private static void triggerPitchShift(ServerPlayer player, RandomSource random) {
		float pitch = 0.1F + random.nextFloat() * 0.15F;
		player.playNotifySound(SoundEvents.ENDERMAN_AMBIENT, SoundSource.HOSTILE, 0.95F, pitch);
	}

	private static void triggerGlitchEffect(ServerPlayer player, RandomSource random) {
		for (int i = 0; i < 5; i++) {
			player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.HOSTILE, 0.8F, 0.1F + random.nextFloat() * 0.1F);
		}
	}

	private static void triggerMassiveScream(ServerPlayer player, RandomSource random) {
		player.playNotifySound(SoundEvents.WARDEN_NEARBY_CLOSEST, SoundSource.HOSTILE, 1.0F, 0.05F);
		player.playNotifySound(SoundEvents.ENDERMAN_AMBIENT, SoundSource.HOSTILE, 1.0F, 0.0F);
		player.displayClientMessage(Component.literal("§0§l§nAAAAAAAAAAAAAAAAAAA"), false);
	}

	private static void triggerHeartbeatRush(ServerPlayer player, RandomSource random) {
		for (int i = 0; i < 8; i++) {
			player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.9F, 0.3F + random.nextFloat() * 0.1F);
		}
	}

	private static void triggerFinalCountdown(ServerPlayer player, int ticksSinceDoomsday) {
		int secondsLeft = (DOOMSDAY_DURATION_TICKS - ticksSinceDoomsday) / 20;
		if (secondsLeft <= 5 && ticksSinceDoomsday % 20 == 0) {
			player.displayClientMessage(Component.literal("§c§l§n" + secondsLeft), false);
			player.playNotifySound(SoundEvents.DISPENSER_LAUNCH, SoundSource.HOSTILE, 1.0F, 0.5F + secondsLeft * 0.1F);
		}
	}

	private static void spawnChaosParticles(ServerPlayer player, RandomSource random) {
		if (random.nextBoolean()) {
			player.level().sendParticles(ParticleTypes.SMOKE,
				player.getX() + random.nextGaussian() * 2,
				player.getEyeY(),
				player.getZ() + random.nextGaussian() * 2,
				random.nextIntBetweenInclusive(3, 8),
				0.2, 0.1, 0.2, 0.05);
		}
	}

	private static void triggerDeath(ServerPlayer player) {
		player.displayClientMessage(Component.literal("§0§l§n=== END OF CONSCIOUSNESS ==="), false);
		player.displayClientMessage(Component.literal("§c§l§oGAME OVER"), true);
		player.playNotifySound(SoundEvents.WARDEN_NEARBY_CLOSEST, SoundSource.HOSTILE, 1.0F, 0.0F);

		// MORT INSTANTANÉE
		player.hurt(player.level().damageSources().magic(), 1000000F);
		SanityJournal.log(player, "I don't remember who I was anymore.");
	}
}
