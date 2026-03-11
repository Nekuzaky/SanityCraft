/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.nekuzaky.sanitycraft.init;

import net.nekuzaky.sanitycraft.client.particle.BloodParticle;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
public class SanitycraftModParticles {
	public static void clientLoad() {
		ParticleFactoryRegistry.getInstance().register(SanitycraftModParticleTypes.BLOOD, BloodParticle::provider);
	}
}