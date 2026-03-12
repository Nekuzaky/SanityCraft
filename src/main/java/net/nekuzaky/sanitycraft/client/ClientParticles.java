package net.nekuzaky.sanitycraft.client;

import net.nekuzaky.sanitycraft.client.particle.BloodParticle;
import net.nekuzaky.sanitycraft.registry.ModParticles;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
public final class ClientParticles {
	private ClientParticles() {
	}

	public static void register() {
		ParticleFactoryRegistry.getInstance().register(ModParticles.BLOOD, BloodParticle::provider);
	}
}
