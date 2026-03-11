package net.nekuzaky.sanitycraft.sanity;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class SanityNarrativeDirector {
	private SanityNarrativeDirector() {
	}

	public static void tick(ServerPlayer player, PlayerSanityComponent component, SanityConfig config) {
		if (!config.hallucinationsEnabled || !config.narrativeEventsEnabled) {
			return;
		}

		int sanity = component.getSanity();
		if (sanity > 70) {
			return;
		}

		RandomSource random = player.getRandom();
		if (sanity <= 45 && component.canWhisper() && random.nextFloat() < clamp01(config.narrativeWhisperChance)) {
			sendNarrativeWhisper(player, sanity, random);
			component.resetWhisperCooldown(random);
		}

		if (sanity <= 55 && random.nextFloat() < clamp01(config.narrativeFootstepChance)) {
			playDistantFootstep(player, random);
		}

		if (sanity <= 20 && component.canJumpscare() && random.nextFloat() < clamp01(config.narrativeJumpscareChance)) {
			int variant = random.nextInt(3);
			int duration = random.nextIntBetweenInclusive(18, 30);
			SanityNetworking.triggerJumpscare(player, variant, duration);
			component.resetJumpscareCooldown(random);
		}
	}

	private static void sendNarrativeWhisper(ServerPlayer player, int sanity, RandomSource random) {
		String[] lines = sanity <= 20
				? new String[] {"YOUR MIND IS NOT YOURS", "IT IS INSIDE THE WALLS", "DO NOT OPEN THE INVENTORY", "IT WALKS BEHIND YOU"}
				: sanity <= 40 ? new String[] {"someone is near", "there is no safe place", "do not stop moving", "you missed a sound behind you"}
						: new String[] {"the cave is breathing", "you should leave now", "something watches the light", "keep your eyes forward"};
		player.displayClientMessage(Component.literal(lines[random.nextInt(lines.length)]), true);
	}

	private static void playDistantFootstep(ServerPlayer player, RandomSource random) {
		float pitch = 0.6F + random.nextFloat() * 0.35F;
		if (random.nextBoolean()) {
			player.playNotifySound(SoundEvents.WARDEN_STEP, SoundSource.HOSTILE, 0.55F, pitch);
		} else {
			player.playNotifySound(SoundEvents.ZOMBIE_STEP, SoundSource.HOSTILE, 0.55F, pitch);
		}
	}

	private static float clamp01(float value) {
		if (value < 0.0F) {
			return 0.0F;
		}
		return Math.min(1.0F, value);
	}
}
