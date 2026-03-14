package com.sanitycraft.client.events;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sanitycraft.client.hud.ClientSanityState;
import java.util.ArrayDeque;
import com.sanitycraft.entity.observer.ObserverEntity;
import com.sanitycraft.network.packet.ClientboundSanityEventPacket;
import com.sanitycraft.registry.ModEntities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public final class SanityClientEventState {
	private static final int BLOOD_COLOR = 0x5A0000;
	private static final ItemStack FAKE_BONE = new ItemStack(Items.BONE);
	private static final ItemStack FAKE_ROTTEN_FLESH = new ItemStack(Items.ROTTEN_FLESH);
	private static final SoundSource[] SILENT_WORLD_SOURCES = {
			SoundSource.AMBIENT,
			SoundSource.BLOCKS,
			SoundSource.HOSTILE,
			SoundSource.NEUTRAL,
			SoundSource.PLAYERS,
			SoundSource.WEATHER,
			SoundSource.MUSIC
	};

	private static final List<FloatingBlockIllusion> FLOATING_BLOCKS = new ArrayList<>();
	private static final List<PulsingWallIllusion> BREATHING_WALLS = new ArrayList<>();
	private static final List<LocalHallucination> LOCAL_HALLUCINATIONS = new ArrayList<>();
	private static final Map<Integer, Long> DISTORTED_ENTITIES = new HashMap<>();

	private static ClientLevel trackedLevel;
	private static long skyBlinkUntilTick;
	private static long inventoryHallucinationUntilTick;
	private static int inventorySeed;
	private static long phantomHotbarUntilTick;
	private static int phantomHotbarSlot = -1;
	private static int phantomHotbarSeed;
	private static long bloodParticlesUntilTick;
	private static int bloodParticleIntensity;
	private static long silentWorldStartTick;
	private static long silentWorldEndTick;
	private static long silentWorldSilenceEndTick;
	private static long silentWorldReturnStartTick;
	private static long silentWorldPresenceTick;
	private static BlockPos silentWorldPresenceAnchor = BlockPos.ZERO;
	private static boolean silentWorldPresenceSpawned;
	private static long wrongSunEndTick;
	private static long wrongSunStartTick;
	private static long wrongSunBaseDayTime;
	private static long wrongSunOffset;
	private static EchoStepState echoStepState;
	private static int nextHallucinationEntityId = -40_000;

	private SanityClientEventState() {
	}

	public static void handleEvent(ClientboundSanityEventPacket payload, Minecraft client) {
		if (client.level == null || client.player == null) {
			return;
		}

		long now = client.level.getGameTime();
		BlockPos anchor = new BlockPos(payload.x(), payload.y(), payload.z());
		switch (payload.eventId()) {
			case "floating_block" -> createFloatingBlocks(client, anchor, now, payload.durationTicks(), payload.intensity(), payload.variant());
			case "fake_creeper" -> spawnFakeCreeper(client, anchor, now, payload.durationTicks());
			case "sky_blink" -> skyBlinkUntilTick = Math.max(skyBlinkUntilTick, now + Math.max(2, payload.durationTicks()));
			case "shadow_runner" -> spawnShadowRunner(client, anchor, now, payload.durationTicks(), payload.variant());
			case "inventory_hallucination" -> {
				inventoryHallucinationUntilTick = Math.max(inventoryHallucinationUntilTick, now + Math.max(20, payload.durationTicks()));
				inventorySeed = payload.variant();
			}
			case "fake_player" -> spawnFakePlayer(client, anchor, now, payload.durationTicks(), payload.text());
			case "breathing_walls" -> createBreathingWalls(client, anchor, now, payload.durationTicks(), payload.variant());
			case "phantom_hotbar_slot" -> {
				phantomHotbarUntilTick = Math.max(phantomHotbarUntilTick, now + Math.max(12, payload.durationTicks()));
				phantomHotbarSlot = Mth.clamp(payload.variant(), 0, 8);
				phantomHotbarSeed = payload.intensity();
			}
			case "entity_distortion" -> distortNearbyHostiles(client, now, payload.durationTicks(), payload.intensity());
			case "mirror_player" -> spawnMirrorPlayer(client, anchor, now, payload.durationTicks());
			case "blood_particle" -> {
				bloodParticlesUntilTick = Math.max(bloodParticlesUntilTick, now + Math.max(16, payload.durationTicks()));
				bloodParticleIntensity = Math.max(bloodParticleIntensity, Math.max(1, payload.intensity()));
			}
			case "silent_world" -> beginSilentWorld(client, anchor, now, payload.durationTicks(), payload.intensity(), payload.variant());
			case "echo_step" -> echoStepState = new EchoStepState(now + Math.max(80, payload.durationTicks()), Math.max(40, payload.intensity()));
			case "wrong_sun" -> beginWrongSun(client, now, Math.max(1, payload.durationTicks()), payload.variant());
			case "almost_mob" -> SanityHallucinatedEntityManager.spawnAlmostMob(client, anchor, now, payload.durationTicks(), payload.variant(), payload.text());
			case "memory_whisper" -> client.player.displayClientMessage(
					Component.translatable(payload.text()).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
					true);
			case "collapse_false_hud" -> ClientSanityState.triggerCollapseFalseHud(payload.durationTicks(), payload.intensity(), payload.variant());
			case "collapse_silence" -> ClientSanityState.triggerCollapseSilence(payload.durationTicks(), payload.intensity());
			case "time_skip" -> client.level.setTimeFromServer(client.level.getGameTime() + payload.durationTicks(), client.level.getDayTime() + payload.durationTicks(), true);
			default -> {
			}
		}
	}

	public static void tick(Minecraft client) {
		if (client.level == null || client.player == null) {
			clear(client);
			return;
		}
		if (trackedLevel != client.level) {
			clear(client);
			trackedLevel = client.level;
		}

		long now = client.level.getGameTime();
		expireIllusions(FLOATING_BLOCKS, now);
		expireIllusions(BREATHING_WALLS, now);
		expireDistortedEntities(now);
		tickHallucinations(client, now);
		SanityHallucinatedEntityManager.tick(client, now);
		tickSilentWorld(client, now);
		tickWrongSun(client, now);
		tickEchoStep(client, now);
		if (bloodParticlesUntilTick > now) {
			spawnBloodParticles(client, now);
		}
	}

	public static void renderWorld(WorldRenderContext context) {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null || client.player == null) {
			return;
		}

		PoseStack poseStack = context.matrixStack();
		MultiBufferSource consumers = context.consumers();
		BlockRenderDispatcher blockRenderer = client.getBlockRenderer();
		Vec3 cameraPos = context.camera().getPosition();
		long now = client.level.getGameTime();

		for (FloatingBlockIllusion illusion : FLOATING_BLOCKS) {
			float time = (now - illusion.startTick()) + client.getFrameTimeNs() / 1_000_000_000.0F;
			float life = 1.0F - Mth.clamp((illusion.endTick() - now) / (float) Math.max(1L, illusion.endTick() - illusion.startTick()), 0.0F, 1.0F);
			double bob = Math.sin(time * 0.12F + illusion.phase()) * 0.14D + 0.18D;
			double orbitX = Math.cos(time * 0.07F + illusion.phase()) * illusion.orbitRadius();
			double orbitZ = Math.sin(time * 0.08F + illusion.phase()) * illusion.orbitRadius();
			poseStack.pushPose();
			poseStack.translate(
					illusion.origin().getX() + 0.5D - cameraPos.x + orbitX,
					illusion.origin().getY() + 0.5D - cameraPos.y + bob + life * 0.12D,
					illusion.origin().getZ() + 0.5D - cameraPos.z + orbitZ);
			poseStack.mulPose(Axis.YP.rotationDegrees(time * 5.0F + illusion.phase() * 22.0F));
			poseStack.scale(0.88F, 0.88F, 0.88F);
			blockRenderer.renderSingleBlock(illusion.state(), poseStack, consumers, LevelRenderer.getLightColor(client.level, illusion.origin()), OverlayTexture.NO_OVERLAY);
			poseStack.popPose();
		}

		for (PulsingWallIllusion illusion : BREATHING_WALLS) {
			float time = (now - illusion.startTick()) + client.getFrameTimeNs() / 1_000_000_000.0F;
			float pulse = 1.0F + (float) Math.sin(time * 0.18F + illusion.phase()) * 0.035F;
			double sway = Math.sin(time * 0.11F + illusion.phase()) * 0.018D;
			poseStack.pushPose();
			poseStack.translate(
					illusion.pos().getX() + 0.5D - cameraPos.x,
					illusion.pos().getY() + 0.5D - cameraPos.y + sway,
					illusion.pos().getZ() + 0.5D - cameraPos.z);
			poseStack.scale(pulse, pulse, pulse);
			poseStack.translate(-0.5D, -0.5D, -0.5D);
			blockRenderer.renderSingleBlock(illusion.state(), poseStack, consumers, LevelRenderer.getLightColor(client.level, illusion.pos()), OverlayTexture.NO_OVERLAY);
			poseStack.popPose();
		}
	}

	public static void renderHudOverlay(GuiGraphics guiGraphics, Minecraft client) {
		if (client.level == null || client.player == null) {
			return;
		}
		long now = client.level.getGameTime();
		if (skyBlinkUntilTick > now) {
			guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), 0xF0000000);
		}
		if (silentWorldEndTick > now) {
			int alpha = Mth.clamp((int) (getSilentWorldSuppression(now) * 132.0F), 20, 132);
			guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), (alpha << 24) | 0x040404);
		}
		if (phantomHotbarUntilTick > now && phantomHotbarSlot >= 0) {
			int centerX = guiGraphics.guiWidth() / 2;
			int x = centerX - 91 + phantomHotbarSlot * 20;
			int y = guiGraphics.guiHeight() - 22;
			guiGraphics.renderOutline(x - 1, y - 1, 24, 24, 0x88A64A4A);
			guiGraphics.fill(x, y, x + 22, y + 22, 0x22100000);
			ItemStack ghost = ((phantomHotbarSlot + phantomHotbarSeed) & 1) == 0 ? FAKE_BONE : FAKE_ROTTEN_FLESH;
			guiGraphics.renderItem(ghost, x + 3, y + 3);
		}
	}

	public static void renderInventoryHallucination(GuiGraphics guiGraphics, Slot slot) {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null || client.player == null || client.level.getGameTime() >= inventoryHallucinationUntilTick || !slot.hasItem()) {
			return;
		}
		int selector = Math.floorMod(slot.index + inventorySeed + (int) client.level.getGameTime(), 5);
		if (selector > 1) {
			return;
		}
		ItemStack fake = (selector & 1) == 0 ? FAKE_ROTTEN_FLESH : FAKE_BONE;
		guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x330A0000);
		guiGraphics.renderItem(fake, slot.x, slot.y);
	}

	public static boolean pushEntityDistortion(Entity entity, PoseStack poseStack) {
		if (!(entity instanceof net.minecraft.world.entity.monster.Monster) || trackedLevel == null) {
			return false;
		}
		Long endTick = DISTORTED_ENTITIES.get(entity.getId());
		if (endTick == null || trackedLevel.getGameTime() >= endTick) {
			return false;
		}
		float wave = (float) Math.sin((trackedLevel.getGameTime() + entity.getId()) * 0.45F);
		float horizontal = 0.88F + wave * 0.04F;
		float vertical = 1.12F - wave * 0.06F;
		poseStack.pushPose();
		poseStack.scale(horizontal, vertical, horizontal);
		return true;
	}

	public static boolean shouldSuppressWorldParticles() {
		return trackedLevel != null && silentWorldEndTick > trackedLevel.getGameTime() && trackedLevel.getGameTime() < silentWorldReturnStartTick;
	}

	private static void beginSilentWorld(Minecraft client, BlockPos anchor, long now, int durationTicks, int silenceTicks, int variant) {
		silentWorldStartTick = now;
		silentWorldEndTick = now + Math.max(80, durationTicks);
		silentWorldSilenceEndTick = Math.min(silentWorldEndTick - 20L, now + Math.max(40, silenceTicks));
		silentWorldReturnStartTick = Math.max(silentWorldSilenceEndTick + 20L, silentWorldEndTick - 20L);
		silentWorldPresenceTick = Math.min(silentWorldReturnStartTick - 2L, silentWorldSilenceEndTick + 18L + Math.floorMod(variant, 10));
		silentWorldPresenceAnchor = anchor.immutable();
		silentWorldPresenceSpawned = false;
		SanityHallucinatedEntityManager.clear(client.level);
		updateSilentWorldAudio(client, now);
	}

	private static void tickSilentWorld(Minecraft client, long now) {
		if (silentWorldEndTick <= now) {
			if (silentWorldEndTick != 0L) {
				restoreAudioVolumes(client);
			}
			silentWorldStartTick = 0L;
			silentWorldEndTick = 0L;
			silentWorldSilenceEndTick = 0L;
			silentWorldReturnStartTick = 0L;
			silentWorldPresenceTick = 0L;
			silentWorldPresenceSpawned = false;
			return;
		}

		if (!silentWorldPresenceSpawned && now >= silentWorldPresenceTick && now < silentWorldReturnStartTick) {
			SanityHallucinatedEntityManager.spawnSilentWorldPresence(client, silentWorldPresenceAnchor, now, (int) Math.max(20L, silentWorldReturnStartTick - now), (int) (now - silentWorldStartTick));
			silentWorldPresenceSpawned = true;
		}

		updateSilentWorldAudio(client, now);
	}

	private static float getSilentWorldSuppression(long now) {
		if (silentWorldEndTick <= now || silentWorldStartTick == 0L) {
			return 0.0F;
		}
		if (now <= silentWorldStartTick + 16L) {
			return Mth.clamp((now - silentWorldStartTick) / 16.0F, 0.0F, 1.0F);
		}
		if (now >= silentWorldReturnStartTick) {
			return 1.0F - Mth.clamp((now - silentWorldReturnStartTick) / (float) Math.max(1L, silentWorldEndTick - silentWorldReturnStartTick), 0.0F, 1.0F);
		}
		return 1.0F;
	}

	private static void updateSilentWorldAudio(Minecraft client, long now) {
		if (client.getSoundManager() == null) {
			return;
		}

		float suppression = getSilentWorldSuppression(now);
		float factor = 1.0F - suppression * 0.94F;
		for (SoundSource source : SILENT_WORLD_SOURCES) {
			client.getSoundManager().updateSourceVolume(source, client.options.getSoundSourceVolume(source) * factor);
		}
	}

	private static void restoreAudioVolumes(Minecraft client) {
		if (client == null || client.getSoundManager() == null) {
			return;
		}
		for (SoundSource source : SILENT_WORLD_SOURCES) {
			client.getSoundManager().updateSourceVolume(source, client.options.getSoundSourceVolume(source));
		}
	}

	private static void beginWrongSun(Minecraft client, long now, int durationTicks, int variant) {
		wrongSunStartTick = now;
		wrongSunEndTick = now + Math.max(1, durationTicks);
		wrongSunBaseDayTime = client.level.getDayTime();
		long dayTime = Math.floorMod(wrongSunBaseDayTime, 24_000L);
		wrongSunOffset = dayTime < 12_000L
				? 12_000L + 1_000L + Math.floorMod(variant, 2_000)
				: -12_000L + 1_000L + Math.floorMod(variant, 1_400);
	}

	private static void tickWrongSun(Minecraft client, long now) {
		if (wrongSunEndTick == 0L) {
			return;
		}

		long elapsed = now - wrongSunStartTick;
		if (now <= wrongSunEndTick) {
			client.level.setTimeFromServer(now, wrongSunBaseDayTime + elapsed + wrongSunOffset, true);
			return;
		}

		client.level.setTimeFromServer(now, wrongSunBaseDayTime + elapsed, true);
		wrongSunEndTick = 0L;
		wrongSunStartTick = 0L;
		wrongSunBaseDayTime = 0L;
		wrongSunOffset = 0L;
	}

	private static void tickEchoStep(Minecraft client, long now) {
		if (echoStepState == null) {
			return;
		}

		if (echoStepState.endTick() <= now) {
			echoStepState = null;
			return;
		}

		echoStepState.captureStep(client, now);
		if (!echoStepState.playDueSteps(client, now)) {
			echoStepState = null;
		}
	}

	private static void createFloatingBlocks(Minecraft client, BlockPos anchor, long now, int durationTicks, int intensity, int variant) {
		RandomSource random = RandomSource.create(variant);
		List<BlockPos> sampled = sampleNearbySolidBlocks(client.level, anchor, 5, Math.max(2, intensity + 1), random);
		for (BlockPos pos : sampled) {
			BlockState state = client.level.getBlockState(pos);
			if (!state.isAir()) {
				FLOATING_BLOCKS.add(new FloatingBlockIllusion(pos.immutable(), state, now, now + Math.max(30, durationTicks), 0.08F + random.nextFloat() * 0.08F, random.nextFloat() * 6.28F));
			}
		}
	}

	private static void createBreathingWalls(Minecraft client, BlockPos anchor, long now, int durationTicks, int variant) {
		RandomSource random = RandomSource.create(variant);
		List<BlockPos> sampled = sampleNearbySolidBlocks(client.level, anchor, 6, 10, random);
		for (BlockPos pos : sampled) {
			if (!client.level.canSeeSky(pos.above()) && client.level.getBlockState(pos).blocksMotion()) {
				BREATHING_WALLS.add(new PulsingWallIllusion(pos.immutable(), client.level.getBlockState(pos), now, now + Math.max(30, durationTicks), random.nextFloat() * 6.28F));
			}
		}
	}

	private static void distortNearbyHostiles(Minecraft client, long now, int durationTicks, int intensity) {
		int maxTargets = Math.max(1, intensity);
		for (Entity entity : client.level.entitiesForRendering()) {
			if (!(entity instanceof net.minecraft.world.entity.monster.Monster monster) || !monster.isAlive() || monster.distanceToSqr(client.player) > 22.0D * 22.0D) {
				continue;
			}
			DISTORTED_ENTITIES.put(entity.getId(), now + Math.max(24, durationTicks));
			maxTargets--;
			if (maxTargets <= 0) {
				break;
			}
		}
	}

	private static void spawnFakeCreeper(Minecraft client, BlockPos anchor, long now, int durationTicks) {
		Creeper creeper = new Creeper(EntityType.CREEPER, client.level);
		creeper.setId(nextEntityId());
		creeper.setPos(anchor.getX() + 0.5D, anchor.getY(), anchor.getZ() + 0.5D);
		creeper.noPhysics = true;
		creeper.setSilent(true);
		creeper.setNoAi(true);
		client.level.addEntity(creeper);
		LOCAL_HALLUCINATIONS.add(new LocalHallucination(creeper, now, now + Math.max(28, durationTicks)) {
			@Override
			void tick(Minecraft minecraft, long gameTime) {
				Creeper fake = (Creeper) entity();
				fake.noPhysics = true;
				fake.setSwellDir(1);
				facePlayer(fake, minecraft.player.position());
				if (gameTime >= endTick() - 4L) {
					for (int i = 0; i < 3; i++) {
						minecraft.level.addParticle(ParticleTypes.SMOKE, fake.getX(), fake.getY() + 0.8D, fake.getZ(), 0.0D, 0.03D, 0.0D);
					}
				}
				if (gameTime >= endTick()) {
					minecraft.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, fake.getX(), fake.getY() + 0.6D, fake.getZ(), 0.0D, 0.0D, 0.0D);
					minecraft.level.playLocalSound(fake.getX(), fake.getY(), fake.getZ(), net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(), net.minecraft.sounds.SoundSource.HOSTILE, 0.55F, 0.92F, false);
				}
			}
		});
	}

	private static void spawnShadowRunner(Minecraft client, BlockPos anchor, long now, int durationTicks, int variant) {
		ObserverEntity observer = new ObserverEntity(ModEntities.OBSERVER, client.level);
		observer.setId(nextEntityId());
		observer.setPos(anchor.getX() + 0.5D, anchor.getY(), anchor.getZ() + 0.5D);
		observer.noPhysics = true;
		observer.setSilent(true);
		client.level.addEntity(observer);

		RandomSource random = RandomSource.create(variant);
		Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
		Vec3 start = observer.position();
		Vec3 end = start.add(direction.getStepX() * (5.0D + random.nextDouble() * 3.0D), 0.0D, direction.getStepZ() * (5.0D + random.nextDouble() * 3.0D));
		LOCAL_HALLUCINATIONS.add(new LinearHallucination(observer, now, now + Math.max(18, durationTicks), start, end));
	}

	private static void spawnFakePlayer(Minecraft client, BlockPos anchor, long now, int durationTicks, String name) {
		GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes(("sanity_fake_" + name).getBytes()), name == null || name.isBlank() ? "Unknown" : name);
		RemotePlayer player = new RemotePlayer(client.level, profile);
		player.setId(nextEntityId());
		player.setPos(anchor.getX() + 0.5D, anchor.getY(), anchor.getZ() + 0.5D);
		player.noPhysics = true;
		player.setSilent(true);
		player.setCustomName(Component.literal(profile.getName()));
		player.setCustomNameVisible(true);
		client.level.addEntity(player);
		LOCAL_HALLUCINATIONS.add(new WatchingPlayerHallucination(player, now, now + Math.max(36, durationTicks)));
	}

	private static void spawnMirrorPlayer(Minecraft client, BlockPos anchor, long now, int durationTicks) {
		GameProfile profile = client.player.getGameProfile();
		RemotePlayer player = new RemotePlayer(client.level, profile);
		player.setId(nextEntityId());
		player.setPos(anchor.getX() + 0.5D, anchor.getY(), anchor.getZ() + 0.5D);
		player.noPhysics = true;
		player.setSilent(true);
		client.level.addEntity(player);
		LOCAL_HALLUCINATIONS.add(new WatchingPlayerHallucination(player, now, now + Math.max(40, durationTicks)));
	}

	private static void tickHallucinations(Minecraft client, long now) {
		Iterator<LocalHallucination> iterator = LOCAL_HALLUCINATIONS.iterator();
		while (iterator.hasNext()) {
			LocalHallucination hallucination = iterator.next();
			if (hallucination.entity().level() != client.level || now > hallucination.endTick()) {
				hallucination.remove(client.level);
				iterator.remove();
				continue;
			}
			hallucination.tick(client, now);
		}
	}

	private static void spawnBloodParticles(Minecraft client, long now) {
		RandomSource random = client.player.getRandom();
		int count = bloodParticleIntensity >= 3 ? 3 : 2;
		for (int i = 0; i < count; i++) {
			double angle = random.nextDouble() * Math.PI * 2.0D;
			double radius = 0.25D + random.nextDouble() * 0.55D;
			double x = client.player.getX() + Math.cos(angle) * radius;
			double y = client.player.getY() + 0.6D + random.nextDouble() * 1.1D;
			double z = client.player.getZ() + Math.sin(angle) * radius;
			client.level.addParticle(new DustParticleOptions(BLOOD_COLOR, 0.6F + random.nextFloat() * 0.35F), x, y, z, 0.0D, -0.012D - random.nextDouble() * 0.020D, 0.0D);
		}
		if (bloodParticleIntensity >= 3 && (now & 3L) == 0L) {
			client.level.addParticle(new DustParticleOptions(BLOOD_COLOR, 0.95F), client.player.getX(), client.player.getY() + 1.4D, client.player.getZ(), 0.0D, -0.05D, 0.0D);
		}
	}

	private static SoundEvent resolveStepSound(ClientLevel level, BlockPos pos) {
		BlockState below = level.getBlockState(pos.below());
		if (below.is(BlockTags.PLANKS)) {
			return SoundEvents.WOOD_STEP;
		}
		if (below.is(BlockTags.LEAVES)
				|| below.is(Blocks.GRASS_BLOCK)
				|| below.is(Blocks.DIRT)
				|| below.is(Blocks.COARSE_DIRT)
				|| below.is(Blocks.PODZOL)) {
			return SoundEvents.GRASS_STEP;
		}
		return SoundEvents.STONE_STEP;
	}

	private static Vec3 centerOf(BlockPos pos) {
		return new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
	}

	private static List<BlockPos> sampleNearbySolidBlocks(ClientLevel level, BlockPos center, int radius, int maxCount, RandomSource random) {
		List<BlockPos> candidates = new ArrayList<>();
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius / 2; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					BlockPos pos = center.offset(x, y, z);
					BlockState state = level.getBlockState(pos);
					if (state.isAir() || !state.blocksMotion()) {
						continue;
					}
					candidates.add(pos.immutable());
				}
			}
		}
		List<BlockPos> sampled = new ArrayList<>();
		while (!candidates.isEmpty() && sampled.size() < maxCount) {
			sampled.add(candidates.remove(random.nextInt(candidates.size())));
		}
		return sampled;
	}

	private static void expireIllusions(List<? extends TimedIllusion> illusions, long now) {
		for (Iterator<? extends TimedIllusion> iterator = illusions.iterator(); iterator.hasNext();) {
			TimedIllusion illusion = iterator.next();
			if (now > illusion.endTick()) {
				iterator.remove();
			}
		}
	}

	private static void expireDistortedEntities(long now) {
		DISTORTED_ENTITIES.entrySet().removeIf(entry -> now > entry.getValue());
	}

	private static void clear(Minecraft client) {
		restoreAudioVolumes(client);
		if (trackedLevel != null) {
			for (LocalHallucination hallucination : LOCAL_HALLUCINATIONS) {
				hallucination.remove(trackedLevel);
			}
		}
		trackedLevel = client.level;
		FLOATING_BLOCKS.clear();
		BREATHING_WALLS.clear();
		LOCAL_HALLUCINATIONS.clear();
		DISTORTED_ENTITIES.clear();
		SanityHallucinatedEntityManager.clear(client.level);
		skyBlinkUntilTick = 0L;
		inventoryHallucinationUntilTick = 0L;
		phantomHotbarUntilTick = 0L;
		phantomHotbarSlot = -1;
		bloodParticlesUntilTick = 0L;
		bloodParticleIntensity = 0;
		silentWorldStartTick = 0L;
		silentWorldEndTick = 0L;
		silentWorldSilenceEndTick = 0L;
		silentWorldReturnStartTick = 0L;
		silentWorldPresenceTick = 0L;
		silentWorldPresenceSpawned = false;
		silentWorldPresenceAnchor = BlockPos.ZERO;
		wrongSunEndTick = 0L;
		wrongSunStartTick = 0L;
		wrongSunBaseDayTime = 0L;
		wrongSunOffset = 0L;
		echoStepState = null;
	}

	private static void facePlayer(Entity entity, Vec3 target) {
		double dx = target.x - entity.getX();
		double dz = target.z - entity.getZ();
		float yaw = (float) (Math.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
		entity.setYRot(yaw);
		entity.setYHeadRot(yaw);
	}

	private static int nextEntityId() {
		return nextHallucinationEntityId--;
	}

	private static final class EchoStepState {
		private final long endTick;
		private final int delayTicks;
		private final ArrayDeque<StepSample> pendingSteps = new ArrayDeque<>();
		private Vec3 lastPosition;
		private float distanceSinceLastStep;

		private EchoStepState(long endTick, int delayTicks) {
			this.endTick = endTick;
			this.delayTicks = delayTicks;
		}

		private long endTick() {
			return endTick;
		}

		private void captureStep(Minecraft client, long now) {
			Vec3 current = client.player.position();
			if (lastPosition == null) {
				lastPosition = current;
				return;
			}

			double horizontal = Math.sqrt(
					(current.x - lastPosition.x) * (current.x - lastPosition.x)
							+ (current.z - lastPosition.z) * (current.z - lastPosition.z));
			lastPosition = current;
			if (!client.player.onGround()) {
				distanceSinceLastStep = 0.0F;
				return;
			}

			distanceSinceLastStep += (float) horizontal;
			if (distanceSinceLastStep < 0.78F) {
				return;
			}

			distanceSinceLastStep = 0.0F;
			BlockPos pos = BlockPos.containing(current.x, client.player.getY(), current.z);
			pendingSteps.addLast(new StepSample(pos, now + delayTicks, resolveStepSound(client.level, pos)));
		}

		private boolean playDueSteps(Minecraft client, long now) {
			while (!pendingSteps.isEmpty() && pendingSteps.peekFirst().playTick() <= now) {
				StepSample sample = pendingSteps.removeFirst();
				Vec3 source = centerOf(sample.pos());
				Vec3 toSource = source.subtract(client.player.getEyePosition()).normalize();
				if (client.player.getLookAngle().normalize().dot(toSource) > 0.35D) {
					return false;
				}
				client.level.playLocalSound(
						source.x,
						source.y,
						source.z,
						sample.sound(),
						SoundSource.BLOCKS,
						0.18F,
						0.92F + client.player.getRandom().nextFloat() * 0.06F,
						false);
			}
			return true;
		}
	}

	private record StepSample(BlockPos pos, long playTick, SoundEvent sound) {
	}

	private interface TimedIllusion {
		long endTick();
	}

	private record FloatingBlockIllusion(BlockPos origin, BlockState state, long startTick, long endTick, float orbitRadius, float phase) implements TimedIllusion {
	}

	private record PulsingWallIllusion(BlockPos pos, BlockState state, long startTick, long endTick, float phase) implements TimedIllusion {
	}

	private abstract static class LocalHallucination {
		private final Entity entity;
		private final long startTick;
		private final long endTick;

		private LocalHallucination(Entity entity, long startTick, long endTick) {
			this.entity = entity;
			this.startTick = startTick;
			this.endTick = endTick;
		}

		abstract void tick(Minecraft client, long gameTime);

		protected Entity entity() {
			return entity;
		}

		protected long startTick() {
			return startTick;
		}

		protected long endTick() {
			return endTick;
		}

		void remove(ClientLevel level) {
			if (level != null) {
				level.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED);
			}
		}
	}

	private static final class LinearHallucination extends LocalHallucination {
		private final Vec3 start;
		private final Vec3 end;

		private LinearHallucination(Entity entity, long startTick, long endTick, Vec3 start, Vec3 end) {
			super(entity, startTick, endTick);
			this.start = start;
			this.end = end;
		}

		@Override
		void tick(Minecraft client, long gameTime) {
			float progress = Mth.clamp((gameTime - startTick()) / (float) Math.max(1L, endTick() - startTick()), 0.0F, 1.0F);
			Vec3 current = start.lerp(end, progress);
			entity().setPos(current.x, current.y, current.z);
			entity().noPhysics = true;
			facePlayer(entity(), client.player.position());
		}
	}

	private static final class WatchingPlayerHallucination extends LocalHallucination {
		private WatchingPlayerHallucination(RemotePlayer entity, long startTick, long endTick) {
			super(entity, startTick, endTick);
		}

		@Override
		void tick(Minecraft client, long gameTime) {
			entity().noPhysics = true;
			facePlayer(entity(), client.player.position());
		}
	}
}
