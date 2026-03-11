package net.nekuzaky.sanitycraft.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.nekuzaky.sanitycraft.sanity.SanityConfig;
import net.nekuzaky.sanitycraft.sanity.SanityManager;

public class HorrorAmbienceDirector {
	private static boolean registered = false;
	private static int heartbeatCooldown = 0;
	private static int collapseHeartbeatCooldown = 0;
	private static int breathCooldown = 0;
	private static int trapCooldown = 0;
	private static int stingerCooldown = 0;
	private static int nearMissCooldown = 600;
	private static int caveMiningCooldown = 1200;
	private static int caveMiningBurstsRemaining = 0;
	private static int caveMiningBurstDelay = 0;
	private static double caveMiningX = 0.0D;
	private static double caveMiningY = 0.0D;
	private static double caveMiningZ = 0.0D;
	private static int lastSanityBand = 5;
	private static final SoundEvent STONE_BREAK_SOUND = resolveSound("block.stone.break", SoundEvents.WARDEN_STEP);
	private static final SoundEvent STONE_HIT_SOUND = resolveSound("block.stone.hit", SoundEvents.ZOMBIE_STEP);

	private HorrorAmbienceDirector() {
	}

	public static void register() {
		if (registered) {
			return;
		}
		registered = true;
		ClientTickEvents.END_CLIENT_TICK.register(HorrorAmbienceDirector::tick);
	}

	private static void tick(Minecraft client) {
		SanityClientState.tickVisualEffects(client);
		if (client.isPaused() || client.player == null || client.level == null) {
			return;
		}

		SanityConfig config = SanityManager.getConfig();
		if (!config.ambienceDirectorEnabled || !config.hallucinationsEnabled) {
			return;
		}

		LocalPlayer player = client.player;
		int sanity = SanityClientState.getSanity();
		if (sanity > Mth.clamp(config.ambienceMinSanityActive, 0, 100)) {
			lastSanityBand = sanity / 20;
			return;
		}

		float master = Mth.clamp(config.ambienceMasterVolume, 0.0F, 2.0F);
		float fear = Mth.clamp((100.0F - sanity) / 100.0F, 0.0F, 1.0F);
		boolean cave = isCaveLike(player);
		boolean night = isNight(player);
		boolean rain = player.level().isRaining();
		boolean hostilesNear = hasHostilesNearby(player, 18.0D);
		RandomSource random = player.getRandom();

		int band = sanity / 20;
		if (band < lastSanityBand && stingerCooldown <= 0) {
			playAtPlayer(player, SoundEvents.WARDEN_NEARBY_CLOSER, SoundSource.HOSTILE, (0.22F + fear * 0.25F) * master, 0.8F + random.nextFloat() * 0.15F);
			stingerCooldown = 120 + random.nextInt(120);
		}
		lastSanityBand = band;

		if (stingerCooldown > 0) {
			stingerCooldown--;
		}
		if (heartbeatCooldown > 0) {
			heartbeatCooldown--;
		}
		if (collapseHeartbeatCooldown > 0) {
			collapseHeartbeatCooldown--;
		}
		if (breathCooldown > 0) {
			breathCooldown--;
		}
		if (trapCooldown > 0) {
			trapCooldown--;
		}
		if (nearMissCooldown > 0) {
			nearMissCooldown--;
		}
		if (caveMiningCooldown > 0) {
			caveMiningCooldown--;
		}
		if (caveMiningBurstDelay > 0) {
			caveMiningBurstDelay--;
		}

		if (config.ambienceHeartbeatEnabled && sanity <= 35 && heartbeatCooldown <= 0) {
			float volume = (0.18F + fear * 0.55F + (hostilesNear ? 0.12F : 0.0F)) * master;
			float pitch = 0.84F + random.nextFloat() * 0.18F;
			playAtPlayer(player, SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, volume, pitch);
			heartbeatCooldown = Math.max(12, 50 - (int) (fear * 22.0F)) + random.nextInt(16);
		}
		tickZeroSanityCollapseHeartbeat(player, random, config, master, sanity);

		if (sanity <= 60 && breathCooldown <= 0) {
			SoundEvent breath = random.nextBoolean() ? SoundEvents.ENDERMAN_AMBIENT : SoundEvents.PHANTOM_AMBIENT;
			float volume = (0.08F + fear * 0.28F) * master;
			float pitch = 0.62F + random.nextFloat() * 0.18F;
			playAtPlayer(player, breath, SoundSource.AMBIENT, volume, pitch);
			breathCooldown = 90 + random.nextInt(120) + sanity;
		}

		if (config.ambienceSoundTrapsEnabled && sanity <= 45 && trapCooldown <= 0) {
			SoundEvent trap = pickTrapSound(random, hostilesNear);
			float volume = (0.12F + fear * 0.32F) * master;
			float pitch = 0.72F + random.nextFloat() * 0.35F;
			playAroundPlayer(player, trap, SoundSource.HOSTILE, volume, pitch, random, 5.0D, 11.0D);
			trapCooldown = 80 + random.nextInt(160) + (int) (sanity * 0.6F);
		}

		if (sanity <= 25 && (cave || night || rain) && stingerCooldown <= 0 && random.nextFloat() < (0.012F + fear * 0.02F)) {
			float volume = (0.18F + fear * 0.35F) * master;
			playAroundPlayer(player, SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE, volume, 0.85F + random.nextFloat() * 0.2F, random, 4.0D, 8.0D);
			stingerCooldown = 140 + random.nextInt(120);
		}

		if (config.nearMissEnabled && sanity <= 42 && nearMissCooldown <= 0 && random.nextFloat() < (0.008F + fear * 0.013F)) {
			triggerNearMiss(player, random, master);
			nearMissCooldown = randomRange(random, config.nearMissMinIntervalTicks, config.nearMissMaxIntervalTicks);
		}

		if (config.caveMiningHallucinationEnabled && cave && sanity <= 62 && caveMiningCooldown <= 0 && random.nextFloat() < (0.006F + fear * 0.014F)) {
			startCaveMiningSequence(player, random, config);
		}
		tickCaveMiningSequence(player, random, master);
	}

	private static SoundEvent pickTrapSound(RandomSource random, boolean hostilesNear) {
		int roll = random.nextInt(hostilesNear ? 5 : 4);
		if (roll == 0) {
			return SoundEvents.ZOMBIE_STEP;
		}
		if (roll == 1) {
			return SoundEvents.WARDEN_STEP;
		}
		if (roll == 2) {
			return SoundEvents.ENDERMAN_AMBIENT;
		}
		if (roll == 3) {
			return SoundEvents.WARDEN_NEARBY_CLOSER;
		}
		return SoundEvents.CREEPER_PRIMED;
	}

	private static boolean isCaveLike(LocalPlayer player) {
		BlockPos pos = player.blockPosition();
		return !player.level().canSeeSky(pos) && pos.getY() < player.level().getSeaLevel() + 2;
	}

	private static boolean isNight(LocalPlayer player) {
		long timeOfDay = player.level().getDayTime() % 24000L;
		return timeOfDay >= 13000L && timeOfDay <= 23000L;
	}

	private static boolean hasHostilesNearby(LocalPlayer player, double radius) {
		AABB area = player.getBoundingBox().inflate(Math.max(1.0D, radius));
		return !player.level().getEntitiesOfClass(Monster.class, area, monster -> monster.isAlive()).isEmpty();
	}

	private static void playAtPlayer(LocalPlayer player, SoundEvent sound, SoundSource source, float volume, float pitch) {
		player.playNotifySound(sound, source, Math.max(0.01F, volume), pitch);
	}

	private static void playAroundPlayer(LocalPlayer player, SoundEvent sound, SoundSource source, float volume, float pitch, RandomSource random, double minDistance, double maxDistance) {
		double angle = random.nextDouble() * Math.PI * 2.0D;
		double distance = minDistance + random.nextDouble() * (maxDistance - minDistance);
		double x = player.getX() + Math.cos(angle) * distance;
		double y = player.getY() + 0.5D;
		double z = player.getZ() + Math.sin(angle) * distance;
		player.level().playLocalSound(x, y, z, sound, source, Math.max(0.01F, volume), pitch, false);
	}

	private static void tickZeroSanityCollapseHeartbeat(LocalPlayer player, RandomSource random, SanityConfig config, float master, int sanity) {
		if (!config.zeroSanityDeathEnabled || sanity > 0) {
			return;
		}
		int delay = Math.max(1, config.zeroSanityDeathDelaySeconds);
		float progress = Mth.clamp(SanityClientState.getZeroSanityElapsedSeconds() / (float) delay, 0.0F, 1.0F);
		if (collapseHeartbeatCooldown > 0) {
			return;
		}

		float volume = (0.34F + progress * 0.56F) * master;
		float pitch = 0.78F + progress * 0.32F + random.nextFloat() * 0.06F;
		playAtPlayer(player, SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, volume, pitch);
		collapseHeartbeatCooldown = Math.max(4, 26 - (int) (progress * 18.0F)) + random.nextInt(4);
	}

	private static void triggerNearMiss(LocalPlayer player, RandomSource random, float master) {
		int side = random.nextBoolean() ? 1 : -1;
		SanityClientState.triggerNearMiss(side, 300);
		playBehindPlayer(player, side, SoundEvents.WARDEN_STEP, SoundSource.HOSTILE, 0.42F * master, 0.85F + random.nextFloat() * 0.20F, 3.4D);
		playBehindPlayer(player, side, SoundEvents.ZOMBIE_STEP, SoundSource.HOSTILE, 0.38F * master, 0.80F + random.nextFloat() * 0.22F, 4.1D);
	}

	private static void playBehindPlayer(LocalPlayer player, int side, SoundEvent sound, SoundSource source, float volume, float pitch, double distance) {
		float yawRad = player.getYRot() * ((float) Math.PI / 180.0F);
		double backwardX = -Math.sin(yawRad);
		double backwardZ = Math.cos(yawRad);
		double sideX = Math.cos(yawRad) * side;
		double sideZ = Math.sin(yawRad) * side;
		double x = player.getX() + backwardX * distance + sideX * 1.6D;
		double y = player.getY() + 0.4D;
		double z = player.getZ() + backwardZ * distance + sideZ * 1.6D;
		player.level().playLocalSound(x, y, z, sound, source, Math.max(0.01F, volume), pitch, false);
	}

	private static void startCaveMiningSequence(LocalPlayer player, RandomSource random, SanityConfig config) {
		double angle = random.nextDouble() * Math.PI * 2.0D;
		double distance = 8.0D + random.nextDouble() * 7.0D;
		caveMiningX = player.getX() + Math.cos(angle) * distance;
		caveMiningY = player.getY() + (random.nextDouble() * 2.0D - 1.0D);
		caveMiningZ = player.getZ() + Math.sin(angle) * distance;
		caveMiningBurstsRemaining = random.nextInt(4) + 3;
		caveMiningBurstDelay = 0;
		caveMiningCooldown = randomRange(random, config.caveMiningMinIntervalTicks, config.caveMiningMaxIntervalTicks);
	}

	private static void tickCaveMiningSequence(LocalPlayer player, RandomSource random, float master) {
		if (caveMiningBurstsRemaining <= 0 || caveMiningBurstDelay > 0) {
			return;
		}
		double jitterX = caveMiningX + (random.nextDouble() - 0.5D) * 0.8D;
		double jitterY = caveMiningY + (random.nextDouble() - 0.5D) * 0.4D;
		double jitterZ = caveMiningZ + (random.nextDouble() - 0.5D) * 0.8D;
		float volume = (0.24F + random.nextFloat() * 0.18F) * master;
		float pitch = 0.74F + random.nextFloat() * 0.20F;
		player.level().playLocalSound(jitterX, jitterY, jitterZ, STONE_BREAK_SOUND, SoundSource.BLOCKS, Math.max(0.01F, volume), pitch, false);
		if (random.nextBoolean()) {
			player.level().playLocalSound(jitterX, jitterY, jitterZ, STONE_HIT_SOUND, SoundSource.BLOCKS, Math.max(0.01F, volume * 0.7F), pitch + 0.15F, false);
		}
		caveMiningBurstsRemaining--;
		caveMiningBurstDelay = 8 + random.nextInt(11);
	}

	private static int randomRange(RandomSource random, int min, int max) {
		int lo = Math.max(20, Math.min(min, max));
		int hi = Math.max(lo + 1, Math.max(min, max));
		return random.nextInt(hi - lo) + lo;
	}

	private static SoundEvent resolveSound(String id, SoundEvent fallback) {
		try {
			SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(ResourceLocation.parse(id));
			return sound != null ? sound : fallback;
		} catch (Exception ignored) {
			return fallback;
		}
	}
}
