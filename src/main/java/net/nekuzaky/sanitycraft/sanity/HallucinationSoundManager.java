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
			case MILD_DISCOMFORT -> player.playNotifySound(SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.6F, 0.7F + random.nextFloat() * 0.3F);
			case UNEASY -> playUneasy(player, random);
			case UNSTABLE -> playUnstable(player, random);
			case SEVERE_BREAKDOWN -> playBreakdown(player, random);
			default -> {
			}
		}
	}

	private static void playUneasy(ServerPlayer player, RandomSource random) {
		int roll = random.nextInt(3);
		if (roll == 0) {
			player.playNotifySound(SoundEvents.PHANTOM_AMBIENT, SoundSource.HOSTILE, 0.70F, 0.65F + random.nextFloat() * 0.25F);
		} else if (roll == 1) {
			player.playNotifySound(SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.65F, 0.6F + random.nextFloat() * 0.2F);
		} else {
			player.playNotifySound(SoundEvents.WARDEN_NEARBY_CLOSER, SoundSource.HOSTILE, 0.55F, 0.5F + random.nextFloat() * 0.15F);
		}
	}

	private static void playUnstable(ServerPlayer player, RandomSource random) {
		int roll = random.nextInt(6);
		if (roll == 0) {
			player.playNotifySound(SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 0.9F, 0.6F + random.nextFloat() * 0.25F);
		} else if (roll == 1) {
			player.playNotifySound(SoundEvents.PHANTOM_AMBIENT, SoundSource.HOSTILE, 0.85F, 0.5F + random.nextFloat() * 0.2F);
		} else if (roll == 2) {
			player.playNotifySound(SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE, 0.95F, 0.75F + random.nextFloat() * 0.25F);
		} else if (roll == 3) {
			player.playNotifySound(SoundEvents.WARDEN_NEARBY_CLOSER, SoundSource.HOSTILE, 0.92F, 0.7F + random.nextFloat() * 0.2F);
		} else if (roll == 4) {
			player.playNotifySound(SoundEvents.ENDERMAN_AMBIENT, SoundSource.HOSTILE, 0.88F, 0.55F + random.nextFloat() * 0.2F);
		} else {
			player.playNotifySound(SoundEvents.WARDEN_NEARBY_CLOSEST, SoundSource.HOSTILE, 0.90F, 0.4F + random.nextFloat() * 0.15F);
		}
	}

	private static void playBreakdown(ServerPlayer player, RandomSource random) {
		int roll = random.nextInt(4);
		if (roll == 0) {
			player.playNotifySound(SoundEvents.WARDEN_NEARBY_CLOSEST, SoundSource.HOSTILE, 0.95F, 0.3F + random.nextFloat() * 0.2F);
		} else if (roll == 1) {
			player.playNotifySound(SoundEvents.ENDERMAN_AMBIENT, SoundSource.HOSTILE, 0.92F, 0.35F + random.nextFloat() * 0.2F);
		} else if (roll == 2) {
			player.playNotifySound(SoundEvents.WARDEN_NEARBY_CLOSER, SoundSource.HOSTILE, 0.95F, 0.25F + random.nextFloat() * 0.15F);
		} else {
			player.playNotifySound(SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 0.88F, 0.2F + random.nextFloat() * 0.1F);
		}
	}
}
