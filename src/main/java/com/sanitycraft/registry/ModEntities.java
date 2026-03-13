package com.sanitycraft.registry;

import com.sanitycraft.SanityCraft;
import com.sanitycraft.entity.bloodycreeper.BloodyCreeperEntity;
import com.sanitycraft.entity.stalker.StalkerEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
	public static final EntityType<StalkerEntity> STALKER = register(
			"stalker",
			EntityType.Builder.<StalkerEntity>of(StalkerEntity::new, MobCategory.MONSTER)
					.clientTrackingRange(64)
					.updateInterval(3)
					.ridingOffset(-0.6F)
					.sized(0.6F, 1.8F));
	public static final EntityType<BloodyCreeperEntity> BLOODY_CREEPER = register(
			"bloody_creeper",
			EntityType.Builder.<BloodyCreeperEntity>of(BloodyCreeperEntity::new, MobCategory.MONSTER)
					.clientTrackingRange(64)
					.updateInterval(3)
					.sized(0.6F, 1.7F));

	private ModEntities() {
	}

	public static void register() {
		FabricDefaultAttributeRegistry.register(STALKER, StalkerEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(BLOODY_CREEPER, BloodyCreeperEntity.createAttributes());
	}

	private static <T extends Entity> EntityType<T> register(String path, EntityType.Builder<T> builder) {
		return Registry.register(
				BuiltInRegistries.ENTITY_TYPE,
				SanityCraft.id(path),
				builder.build(ResourceKey.create(Registries.ENTITY_TYPE, SanityCraft.id(path))));
	}
}
