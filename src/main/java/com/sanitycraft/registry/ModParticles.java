package com.sanitycraft.registry;

import com.sanitycraft.SanityCraft;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModParticles {
	public static final SimpleParticleType BLOOD = register("blood", FabricParticleTypes.simple());

	private ModParticles() {
	}

	public static void register() {
	}

	private static SimpleParticleType register(String path, SimpleParticleType particleType) {
		return Registry.register(BuiltInRegistries.PARTICLE_TYPE, SanityCraft.id(path), particleType);
	}
}
