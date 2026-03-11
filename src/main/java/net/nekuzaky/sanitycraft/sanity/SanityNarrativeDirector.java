package net.nekuzaky.sanitycraft.sanity;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
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
		if (component.hasHallucinationShield()) {
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
		if (config.biomePersonalityEnabled && sanity <= 58 && random.nextFloat() < clamp01(config.biomePersonalityChance)) {
			playBiomePersonality(player, random);
		}
		if (config.paranoiaMimicEnabled && sanity <= 50 && component.canPlayMimic() && random.nextFloat() < clamp01(config.paranoiaMimicChance)) {
			playParanoiaMimic(player, random);
			component.resetMimicCooldown(random);
		}
		if (config.falseUiEventsEnabled && sanity <= 45 && component.canTriggerFalseUi() && random.nextFloat() < clamp01(config.falseUiEventChance)) {
			triggerFalseUiEvent(player, random);
			component.resetFalseUiCooldown(random);
		}

		if (!config.streamerSafeMode && sanity <= 20 && component.canJumpscare() && random.nextFloat() < clamp01(config.narrativeJumpscareChance)) {
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

	private static void playParanoiaMimic(ServerPlayer player, RandomSource random) {
		int roll = random.nextInt(5);
		if (roll == 0) {
			player.playNotifySound(SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE, 0.75F, 0.9F + random.nextFloat() * 0.12F);
			SanityJournal.log(player, "I swear I heard a creeper fuse.");
		} else if (roll == 1) {
			player.playNotifySound(SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.7F, 0.85F + random.nextFloat() * 0.2F);
			SanityJournal.log(player, "A chest opened somewhere nearby.");
		} else if (roll == 2) {
			player.playNotifySound(SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 0.7F, 0.8F + random.nextFloat() * 0.18F);
			SanityJournal.log(player, "A door moved in the dark.");
		} else if (roll == 3) {
			player.playNotifySound(SoundEvents.ITEM_BREAK.value(), SoundSource.PLAYERS, 0.65F, 0.65F + random.nextFloat() * 0.2F);
			SanityJournal.log(player, "Something snapped behind me.");
		} else {
			player.playNotifySound(SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 0.7F, 0.8F + random.nextFloat() * 0.2F);
			SanityJournal.log(player, "A trap sound echoed from nowhere.");
		}
	}

	private static void playBiomePersonality(ServerPlayer player, RandomSource random) {
		var biome = player.level().getBiome(player.blockPosition());
		if (biome.is(BiomeTags.IS_NETHER)) {
			player.playNotifySound(SoundEvents.GHAST_AMBIENT, SoundSource.HOSTILE, 0.65F, 0.75F + random.nextFloat() * 0.2F);
			SanityJournal.log(player, "The Nether whispered my name.");
		} else if (biome.is(BiomeTags.IS_OCEAN)) {
			player.playNotifySound(SoundEvents.DROWNED_AMBIENT_WATER, SoundSource.HOSTILE, 0.65F, 0.8F + random.nextFloat() * 0.2F);
			SanityJournal.log(player, "I heard breathing from the water.");
		} else if (biome.is(BiomeTags.IS_FOREST)) {
			player.playNotifySound(SoundEvents.FOX_SCREECH, SoundSource.AMBIENT, 0.6F, 0.7F + random.nextFloat() * 0.2F);
			SanityJournal.log(player, "The forest sounded wrong.");
		} else {
			player.playNotifySound(SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.6F, 0.72F + random.nextFloat() * 0.2F);
			SanityJournal.log(player, "The biome has a voice now.");
		}
	}

	private static void triggerFalseUiEvent(ServerPlayer player, RandomSource random) {
		int roll = random.nextInt(3);
		if (roll == 0) {
			player.displayClientMessage(Component.literal("Inventory corrupted. Re-indexing..."), true);
			player.playNotifySound(SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.55F, 0.7F + random.nextFloat() * 0.15F);
		} else if (roll == 1) {
			int fakePing = random.nextIntBetweenInclusive(420, 980);
			player.displayClientMessage(Component.literal("Server ping unstable: " + fakePing + "ms"), true);
			player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 0.5F, 0.5F);
		} else {
			player.displayClientMessage(Component.literal("Advancement made! The Mind Watches Back"), true);
			player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.6F, 0.65F + random.nextFloat() * 0.2F);
		}
		SanityJournal.log(player, "My UI lied to me again.");
	}

	private static float clamp01(float value) {
		if (value < 0.0F) {
			return 0.0F;
		}
		return Math.min(1.0F, value);
	}
}
