package net.nekuzaky.sanitycraft.registry;

import net.nekuzaky.sanitycraft.entity.StalkerEntity;
import net.nekuzaky.sanitycraft.entity.BloodyCreeperEntity;
import net.nekuzaky.sanitycraft.SanitycraftMod;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public final class ModEntities {
	public static final EntityType<StalkerEntity> STALKER = register("stalker",
			EntityType.Builder.<StalkerEntity>of(StalkerEntity::new, MobCategory.MONSTER).clientTrackingRange(64).updateInterval(3).ridingOffset(-0.6f).sized(0.6f, 1.8f));
	public static final EntityType<BloodyCreeperEntity> BLOODY_CREEPER = register("bloody_creeper",
			EntityType.Builder.<BloodyCreeperEntity>of(BloodyCreeperEntity::new, MobCategory.MONSTER).clientTrackingRange(64).updateInterval(3).sized(0.6f, 1.7f));

	private ModEntities() {
	}

	public static void register() {
		registerAttributes();
	}

	private static <T extends Entity> EntityType<T> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(SanitycraftMod.MODID, registryname),
				(EntityType<T>) entityTypeBuilder.build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(SanitycraftMod.MODID, registryname))));
	}

	public static void registerAttributes() {
		FabricDefaultAttributeRegistry.register(STALKER, StalkerEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(BLOODY_CREEPER, BloodyCreeperEntity.createAttributes());
	}
}
