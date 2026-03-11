/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.nekuzaky.sanitycraft.init;

import net.nekuzaky.sanitycraft.SanitycraftMod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.Registry;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;

public class SanitycraftModParticleTypes {
	public static SimpleParticleType BLOOD;

	public static void load() {
		register("blood", FabricParticleTypes.simple());
	}

	private static SimpleParticleType register(String registryname, SimpleParticleType element) {
		return Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(SanitycraftMod.MODID, registryname), element);
	}
}