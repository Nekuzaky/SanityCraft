package com.sanitycraft;

import com.sanitycraft.data.config.SanityCraftConfig;
import com.sanitycraft.network.sync.SanitySyncService;
import com.sanitycraft.registry.ModBlocks;
import com.sanitycraft.registry.ModCommands;
import com.sanitycraft.registry.ModCreativeTabs;
import com.sanitycraft.registry.ModEffects;
import com.sanitycraft.registry.ModEntities;
import com.sanitycraft.registry.ModGameRules;
import com.sanitycraft.registry.ModItems;
import com.sanitycraft.registry.ModParticles;
import com.sanitycraft.registry.ModSounds;
import com.sanitycraft.sanity.SanityEvents;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class SanityCraft implements ModInitializer {
	public static final String MOD_ID = "sanitycraft";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		SanityCraftConfig.loadOrCreate();
		ModBlocks.register();
		ModEffects.register();
		ModSounds.register();
		ModParticles.register();
		ModEntities.register();
		ModItems.register();
		ModCreativeTabs.register();
		ModGameRules.register();
		SanitySyncService.register();
		SanityEvents.register();
		ModCommands.register();
		LOGGER.info("Prepared new foundation bootstrap for {}", MOD_ID);
	}
}
