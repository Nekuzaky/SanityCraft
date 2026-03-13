package com.sanitycraft.client.particles;

import com.sanitycraft.registry.ModParticles;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

@Environment(EnvType.CLIENT)
public final class ClientParticles {
	private ClientParticles() {
	}

	public static void register() {
		ParticleFactoryRegistry.getInstance().register(ModParticles.BLOOD, BloodParticle::provider);
	}
}
