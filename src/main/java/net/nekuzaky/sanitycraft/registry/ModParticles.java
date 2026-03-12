package net.nekuzaky.sanitycraft.registry;

import net.nekuzaky.sanitycraft.SanitycraftMod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.Registry;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;

public final class ModParticles {
	public static final SimpleParticleType BLOOD = register("blood", FabricParticleTypes.simple());

	private ModParticles() {
	}

	public static void register() {
	}

	private static SimpleParticleType register(String registryname, SimpleParticleType element) {
		return Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(SanitycraftMod.MODID, registryname), element);
	}
}
