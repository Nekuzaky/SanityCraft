package com.sanitycraft.client.particles;

import com.sanitycraft.client.hud.ClientSanityState;
import com.sanitycraft.entity.observer.ObserverEntity;
import com.sanitycraft.registry.ModParticles;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public final class SanityBloodParticleEmitter {
	private static final int DARK_BLOOD = 0x8B0000;
	private static final int DARK_BLOOD_DEEP = 0x5A0000;

	private SanityBloodParticleEmitter() {
	}

	public static void tick(Minecraft client) {
		if (client.isPaused() || client.level == null || client.player == null) {
			return;
		}

		int sanity = ClientSanityState.getSanity();
		if (sanity > 50) {
			return;
		}

		for (ObserverEntity observer : client.level.getEntitiesOfClass(
				ObserverEntity.class,
				client.player.getBoundingBox().inflate(96.0D),
				entity -> entity.isAlive() && !entity.isDoppelganger() && entity.distanceToSqr(client.player) <= 64.0D * 64.0D)) {
			emitAroundObserver(client, observer, sanity, client.level.random);
		}
	}

	private static void emitAroundObserver(Minecraft client, ObserverEntity observer, int sanity, RandomSource random) {
		float emissionChance;
		int particleCount;
		boolean allowDrip = false;

		if (sanity > 25) {
			emissionChance = 0.16F;
			particleCount = 1 + random.nextInt(2);
		} else if (sanity > 10) {
			emissionChance = 0.34F;
			particleCount = 2 + random.nextInt(3);
		} else {
			emissionChance = 0.48F;
			particleCount = 2 + random.nextInt(3);
			allowDrip = true;
		}

		if (random.nextFloat() >= emissionChance) {
			return;
		}

		for (int i = 0; i < particleCount; i++) {
			double radius = 0.20D + random.nextDouble() * 0.40D;
			double angle = random.nextDouble() * Math.PI * 2.0D;
			double x = observer.getX() + Math.cos(angle) * radius;
			double z = observer.getZ() + Math.sin(angle) * radius;
			double y = observer.getY() + observer.getBbHeight() * (0.50D + random.nextDouble() * 0.26D);
			double xd = (random.nextDouble() - 0.5D) * 0.006D;
			double yd = -0.006D - random.nextDouble() * 0.012D;
			double zd = (random.nextDouble() - 0.5D) * 0.006D;
			float scale = sanity > 25 ? 0.45F + random.nextFloat() * 0.10F : 0.52F + random.nextFloat() * 0.16F;
			int color = random.nextBoolean() ? DARK_BLOOD : DARK_BLOOD_DEEP;
			client.level.addParticle(new DustParticleOptions(color, scale), x, y, z, xd, yd, zd);
		}

		if (allowDrip && random.nextFloat() < 0.10F) {
			double radius = 0.18D + random.nextDouble() * 0.18D;
			double angle = random.nextDouble() * Math.PI * 2.0D;
			double x = observer.getX() + Math.cos(angle) * radius;
			double z = observer.getZ() + Math.sin(angle) * radius;
			double y = observer.getY() + observer.getBbHeight() * (0.58D + random.nextDouble() * 0.20D);
			client.level.addParticle(ModParticles.BLOOD, x, y, z, 0.0D, -0.055D - random.nextDouble() * 0.035D, 0.0D);
		}
	}
}
