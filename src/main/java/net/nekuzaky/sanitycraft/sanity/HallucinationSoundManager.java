package net.nekuzaky.sanitycraft.sanity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class HallucinationSoundManager {
	private HallucinationSoundManager() {
	}

	public static void playStageSound(ServerPlayer player, SanityStage stage, RandomSource random) {
		switch (stage) {
			case MILD_DISCOMFORT -> player.playNotifySound(SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.45F, 0.8F + random.nextFloat() * 0.4F);
			case UNEASY -> playUneasy(player, random);
			case UNSTABLE -> playUnstable(player, random);
			case SEVERE_BREAKDOWN -> playBreakdown(player, random);
			default -> {
			}
		}
	}

	private static void playUneasy(ServerPlayer player, RandomSource random) {
		if (random.nextBoolean()) {
			player.playNotifySound(SoundEvents.PHANTOM_AMBIENT, SoundSource.HOSTILE, 0.55F, 0.75F + random.nextFloat() * 0.2F);
		} else {
			player.playNotifySound(SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.5F, 0.7F + random.nextFloat() * 0.2F);
		}
	}

	private static void playUnstable(ServerPlayer player, RandomSource random) {
		int roll = random.nextInt(4);
		if (roll == 0) {
			player.playNotifySound(SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 0.7F, 0.8F + random.nextFloat() * 0.3F);
		} else if (roll == 1) {
			player.playNotifySound(SoundEvents.PHANTOM_AMBIENT, SoundSource.HOSTILE, 0.7F, 0.75F + random.nextFloat() * 0.2F);
		} else if (roll == 2) {
			player.playNotifySound(SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE, 0.8F, 0.9F + random.nextFloat() * 0.2F);
		} else {
			player.playNotifySound(SoundEvents.WARDEN_NEARBY_CLOSER, SoundSource.HOSTILE, 0.8F, 0.9F + random.nextFloat() * 0.2F);
		}
	}

	private static void playBreakdown(ServerPlayer player, RandomSource random) {
		if (random.nextBoolean()) {
			player.playNotifySound(SoundEvents.WARDEN_NEARBY_CLOSEST, SoundSource.HOSTILE, 0.85F, 0.7F + random.nextFloat() * 0.25F);
		} else {
			player.playNotifySound(SoundEvents.ENDERMAN_AMBIENT, SoundSource.HOSTILE, 0.85F, 0.65F + random.nextFloat() * 0.25F);
		}
	}
}
