package net.nekuzaky.sanitycraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.nekuzaky.sanitycraft.sanity.SanityConfig;
import net.nekuzaky.sanitycraft.sanity.SanityManager;

public class HorrorUiOverlays {
	private static final String[] PHANTOM_WHISPERS = {"DON'T LOOK BACK", "THE DOOR MOVED", "STAY OUT OF THE DARK", "IT STOOD IN THE WINDOW", "YOUR HOUSE IS NOT SAFE", "KEEP THE LIGHT ON"};
	private static long lastTorchScanMs = 0L;
	private static float cachedTorchRepel = 0.0F;
	private static float smoothedAmbientFogAlpha = 0.0F;
	private static long nextLootCorruptionMs = 0L;

	private HorrorUiOverlays() {
	}

	public static void renderGlobalFearOverlay(GuiGraphics guiGraphics, int sanity) {
		int width = guiGraphics.guiWidth();
		int height = guiGraphics.guiHeight();
		int fear = 100 - sanity;
		if (fear <= 0 && !SanityClientState.hasScarePulse()) {
			return;
		}
		SanityConfig config = SanityManager.getConfig();
		float visual = clamp01(config.accessibilityVisualIntensity);
		renderAmbientFog(guiGraphics, sanity, width, height);
		renderHeartbeatPressure(guiGraphics, sanity, width, height);
		renderScarePulseOverlay(guiGraphics, width, height);
		renderNearMissSilhouette(guiGraphics, width, height);
		renderVisualHallucinations(guiGraphics, sanity, width, height, visual);

		int darknessAlpha = (int) (Math.min(90, fear) * visual);
		int tintAlpha = (int) (Math.min(70, fear / 2) * visual);
		guiGraphics.fill(0, 0, width, height, (darknessAlpha << 24));
		guiGraphics.fill(0, 0, width, 18, (tintAlpha << 24) | 0x220000);
		guiGraphics.fill(0, height - 18, width, height, (tintAlpha << 24) | 0x220000);
		guiGraphics.fill(0, 0, 18, height, (tintAlpha << 24) | 0x220000);
		guiGraphics.fill(width - 18, 0, width, height, (tintAlpha << 24) | 0x220000);

		if (sanity <= 35) {
			int pulse = (int) (((Math.sin(System.currentTimeMillis() / 130.0D) + 1.0D) * 0.5D * 60.0D) * visual);
			guiGraphics.fill(0, 0, width, height, (Math.min(60, pulse) << 24) | 0x330000);
		}
		if (sanity <= 25) {
			int scanAlpha = (int) ((18 + (Math.sin(System.currentTimeMillis() / 45.0D) + 1.0D) * 0.5D * 14.0D) * visual);
			for (int y = 0; y < height; y += 4) {
				guiGraphics.fill(0, y, width, y + 1, (scanAlpha << 24) | 0x101010);
			}
			renderSilhouetteFlash(guiGraphics, width, height);
		}
		renderScriptedJumpscare(guiGraphics, width, height);
	}

	private static void renderAmbientFog(GuiGraphics guiGraphics, int sanity, int width, int height) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) {
			smoothedAmbientFogAlpha = 0.0F;
			return;
		}
		SanityConfig config = SanityManager.getConfig();
		if (!config.ambientFogEnabled) {
			smoothedAmbientFogAlpha = 0.0F;
			return;
		}

		float fearRatio = Math.max(0.0F, Math.min(1.0F, (100.0F - sanity) / 100.0F));
		float dreadStyle = config.dreadFogEnabled ? clamp01(config.dreadFogIntensity) : 0.0F;
		float weatherBoost = minecraft.level.isRaining() ? 0.12F : 0.0F;
		float stormBoost = minecraft.level.isThundering() ? 0.10F : 0.0F;
		float styleBoost = dreadStyle * 0.22F;
		float density = Math.min(1.0F, fearRatio + weatherBoost + stormBoost + styleBoost);
		int baseAlpha = clamp(config.ambientFogBaseAlpha, 0, 120);
		int maxAlpha = Math.max(baseAlpha, clamp(config.ambientFogMaxAlpha, 0, 160));
		int alpha = clamp(baseAlpha + (int) ((maxAlpha - baseAlpha) * density) + (int) (dreadStyle * 14.0F), 0, 180);

		LocalPlayer player = minecraft.player;
		if (player != null && config.cinematicCaveFogEnabled && sanity < 40 && isCaveLike(player)) {
			alpha = clamp(alpha + Math.max(0, config.cinematicCaveFogBonusAlpha), 0, 180);
		}
		if (player != null && dreadStyle > 0.0F && isForestLikeBiome(player)) {
			alpha = clamp(alpha + (int) (10.0F * dreadStyle), 0, 185);
		}
		if (config.torchRepelsFog) {
			float repel = computeTorchRepel(player, config);
			alpha = clamp((int) (alpha * (1.0F - repel)), 0, 180);
		}
		smoothedAmbientFogAlpha += (alpha - smoothedAmbientFogAlpha) * 0.08F;
		alpha = clamp((int) smoothedAmbientFogAlpha, 0, 180);
		if (alpha <= 1) {
			return;
		}

		int topColor = blendRgb(0x1A1C20, 0xAEBABE, dreadStyle);
		int bottomColor = blendRgb(0x17191D, 0x9EAAAE, dreadStyle);
		int midColor = blendRgb(0x1B1E23, 0xB8C3C7, dreadStyle);

		int topHeight = Math.max(24, (int) (height * 0.32F));
		int bottomStart = (int) (height * 0.68F);
		for (int i = 0; i < topHeight; i++) {
			float t = i / (float) Math.max(1, topHeight);
			int lineAlpha = (int) (alpha * (1.0F - t));
			guiGraphics.fill(0, i, width, i + 1, (lineAlpha << 24) | topColor);
		}
		for (int i = 0; i < height - bottomStart; i++) {
			float t = i / (float) Math.max(1, height - bottomStart);
			int lineAlpha = (int) (alpha * (1.0F - t));
			int y = height - i - 1;
			guiGraphics.fill(0, y, width, y + 1, (lineAlpha << 24) | bottomColor);
		}

		long time = System.currentTimeMillis();
		int cloudAlpha = (int) (alpha * (0.22F + dreadStyle * 0.08F));
		renderSoftFogLayer(guiGraphics, width, height, time, cloudAlpha, midColor, dreadStyle);
	}

	private static void renderHeartbeatPressure(GuiGraphics guiGraphics, int sanity, int width, int height) {
		if (sanity > 35) {
			return;
		}
		float fearRatio = Math.max(0.0F, Math.min(1.0F, (35.0F - sanity) / 35.0F));
		double pulse = (Math.sin(System.currentTimeMillis() / Math.max(90.0D, 170.0D - fearRatio * 70.0D)) + 1.0D) * 0.5D;
		int alpha = 8 + (int) (fearRatio * 28.0F) + (int) (pulse * 26.0D);
		int color = (clamp(alpha, 0, 120) << 24) | 0x280000;

		int inset = 4 + (int) ((1.0D - pulse) * 16.0D);
		guiGraphics.fill(0, 0, width, inset, color);
		guiGraphics.fill(0, height - inset, width, height, color);
		guiGraphics.fill(0, 0, inset, height, color);
		guiGraphics.fill(width - inset, 0, width, height, color);
	}

	public static void renderInventoryHorrorSkin(GuiGraphics guiGraphics, int leftPos, int topPos, int imageWidth, int imageHeight, int sanity, Font font) {
		int fear = 100 - sanity;
		if (fear <= 0) {
			return;
		}

		int bgAlpha = Math.min(120, 40 + fear);
		guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), (bgAlpha << 24) | 0x040000);

		int borderColor = ((120 + Math.min(100, fear)) << 24) | 0x5A1010;
		guiGraphics.fill(leftPos - 3, topPos - 3, leftPos + imageWidth + 3, topPos - 1, borderColor);
		guiGraphics.fill(leftPos - 3, topPos + imageHeight + 1, leftPos + imageWidth + 3, topPos + imageHeight + 3, borderColor);
		guiGraphics.fill(leftPos - 3, topPos - 1, leftPos - 1, topPos + imageHeight + 1, borderColor);
		guiGraphics.fill(leftPos + imageWidth + 1, topPos - 1, leftPos + imageWidth + 3, topPos + imageHeight + 1, borderColor);

		String state = sanity <= 20 ? "Mind: Fractured" : sanity <= 40 ? "Mind: Unstable" : sanity <= 60 ? "Mind: Disturbed" : sanity <= 80 ? "Mind: Uneasy" : "Mind: Stable";
		guiGraphics.drawString(font, state, leftPos + 8, topPos - 11, 0xFFB7A8A8, false);
		if (sanity <= 35) {
			int warningPulse = (int) ((Math.sin(System.currentTimeMillis() / 120.0D) + 1.0D) * 0.5D * 120.0D);
			int warningColor = 0xFF700000 + (warningPulse << 8);
			guiGraphics.drawString(font, "WARNING: PSYCHOSIS RISK", leftPos + 8, topPos + imageHeight + 6, warningColor, false);
		}
		triggerCorruptedLootMoment(sanity);
	}

	private static void renderSilhouetteFlash(GuiGraphics guiGraphics, int width, int height) {
		// Procedural "image" silhouette glitch without replacing vanilla textures.
		long time = System.currentTimeMillis();
		if ((time / 220L) % 7L != 0L) {
			return;
		}

		int cx = width / 2 + (int) (Math.sin(time / 80.0D) * 14.0D);
		int top = height / 2 - 60;
		int bodyAlpha = 80;
		int color = (bodyAlpha << 24);

		guiGraphics.fill(cx - 10, top, cx + 10, top + 44, color);
		guiGraphics.fill(cx - 18, top + 16, cx - 10, top + 40, color);
		guiGraphics.fill(cx + 10, top + 16, cx + 18, top + 40, color);
		guiGraphics.fill(cx - 8, top + 44, cx - 1, top + 74, color);
		guiGraphics.fill(cx + 1, top + 44, cx + 8, top + 74, color);

		// Small red eye glitch.
		guiGraphics.fill(cx - 5, top + 10, cx - 2, top + 12, 0xAA8E0000);
		guiGraphics.fill(cx + 2, top + 10, cx + 5, top + 12, 0xAA8E0000);
	}

	private static void renderScriptedJumpscare(GuiGraphics guiGraphics, int width, int height) {
		if (!SanityClientState.hasActiveJumpscare()) {
			return;
		}
		SanityConfig config = SanityManager.getConfig();
		if (config.streamerSafeMode) {
			return;
		}

		float progress = SanityClientState.getJumpscareProgress();
		int variant = SanityClientState.getJumpscareVariant() % 3;
		float visual = clamp01(config.accessibilityVisualIntensity);
		if (config.noHardFlashes) {
			visual = Math.min(visual, 0.6F);
		}
		int alpha = (int) (230.0F * (1.0F - progress * 0.75F) * visual);
		guiGraphics.fill(0, 0, width, height, (alpha << 24));

		int cx = width / 2;
		int cy = height / 2;
		int wobbleX = (int) (Math.sin(System.currentTimeMillis() / 28.0D + variant) * 12.0D);
		int wobbleY = (int) (Math.cos(System.currentTimeMillis() / 36.0D + variant) * 8.0D);
		int headSize = 44 + (variant * 6);
		int torsoH = 98 + (variant * 10);

		int bodyColor = ((180 - (int) (progress * 60.0F)) << 24);
		int eyeColor = ((140 + (int) (Math.sin(System.currentTimeMillis() / 40.0D) * 40.0D)) << 24) | 0x00A00000;

		int x = cx + wobbleX;
		int y = cy - 90 + wobbleY;

		// Head
		guiGraphics.fill(x - headSize / 2, y - headSize / 2, x + headSize / 2, y + headSize / 2, bodyColor);
		// Torso
		guiGraphics.fill(x - 26, y + 16, x + 26, y + 16 + torsoH, bodyColor);
		// Arms
		guiGraphics.fill(x - 50, y + 26, x - 26, y + 26 + torsoH - 12, bodyColor);
		guiGraphics.fill(x + 26, y + 26, x + 50, y + 26 + torsoH - 12, bodyColor);
		// Legs
		guiGraphics.fill(x - 22, y + 16 + torsoH, x - 4, y + 16 + torsoH + 56, bodyColor);
		guiGraphics.fill(x + 4, y + 16 + torsoH, x + 22, y + 16 + torsoH + 56, bodyColor);

		guiGraphics.fill(x - 13, y - 4, x - 4, y + 3, eyeColor);
		guiGraphics.fill(x + 4, y - 4, x + 13, y + 3, eyeColor);

		// Hard cut flash at the start.
		if (progress < 0.12F) {
			if (config.noHardFlashes) {
				return;
			}
			int flash = (int) (255.0F * (1.0F - progress / 0.12F) * visual);
			guiGraphics.fill(0, 0, width, height, (flash << 24) | 0x00FFFFFF);
		}
	}

	private static void renderVisualHallucinations(GuiGraphics guiGraphics, int sanity, int width, int height, float visual) {
		if (sanity > 55 || visual <= 0.0F) {
			return;
		}
		renderPeripheralShadows(guiGraphics, sanity, width, height, visual);
		if (sanity <= 34) {
			renderFrameCorruption(guiGraphics, width, height, visual);
		}
		renderPhantomMessage(guiGraphics, width, height, visual);
		renderHallucinationBlink(guiGraphics, width, height, visual);
		renderEdgeWatcher(guiGraphics, width, height, visual);
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static void renderScarePulseOverlay(GuiGraphics guiGraphics, int width, int height) {
		float factor = SanityClientState.getScarePulseFactor();
		if (factor <= 0.0F) {
			return;
		}
		int flashAlpha = clamp((int) (24.0F + factor * 90.0F), 0, 150);
		guiGraphics.fill(0, 0, width, height, (flashAlpha << 24) | 0x180000);
		int edge = 10 + (int) (factor * 22.0F);
		int edgeAlpha = clamp((int) (20.0F + factor * 70.0F), 0, 120);
		int edgeColor = (edgeAlpha << 24) | 0x260000;
		guiGraphics.fill(0, 0, width, edge, edgeColor);
		guiGraphics.fill(0, height - edge, width, height, edgeColor);
		guiGraphics.fill(0, 0, edge, height, edgeColor);
		guiGraphics.fill(width - edge, 0, width, height, edgeColor);
	}

	private static void renderNearMissSilhouette(GuiGraphics guiGraphics, int width, int height) {
		if (!SanityClientState.hasNearMiss()) {
			return;
		}
		float progress = SanityClientState.getNearMissProgress();
		float fade = Math.max(0.0F, 1.0F - progress);
		int alpha = clamp((int) (fade * 120.0F), 0, 140);
		if (alpha <= 0) {
			return;
		}

		int side = SanityClientState.getNearMissSide();
		int margin = 12;
		int bodyW = 18;
		int bodyH = 74;
		int y = (int) (height * 0.36F);
		int x = side < 0 ? margin : width - margin - bodyW;
		int color = (alpha << 24);

		guiGraphics.fill(x, y, x + bodyW, y + 40, color);
		guiGraphics.fill(x - 5, y + 12, x, y + 38, color);
		guiGraphics.fill(x + bodyW, y + 12, x + bodyW + 5, y + 38, color);
		guiGraphics.fill(x + 3, y + 40, x + 8, y + bodyH, color);
		guiGraphics.fill(x + bodyW - 8, y + 40, x + bodyW - 3, y + bodyH, color);

		int eye = (clamp((int) (alpha * 0.65F), 0, 110) << 24) | 0x00800000;
		guiGraphics.fill(x + 4, y + 10, x + 7, y + 12, eye);
		guiGraphics.fill(x + bodyW - 7, y + 10, x + bodyW - 4, y + 12, eye);
	}

	private static void renderPeripheralShadows(GuiGraphics guiGraphics, int sanity, int width, int height, float visual) {
		long time = System.currentTimeMillis();
		float pressure = clamp01((55.0F - sanity) / 55.0F);
		int bandCount = 3 + (sanity <= 25 ? 2 : 0);
		for (int i = 0; i < bandCount; i++) {
			double wave = Math.sin(time / (640.0D + i * 120.0D) + i * 1.6D);
			int bandWidth = 12 + (int) (pressure * 24.0F) + i * 4;
			int bandAlpha = clamp((int) ((18.0F + pressure * 48.0F + i * 6.0F) * visual), 0, 96);
			int yOffset = (int) (height * (0.10F + i * 0.18F) + wave * (12.0D + i * 4.0D));
			guiGraphics.fill(0, Math.max(0, yOffset), bandWidth, Math.min(height, yOffset + 56 + i * 14), (bandAlpha << 24));
			guiGraphics.fill(width - bandWidth, Math.max(0, yOffset + 18), width, Math.min(height, yOffset + 82 + i * 12), (bandAlpha << 24));
		}
	}

	private static void renderFrameCorruption(GuiGraphics guiGraphics, int width, int height, float visual) {
		long phase = System.currentTimeMillis() / 140L;
		for (int i = 0; i < 8; i++) {
			int seed = mix((int) phase + i * 37);
			int blockWidth = 18 + Math.abs(seed % 52);
			int blockHeight = 6 + Math.abs((seed >> 4) % 28);
			int x = Math.abs((seed >> 8) % Math.max(1, width - blockWidth));
			int y = Math.abs((seed >> 15) % Math.max(1, height - blockHeight));
			int alpha = clamp((int) ((18 + Math.abs((seed >> 22) % 60)) * visual), 0, 120);
			int color = (alpha << 24) | (((seed >> 3) & 1) == 0 ? 0x100000 : 0x081010);
			guiGraphics.fill(x, y, x + blockWidth, y + blockHeight, color);
		}
	}

	private static void renderPhantomMessage(GuiGraphics guiGraphics, int width, int height, float visual) {
		if (!SanityClientState.hasPhantomMessage()) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		float progress = SanityClientState.getPhantomMessageProgress();
		float fade = progress < 0.18F ? progress / 0.18F : progress > 0.82F ? (1.0F - progress) / 0.18F : 1.0F;
		int alpha = clamp((int) (fade * 170.0F * visual), 0, 190);
		if (alpha <= 0) {
			return;
		}
		String text = PHANTOM_WHISPERS[Math.floorMod(SanityClientState.getPhantomMessageIndex(), PHANTOM_WHISPERS.length)];
		int x = clamp(SanityClientState.getPhantomMessageX(), 12, Math.max(12, width - font.width(text) - 12));
		int y = clamp(SanityClientState.getPhantomMessageY(), 14, Math.max(14, height - 16));
		int red = ((alpha / 2) << 24) | 0x00700000;
		int white = (alpha << 24) | 0x00E7D8D8;
		int offset = (int) (Math.sin(System.currentTimeMillis() / 42.0D) * 2.0D);
		guiGraphics.drawString(font, text, x + offset + 1, y, red, false);
		guiGraphics.drawString(font, text, x, y, white, false);
	}

	private static void renderHallucinationBlink(GuiGraphics guiGraphics, int width, int height, float visual) {
		if (!SanityClientState.hasHallucinationBlink()) {
			return;
		}
		float progress = SanityClientState.getHallucinationBlinkProgress();
		float eyelid = progress < 0.5F ? progress / 0.5F : (1.0F - progress) / 0.5F;
		int cover = (int) (height * (0.18F + eyelid * 0.28F) * visual);
		int alpha = clamp((int) (70.0F + eyelid * 140.0F), 0, 220);
		guiGraphics.fill(0, 0, width, cover, (alpha << 24));
		guiGraphics.fill(0, height - cover, width, height, (alpha << 24));
	}

	private static void renderEdgeWatcher(GuiGraphics guiGraphics, int width, int height, float visual) {
		if (!SanityClientState.hasEdgeWatcher()) {
			return;
		}
		float progress = SanityClientState.getEdgeWatcherProgress();
		float fade = Math.max(0.0F, 1.0F - progress);
		int alpha = clamp((int) (fade * 150.0F * visual), 0, 160);
		if (alpha <= 0) {
			return;
		}
		int side = SanityClientState.getEdgeWatcherSide();
		int bodyW = 22;
		int bodyH = 96;
		int x = side < 0 ? 8 : width - bodyW - 8;
		int y = clamp(SanityClientState.getEdgeWatcherY(), 24, Math.max(24, height - bodyH - 16));
		int color = (alpha << 24);
		guiGraphics.fill(x, y, x + bodyW, y + 52, color);
		guiGraphics.fill(x - 7, y + 16, x, y + 46, color);
		guiGraphics.fill(x + bodyW, y + 12, x + bodyW + 6, y + 42, color);
		guiGraphics.fill(x + 4, y + 52, x + 10, y + bodyH, color);
		guiGraphics.fill(x + bodyW - 10, y + 52, x + bodyW - 4, y + bodyH, color);
		int eye = (clamp((int) (alpha * 0.65F), 0, 110) << 24) | 0x00950000;
		guiGraphics.fill(x + 5, y + 12, x + 9, y + 14, eye);
		guiGraphics.fill(x + bodyW - 9, y + 12, x + bodyW - 5, y + 14, eye);
	}

	private static boolean isCaveLike(LocalPlayer player) {
		BlockPos pos = player.blockPosition();
		return !player.level().canSeeSky(pos) && pos.getY() < player.level().getSeaLevel() + 2;
	}

	private static boolean isForestLikeBiome(LocalPlayer player) {
		return player.level().getBiome(player.blockPosition()).unwrapKey().map(key -> {
			String path = key.location().getPath();
			return path.contains("forest") || path.contains("taiga") || path.contains("swamp") || path.contains("dark_forest");
		}).orElse(false);
	}

	private static float computeTorchRepel(LocalPlayer player, SanityConfig config) {
		if (player == null || player.level() == null) {
			return 0.0F;
		}
		long now = System.currentTimeMillis();
		if (now - lastTorchScanMs < 250L) {
			return cachedTorchRepel;
		}
		lastTorchScanMs = now;

		float repel = 0.0F;
		if (isTorchInHand(player.getMainHandItem()) || isTorchInHand(player.getOffhandItem())) {
			repel += clamp01(config.heldTorchFogRepel);
		}

		BlockPos center = player.blockPosition();
		if (isNearTorchBlock(player, center, 5) || player.level().getBrightness(LightLayer.BLOCK, center) >= 10) {
			repel += clamp01(config.nearbyTorchFogRepel);
		}
		cachedTorchRepel = clamp01(repel);
		return cachedTorchRepel;
	}

	private static boolean isTorchInHand(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		return stack.is(Items.TORCH) || stack.is(Items.SOUL_TORCH) || stack.is(Items.REDSTONE_TORCH);
	}

	private static boolean isNearTorchBlock(LocalPlayer player, BlockPos center, int radius) {
		int safeRadius = Math.max(1, radius);
		for (int x = -safeRadius; x <= safeRadius; x++) {
			for (int y = -2; y <= 2; y++) {
				for (int z = -safeRadius; z <= safeRadius; z++) {
					BlockPos check = center.offset(x, y, z);
					BlockState state = player.level().getBlockState(check);
					if (state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH) || state.is(Blocks.SOUL_TORCH) || state.is(Blocks.SOUL_WALL_TORCH) || state.is(Blocks.REDSTONE_TORCH)
							|| state.is(Blocks.REDSTONE_WALL_TORCH)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static float clamp01(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}

	private static void renderSoftFogLayer(GuiGraphics guiGraphics, int width, int height, long time, int alpha, int color, float style) {
		if (alpha <= 0) {
			return;
		}
		int layers = style > 0.45F ? 4 : 3;
		for (int i = 0; i < layers; i++) {
			float wave = (float) Math.sin((time / (1400.0D + i * 260.0D)) + i * 1.37D);
			int centerY = (int) (height * (0.24F + i * 0.18F) + wave * (8.0F + i * 3.0F));
			int bandHeight = (int) (height * (0.10F + i * 0.015F));
			int layerAlpha = Math.max(1, (int) (alpha * (0.70F - i * 0.14F)));
			renderSoftHorizontalBand(guiGraphics, width, height, centerY, bandHeight, layerAlpha, color);
		}
	}

	private static void renderSoftHorizontalBand(GuiGraphics guiGraphics, int width, int height, int centerY, int bandHeight, int alpha, int color) {
		int half = Math.max(4, bandHeight / 2);
		int start = Math.max(0, centerY - half);
		int end = Math.min(height, centerY + half);
		for (int y = start; y < end; y++) {
			float distance = Math.abs((y - centerY) / (float) half);
			float falloff = 1.0F - distance * distance;
			if (falloff <= 0.0F) {
				continue;
			}
			int lineAlpha = Math.max(1, (int) (alpha * falloff));
			guiGraphics.fill(0, y, width, y + 1, (lineAlpha << 24) | color);
		}
	}

	private static int blendRgb(int from, int to, float t) {
		float clamped = clamp01(t);
		int fr = (from >> 16) & 0xFF;
		int fg = (from >> 8) & 0xFF;
		int fb = from & 0xFF;
		int tr = (to >> 16) & 0xFF;
		int tg = (to >> 8) & 0xFF;
		int tb = to & 0xFF;
		int r = (int) (fr + (tr - fr) * clamped);
		int g = (int) (fg + (tg - fg) * clamped);
		int b = (int) (fb + (tb - fb) * clamped);
		return (r << 16) | (g << 8) | b;
	}

	private static int mix(int value) {
		int seed = value;
		seed ^= seed << 13;
		seed ^= seed >>> 17;
		seed ^= seed << 5;
		return seed;
	}

	private static void triggerCorruptedLootMoment(int sanity) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.level == null) {
			return;
		}
		SanityConfig config = SanityManager.getConfig();
		if (!config.corruptedLootMomentsEnabled || sanity > 35) {
			return;
		}
		long now = System.currentTimeMillis();
		if (now < nextLootCorruptionMs) {
			return;
		}

		if (minecraft.player.getRandom().nextFloat() < clamp01(config.corruptedLootMomentChance) * 0.12F) {
			minecraft.player.displayClientMessage(net.minecraft.network.chat.Component.literal("Loot cache integrity failure..."), true);
			float volume = 0.55F * clamp01(config.accessibilityAudioIntensity);
			minecraft.player.playNotifySound(SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, Math.max(0.01F, volume), 0.65F + minecraft.player.getRandom().nextFloat() * 0.2F);
			minecraft.level.playLocalSound(minecraft.player.getX(), minecraft.player.getY(), minecraft.player.getZ(), SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE, Math.max(0.01F, volume * 0.8F),
					0.7F + minecraft.player.getRandom().nextFloat() * 0.2F, false);
			SanityClientState.triggerScarePulse(8, 3);
		}
		nextLootCorruptionMs = now + 1200L + minecraft.player.getRandom().nextInt(2200);
	}
}
