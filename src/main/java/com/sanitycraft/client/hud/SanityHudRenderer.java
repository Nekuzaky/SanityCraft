package com.sanitycraft.client.hud;

import com.sanitycraft.client.effects.SanityVisualProfile;
import com.sanitycraft.sanity.SanityThresholds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;

@Environment(EnvType.CLIENT)
public final class SanityHudRenderer {
	private SanityHudRenderer() {
	}

	public static void register() {
		HudRenderCallback.EVENT.register(SanityHudRenderer::render);
	}

	private static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.options.hideGui) {
			return;
		}

		int sanity = ClientSanityState.getSanity();
		SanityThresholds.Stage stage = ClientSanityState.getStage();
		renderOverlays(guiGraphics, minecraft, sanity, stage);
		renderCollapseHudInstability(guiGraphics, minecraft);
		renderSanityPanel(guiGraphics, minecraft.font, sanity, stage);
	}

	private static void renderSanityPanel(GuiGraphics guiGraphics, Font font, int sanity, SanityThresholds.Stage stage) {
		int x = 10;
		int y = 10;
		int panelWidth = 132;
		int barWidth = 116;
		int barHeight = 8;
		int jitter = stage == SanityThresholds.Stage.COLLAPSE ? (int) (Math.sin(System.currentTimeMillis() / 55.0D) * 2.0D) : 0;

		guiGraphics.fill(x - 4 + jitter, y - 4 + jitter, x + panelWidth + 4 + jitter, y + 36 + jitter, 0x6A040101);
		guiGraphics.fill(x - 3 + jitter, y - 3 + jitter, x + panelWidth + 3 + jitter, y + 35 + jitter, 0x8C210909);
		guiGraphics.fill(x - 2 + jitter, y - 2 + jitter, x + panelWidth + 2 + jitter, y + 34 + jitter, 0xD10C0909);

		guiGraphics.drawString(font, "SANITY", x + jitter, y + jitter, 0xFFE7D9D2, false);
		guiGraphics.drawString(font, sanity + "%", x + panelWidth - 28 + jitter, y + jitter, 0xFFD0B6A8, false);
		guiGraphics.drawString(font, stageLabel(stage), x + jitter, y + 24 + jitter, stageColor(stage), false);

		int barX = x;
		int barY = y + 12;
		int fill = Math.max(0, Math.min(barWidth, Math.round((sanity / 100.0F) * barWidth)));
		guiGraphics.fill(barX - 1 + jitter, barY - 1 + jitter, barX + barWidth + 1 + jitter, barY + barHeight + 1 + jitter, 0xFF3C1D1D);
		guiGraphics.fill(barX + jitter, barY + jitter, barX + barWidth + jitter, barY + barHeight + jitter, 0xFF140B0B);
		if (fill > 0) {
			for (int i = 0; i < barHeight; i++) {
				float depth = 0.74F + (1.0F - i / (float) barHeight) * 0.24F;
				int color = shade(stageColor(stage), depth);
				guiGraphics.fill(barX + jitter, barY + i + jitter, barX + fill + jitter, barY + i + 1 + jitter, color);
			}
			int shineX = barX + ((int) (System.currentTimeMillis() / 14L) % Math.max(2, fill + 8)) - 6 + jitter;
			guiGraphics.fill(shineX, barY + jitter, shineX + 2, barY + barHeight + jitter, 0x44FFFFFF);
		}
	}

	private static void renderOverlays(GuiGraphics guiGraphics, Minecraft minecraft, int sanity, SanityThresholds.Stage stage) {
		int width = guiGraphics.guiWidth();
		int height = guiGraphics.guiHeight();
		long time = System.currentTimeMillis();
		float distortion = ClientSanityState.getDistortionFactor();
		float fogDensity = SanityVisualProfile.getFogDensity(sanity, distortion);
		float vignetteStrength = SanityVisualProfile.getVignetteStrength(sanity, distortion);
		float grainStrength = SanityVisualProfile.getGrainStrength(sanity, distortion);
		float chromaticStrength = SanityVisualProfile.getChromaticStrength(sanity, distortion);
		float instability = SanityVisualProfile.getInstabilityStrength(sanity, distortion);
		float ambientDarkness = SanityVisualProfile.getAmbientDarkness(sanity, distortion);
		int blockLight = minecraft.level == null ? 0 : minecraft.level.getBrightness(LightLayer.BLOCK, minecraft.player.blockPosition());
		boolean holdingTorch = isTorch(minecraft.player.getMainHandItem()) || isTorch(minecraft.player.getOffhandItem());
		float torchRelief = SanityVisualProfile.getTorchRelief(blockLight, holdingTorch);

		if (ambientDarkness > 0.0F) {
			int darknessAlpha = clamp((int) ((ambientDarkness * (0.85F - torchRelief * 0.18F)) * 180.0F), 0, 185);
			guiGraphics.fill(0, 0, width, height, (darknessAlpha << 24));
		}
		if (fogDensity > 0.0F) {
			renderFog(guiGraphics, width, height, time, fogDensity, distortion);
		}
		if (torchRelief > 0.0F) {
			renderTorchFlicker(guiGraphics, width, height, time, torchRelief, ambientDarkness, instability);
		}
		if (vignetteStrength > 0.0F) {
			renderVignette(guiGraphics, width, height, vignetteStrength);
		}
		if (instability > 0.0F) {
			renderPulseStrain(guiGraphics, width, height, time, instability, distortion);
		}
		if (chromaticStrength > 0.0F) {
			renderChromaticAberration(guiGraphics, width, height, time, chromaticStrength);
		}
		if (grainStrength > 0.0F) {
			renderFilmGrain(guiGraphics, width, height, time, grainStrength);
		}
		if (ClientSanityState.hasShadowFlicker()) {
			renderShadowFlicker(guiGraphics, width, height);
		}
		if (ClientSanityState.hasHallucinationBlink()) {
			renderBlink(guiGraphics, width, height);
		}
		if (ClientSanityState.hasEdgeWatcher()) {
			renderEdgeWatcher(guiGraphics, width, height);
		}
		if (ClientSanityState.hasStructureEcho()) {
			renderStructureEcho(guiGraphics);
		}
		if (ClientSanityState.hasPhantomText()) {
			renderPhantomText(guiGraphics, minecraft.font);
		}
		if (stage == SanityThresholds.Stage.COLLAPSE) {
			renderCollapseNoise(guiGraphics, width, height, time, instability + distortion * 0.35F);
		}
	}

	private static void renderFog(GuiGraphics guiGraphics, int width, int height, long time, float fogDensity, float distortion) {
		int layers = 2 + (int) (fogDensity * 4.0F);
		for (int i = 0; i < layers; i++) {
			double wave = Math.sin(time / (1500.0D + i * 230.0D) + i * 1.18D);
			int centerY = (int) (height * (0.16F + i * 0.13F) + wave * (8.0D + fogDensity * 16.0D + i * 4.0D));
			int bandHeight = (int) (height * (0.06F + fogDensity * 0.08F + i * 0.012F));
			int alpha = clamp((int) ((18.0F + i * 6.0F + fogDensity * 48.0F + distortion * 14.0F)), 0, 92);
			renderBand(guiGraphics, width, height, centerY, bandHeight, alpha, 0x111417);
		}
	}

	private static void renderBand(GuiGraphics guiGraphics, int width, int height, int centerY, int bandHeight, int alpha, int rgb) {
		int half = Math.max(5, bandHeight / 2);
		for (int y = Math.max(0, centerY - half); y < Math.min(height, centerY + half); y++) {
			float distance = Math.abs((y - centerY) / (float) half);
			float falloff = 1.0F - distance * distance;
			if (falloff <= 0.0F) {
				continue;
			}
			int lineAlpha = clamp((int) (alpha * falloff), 0, 90);
			guiGraphics.fill(0, y, width, y + 1, (lineAlpha << 24) | rgb);
		}
	}

	private static void renderVignette(GuiGraphics guiGraphics, int width, int height, float strength) {
		int bands = 3 + (strength > 0.55F ? 1 : 0);
		for (int i = 0; i < bands; i++) {
			int inset = i * 6;
			int edge = 12 + i * 5 + (int) (strength * 14.0F);
			int alpha = clamp((int) ((20.0F + i * 10.0F) * strength), 0, 120);
			int color = (alpha << 24) | 0x180000;
			guiGraphics.fill(inset, inset, width - inset, inset + edge, color);
			guiGraphics.fill(inset, height - inset - edge, width - inset, height - inset, color);
			guiGraphics.fill(inset, inset, inset + edge, height - inset, color);
			guiGraphics.fill(width - inset - edge, inset, width - inset, height - inset, color);
		}
	}

	private static void renderPulseStrain(GuiGraphics guiGraphics, int width, int height, long time, float instability, float distortion) {
		int pulse = clamp((int) ((Math.sin(time / 130.0D) + 1.0D) * (6.0D + instability * 24.0D + distortion * 8.0D)), 0, 74);
		guiGraphics.fill(0, 0, width, height, (pulse << 24) | 0x140000);
		if (instability > 0.14F) {
			for (int y = 0; y < height; y += 4) {
				guiGraphics.fill(0, y, width, y + 1, 0x11000000);
			}
		}
	}

	private static void renderChromaticAberration(GuiGraphics guiGraphics, int width, int height, long time, float strength) {
		int shift = 1 + (int) (strength * 3.0F);
		int edgeAlpha = clamp((int) (18.0F + strength * 34.0F), 0, 64);
		int red = (edgeAlpha << 24) | 0x330000;
		int cyan = (edgeAlpha << 24) | 0x001922;
		guiGraphics.fill(0, 0, shift, height, red);
		guiGraphics.fill(width - shift, 0, width, height, cyan);
		guiGraphics.fill(0, 0, width, shift, red);
		guiGraphics.fill(0, height - shift, width, height, cyan);

		int splits = 2 + (int) (strength * 4.0F);
		for (int i = 0; i < splits; i++) {
			int seed = mix((int) (time / 90L) + i * 41);
			int y = Math.abs(seed % Math.max(1, height - 3));
			int thickness = 1 + Math.abs((seed >> 4) % 3);
			int spanStart = Math.abs((seed >> 8) % Math.max(1, width / 3));
			int spanWidth = width - spanStart * 2;
			if (spanWidth <= 0) {
				continue;
			}
			guiGraphics.fill(spanStart, y, spanStart + spanWidth, y + thickness, ((edgeAlpha / 2) << 24) | 0x2A0000);
			guiGraphics.fill(Math.max(0, spanStart + shift), y, Math.min(width, spanStart + spanWidth + shift), y + thickness, ((edgeAlpha / 2) << 24) | 0x001F28);
		}
	}

	private static void renderFilmGrain(GuiGraphics guiGraphics, int width, int height, long time, float strength) {
		int particles = 16 + (int) (strength * 42.0F);
		int alphaFloor = clamp((int) (8.0F + strength * 16.0F), 4, 30);
		for (int i = 0; i < particles; i++) {
			int seed = mix((int) (time / 48L) + i * 67);
			int size = 1 + Math.abs((seed >> 3) % 2);
			int x = Math.abs((seed >> 7) % Math.max(1, width - size));
			int y = Math.abs((seed >> 13) % Math.max(1, height - size));
			int alpha = alphaFloor + Math.abs((seed >> 17) % Math.max(2, 8 + (int) (strength * 16.0F)));
			int rgb = ((seed >> 6) & 1) == 0 ? 0x101010 : 0xD8D6D2;
			guiGraphics.fill(x, y, x + size, y + size, (clamp(alpha, 0, 42) << 24) | rgb);
		}
		if (strength > 0.18F) {
			int lineAlpha = clamp((int) (6.0F + strength * 10.0F), 0, 22);
			for (int y = 0; y < height; y += 5) {
				guiGraphics.fill(0, y, width, y + 1, (lineAlpha << 24) | 0x0A0A0A);
			}
		}
	}

	private static void renderTorchFlicker(GuiGraphics guiGraphics, int width, int height, long time, float torchRelief, float ambientDarkness, float instability) {
		float flicker = 0.82F
				+ (float) Math.sin(time / 78.0D) * 0.08F
				+ (float) Math.sin(time / 37.0D + 1.2D) * 0.05F
				- instability * 0.10F;
		if (ClientSanityState.hasShadowFlicker()) {
			flicker -= (1.0F - ClientSanityState.getShadowFlickerProgress()) * 0.28F;
		}
		float intensity = Math.max(0.0F, torchRelief * (0.12F + (1.0F - ambientDarkness) * 0.12F) * flicker);
		if (intensity <= 0.01F) {
			return;
		}

		int centerX = width / 2;
		int centerY = height / 2 + 18;
		int rings = 4;
		for (int i = 0; i < rings; i++) {
			float falloff = 1.0F - i / (float) rings;
			int radiusX = (int) (width * (0.09F + falloff * 0.11F));
			int radiusY = (int) (height * (0.07F + falloff * 0.09F));
			int alpha = clamp((int) (intensity * falloff * 34.0F), 0, 34);
			int color = (alpha << 24) | (i == 0 ? 0x5C2A06 : 0x2A1405);
			guiGraphics.fill(centerX - radiusX, centerY - radiusY, centerX + radiusX, centerY + radiusY, color);
		}
	}

	private static void renderBlink(GuiGraphics guiGraphics, int width, int height) {
		float progress = ClientSanityState.getHallucinationBlinkProgress();
		float eyelid = progress < 0.5F ? progress / 0.5F : (1.0F - progress) / 0.5F;
		int cover = (int) (height * (0.18F + eyelid * 0.26F));
		int alpha = clamp((int) (82.0F + eyelid * 160.0F), 0, 220);
		guiGraphics.fill(0, 0, width, cover, (alpha << 24));
		guiGraphics.fill(0, height - cover, width, height, (alpha << 24));
	}

	private static void renderEdgeWatcher(GuiGraphics guiGraphics, int width, int height) {
		float fade = Math.max(0.0F, 1.0F - ClientSanityState.getEdgeWatcherProgress());
		int alpha = clamp((int) ((1.0F - fade) * 160.0F), 0, 160);
		if (alpha <= 0) {
			return;
		}
		int side = ClientSanityState.getEdgeWatcherSide();
		int bodyW = 24;
		int bodyH = 98;
		int x = side < 0 ? 6 : width - bodyW - 6;
		int y = ClientSanityState.getEdgeWatcherY();
		int color = alpha << 24;
		guiGraphics.fill(x, y, x + bodyW, y + 54, color);
		guiGraphics.fill(x - 6, y + 14, x, y + 46, color);
		guiGraphics.fill(x + bodyW, y + 12, x + bodyW + 6, y + 42, color);
		guiGraphics.fill(x + 5, y + 54, x + 10, y + bodyH, color);
		guiGraphics.fill(x + bodyW - 10, y + 54, x + bodyW - 5, y + bodyH, color);
		int eye = (clamp((int) (alpha * 0.72F), 0, 120) << 24) | 0x00960000;
		guiGraphics.fill(x + 5, y + 12, x + 9, y + 14, eye);
		guiGraphics.fill(x + bodyW - 9, y + 12, x + bodyW - 5, y + 14, eye);
	}

	private static void renderStructureEcho(GuiGraphics guiGraphics) {
		float progress = ClientSanityState.getStructureEchoProgress();
		float fade = Math.max(0.0F, 1.0F - progress);
		int alpha = clamp((int) ((1.0F - fade) * 140.0F), 0, 150);
		if (alpha <= 0) {
			return;
		}
		int x = ClientSanityState.getStructureEchoX();
		int y = ClientSanityState.getStructureEchoY();
		int width = ClientSanityState.getStructureEchoWidth();
		int height = ClientSanityState.getStructureEchoHeight();
		int color = (alpha << 24) | 0x130000;
		guiGraphics.renderOutline(x, y, width, height, color);
		guiGraphics.fill(x + width / 2 - 1, y, x + width / 2 + 1, y + height, color);
		guiGraphics.fill(x, y + height / 2 - 1, x + width, y + height / 2 + 1, color);
		if (ClientSanityState.getStructureEchoVariant() > 0) {
			guiGraphics.renderOutline(x + 8, y + 12, Math.max(12, width - 16), Math.max(16, height - 24), (alpha / 2 << 24) | 0x380808);
		}
	}

	private static void renderPhantomText(GuiGraphics guiGraphics, Font font) {
		float progress = ClientSanityState.getPhantomTextProgress();
		float fade = progress < 0.2F ? progress / 0.2F : progress > 0.82F ? (1.0F - progress) / 0.18F : 1.0F;
		int alpha = clamp((int) (fade * 170.0F), 0, 190);
		if (alpha <= 0) {
			return;
		}
		String text = ClientSanityState.getPhantomText();
		int x = ClientSanityState.getPhantomTextX();
		int y = ClientSanityState.getPhantomTextY();
		int red = ((alpha / 2) << 24) | 0x00700000;
		int white = (alpha << 24) | 0x00E7D8D8;
		int offset = (int) (Math.sin(System.currentTimeMillis() / 42.0D) * 2.0D);
		guiGraphics.drawString(font, text, x + offset + 1, y, red, false);
		guiGraphics.drawString(font, text, x, y, white, false);
	}

	private static void renderCollapseNoise(GuiGraphics guiGraphics, int width, int height, long time, float distortion) {
		int pulseAlpha = clamp((int) (24.0F + distortion * 54.0F), 0, 110);
		for (int i = 0; i < 8; i++) {
			int seed = mix((int) (time / 120L) + i * 31);
			int blockWidth = 18 + Math.abs(seed % 58);
			int blockHeight = 4 + Math.abs((seed >> 4) % 22);
			int x = Math.abs((seed >> 7) % Math.max(1, width - blockWidth));
			int y = Math.abs((seed >> 13) % Math.max(1, height - blockHeight));
			guiGraphics.fill(x, y, x + blockWidth, y + blockHeight, (pulseAlpha << 24) | (((seed >> 3) & 1) == 0 ? 0x140000 : 0x101010));
		}
	}

	private static void renderCollapseHudInstability(GuiGraphics guiGraphics, Minecraft minecraft) {
		float instability = ClientSanityState.getHudInstability();
		if (instability <= 0.0F || minecraft.player == null) {
			return;
		}

		int width = guiGraphics.guiWidth();
		int height = guiGraphics.guiHeight();
		int centerX = width / 2;
		long time = System.currentTimeMillis();
		int seed = mix((int) (time / 45L));
		float flashStrength = ClientSanityState.getFakeDamageFlashStrength();
		int jitterX = ((seed & 1) == 0 ? -1 : 1) * (instability > 0.42F ? 2 : 1);
		int jitterY = ((seed >> 2) & 1) == 0 ? 0 : 1;
		if (flashStrength > 0.0F) {
			jitterX += ((seed >> 3) & 1) == 0 ? -1 : 1;
			jitterY += 1;
		}

		renderHealthInstability(guiGraphics, minecraft, centerX, height, instability, flashStrength, jitterX, jitterY, seed);
		renderFoodInstability(guiGraphics, minecraft, centerX, height, instability, flashStrength, jitterX, jitterY, seed);
		renderExperienceInstability(guiGraphics, minecraft, centerX, height, instability, flashStrength, jitterX, jitterY);

		if (flashStrength > 0.0F) {
			int alpha = clamp((int) (24.0F + flashStrength * 54.0F), 0, 96);
			guiGraphics.fill(0, 0, width, height, (alpha << 24) | 0x4A0000);
		}
	}

	private static void renderHealthInstability(
			GuiGraphics guiGraphics,
			Minecraft minecraft,
			int centerX,
			int height,
			float instability,
			float flashStrength,
			int jitterX,
			int jitterY,
			int seed) {
		int heartSlots = Math.max(10, Mth.ceil(minecraft.player.getMaxHealth() / 2.0F));
		int filledUnits = Mth.ceil(minecraft.player.getHealth());
		for (int i = 0; i < heartSlots; i++) {
			int row = i / 10;
			int column = i % 10;
			int x = centerX - 91 + column * 8 + jitterX + ((((seed >> (i % 5)) & 1) == 0) ? 0 : 1);
			int y = height - 39 - row * 10 + jitterY;
			boolean filled = filledUnits > i * 2;
			boolean half = filledUnits == i * 2 + 1;
			int alpha = clamp((int) ((filled ? 20.0F : 8.0F) + instability * 28.0F + flashStrength * 34.0F), 0, 92);
			int color = (alpha << 24) | (filled ? 0x62121A : 0x150708);
			int glow = ((alpha / 2) << 24) | 0xC72E3B;
			guiGraphics.fill(x + 2, y, x + 6, y + 8, color);
			guiGraphics.fill(x, y + 2, x + 8, y + 6, color);
			if (half) {
				guiGraphics.fill(x + 4, y, x + 6, y + 8, glow);
			} else if (filled) {
				guiGraphics.fill(x + 2, y + 1, x + 6, y + 3, glow);
			}
		}
	}

	private static void renderFoodInstability(
			GuiGraphics guiGraphics,
			Minecraft minecraft,
			int centerX,
			int height,
			float instability,
			float flashStrength,
			int jitterX,
			int jitterY,
			int seed) {
		int foodLevel = minecraft.player.getFoodData().getFoodLevel();
		for (int i = 0; i < 10; i++) {
			int x = centerX + 91 - (i + 1) * 8 + jitterX - ((((seed >> ((i + 1) % 5)) & 1) == 0) ? 0 : 1);
			int y = height - 39 + jitterY;
			boolean filled = foodLevel > i * 2;
			boolean half = foodLevel == i * 2 + 1;
			int alpha = clamp((int) ((filled ? 18.0F : 7.0F) + instability * 24.0F + flashStrength * 30.0F), 0, 84);
			int color = (alpha << 24) | (filled ? 0x4E2C09 : 0x150D05);
			int glow = ((alpha / 2) << 24) | 0xD8962A;
			guiGraphics.fill(x + 1, y + 1, x + 7, y + 7, color);
			guiGraphics.fill(x + 2, y, x + 6, y + 8, color);
			if (half) {
				guiGraphics.fill(x + 4, y + 1, x + 7, y + 7, glow);
			} else if (filled) {
				guiGraphics.fill(x + 2, y + 1, x + 6, y + 2, glow);
			}
		}
	}

	private static void renderExperienceInstability(
			GuiGraphics guiGraphics,
			Minecraft minecraft,
			int centerX,
			int height,
			float instability,
			float flashStrength,
			int jitterX,
			int jitterY) {
		if (minecraft.player.experienceLevel <= 0 && minecraft.player.experienceProgress <= 0.0F) {
			return;
		}

		int x = centerX - 91 + jitterX;
		int y = height - 29 + jitterY;
		int filled = clamp((int) (minecraft.player.experienceProgress * 183.0F), 0, 182);
		int alpha = clamp((int) (14.0F + instability * 20.0F + flashStrength * 22.0F), 0, 72);
		guiGraphics.fill(x, y, x + 182, y + 5, (alpha << 24) | 0x081205);
		if (filled > 0) {
			guiGraphics.fill(x, y + 1, x + filled, y + 4, ((alpha + 18) << 24) | 0x3A8F1E);
		}
		if (instability > 0.42F || flashStrength > 0.0F) {
			guiGraphics.fill(x + 2, y - 1, x + 180, y, ((alpha / 2) << 24) | 0xB4FF92);
		}
	}

	private static void renderShadowFlicker(GuiGraphics guiGraphics, int width, int height) {
		float progress = ClientSanityState.getShadowFlickerProgress();
		float envelope = progress < 0.35F ? progress / 0.35F : (1.0F - progress) / 0.65F;
		float strength = clampFloat(envelope, 0.0F, 1.0F);
		int alpha = clamp((int) (48.0F + strength * 118.0F), 0, 180);
		guiGraphics.fill(0, 0, width, height, (alpha << 24));
		int bandWidth = Math.max(28, width / 5);
		int x = (int) (width * 0.18F + Math.sin(System.currentTimeMillis() / 34.0D) * width * 0.12F);
		guiGraphics.fill(x, 0, Math.min(width, x + bandWidth), height, ((alpha / 2) << 24) | 0x090909);
	}

	private static String stageLabel(SanityThresholds.Stage stage) {
		return switch (stage) {
			case STABLE -> "Stable";
			case UNEASY -> "Uneasy";
			case DISTURBED -> "Disturbed";
			case FRACTURED -> "Fractured";
			case COLLAPSE -> "Collapse";
		};
	}

	private static int stageColor(SanityThresholds.Stage stage) {
		return switch (stage) {
			case STABLE -> 0xFFBAC5C7;
			case UNEASY -> 0xFFD2B892;
			case DISTURBED -> 0xFFC78A71;
			case FRACTURED -> 0xFFB85B56;
			case COLLAPSE -> 0xFFAF3030;
		};
	}

	private static int shade(int color, float factor) {
		int alpha = (color >>> 24) & 0xFF;
		int red = (int) (((color >>> 16) & 0xFF) * factor);
		int green = (int) (((color >>> 8) & 0xFF) * factor);
		int blue = (int) ((color & 0xFF) * factor);
		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

	private static int mix(int value) {
		int seed = value;
		seed ^= seed << 13;
		seed ^= seed >>> 17;
		seed ^= seed << 5;
		return seed;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static float clampFloat(float value, float min, float max) {
		return Math.max(min, Math.min(max, value));
	}

	private static boolean isTorch(ItemStack stack) {
		return !stack.isEmpty() && (stack.is(Items.TORCH) || stack.is(Items.SOUL_TORCH) || stack.is(Items.REDSTONE_TORCH));
	}
}
