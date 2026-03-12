package net.nekuzaky.sanitycraft;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.nekuzaky.sanitycraft.registry.ModCommands;
import net.nekuzaky.sanitycraft.registry.ModCreativeTabs;
import net.nekuzaky.sanitycraft.registry.ModEntities;
import net.nekuzaky.sanitycraft.registry.ModItems;
import net.nekuzaky.sanitycraft.registry.ModParticles;
import net.nekuzaky.sanitycraft.util.ServerWorkQueue;

import net.fabricmc.api.ModInitializer;

public class SanitycraftMod implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger(SanitycraftMod.class);
	public static final String MODID = "sanitycraft";

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing {}", MODID);
		ModEntities.register();
		ModItems.register();
		ModParticles.register();
		ModCreativeTabs.register();
		ModCommands.register();
		ServerWorkQueue.register();
		net.nekuzaky.sanitycraft.sanity.SanityNetworking.initialize();
		net.nekuzaky.sanitycraft.sanity.SanityManager.initialize();
		net.nekuzaky.sanitycraft.sanity.SanityEvents.register();
	}
}
