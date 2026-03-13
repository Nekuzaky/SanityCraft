package com.sanitycraft.client.menu;

import com.sanitycraft.SanityCraft;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public final class SanityCraftMenuAtmosphereRenderer {
	private static final ResourceLocation[] PANORAMA_TEXTURES = {
			SanityCraft.id("textures/gui/title/background/panorama_0.png"),
			SanityCraft.id("textures/gui/title/background/panorama_1.png"),
			SanityCraft.id("textures/gui/title/background/panorama_2.png"),
			SanityCraft.id("textures/gui/title/background/panorama_3.png"),
			SanityCraft.id("textures/gui/title/background/panorama_4.png"),
			SanityCraft.id("textures/gui/title/background/panorama_5.png")
	};
	private static final int[] PANORAMA_WIDTHS = {566, 522, 517, 565, 565, 526};
	private static final int[] PANORAMA_HEIGHTS = {237, 241, 234, 236, 237, 238};

	private SanityCraftMenuAtmosphereRenderer() {
	}

	public static void renderForestBackdrop(GuiGraphics guiGraphics, int width, int height, long time, float distortion, boolean titleScreen) {
		int frameIndex = (int) ((time / (titleScreen ? 3600L : 4200L)) % PANORAMA_TEXTURES.length);
		float breathing = 1.0F + (float) Math.sin(time / (titleScreen ? 3600.0D : 4200.0D)) * (titleScreen ? 0.015F : 0.011F);
		int overscan = titleScreen ? 120 : 92;
		int drawWidth = Math.round((width + overscan) * breathing);
		int drawHeight = Math.round((height + overscan) * breathing);
		int driftRangeX = titleScreen ? 10 : 7;
		int driftRangeY = titleScreen ? 8 : 6;
		int x = (width - drawWidth) / 2 + (int) (Math.sin(time / 2600.0D) * (driftRangeX + distortion * 8.0D));
		int y = (height - drawHeight) / 2 + (int) (Math.cos(time / 3400.0D) * (driftRangeY + distortion * 6.0D));

		guiGraphics.blit(
				RenderPipelines.GUI_TEXTURED,
				PANORAMA_TEXTURES[frameIndex],
				x,
				y,
				0.0F,
				0.0F,
				drawWidth,
				drawHeight,
				PANORAMA_WIDTHS[frameIndex],
				PANORAMA_HEIGHTS[frameIndex]);

		renderBackdropGrade(guiGraphics, width, height, time, distortion, titleScreen);
	}

	public static void renderTitleAtmosphere(GuiGraphics guiGraphics, int width, int height, long time, float distortion) {
		int flicker = 0x20 + (int) ((Math.sin(time / 150.0D) + 1.0D) * (4.0D + distortion * 5.0D));
		guiGraphics.fill(0, 0, width, height, (flicker << 24));
		renderTopAndBottomPressure(guiGraphics, width, height, 0x86100505, 0x8E0C0505);
		renderCenterPressure(guiGraphics, width, height, time, 0.09F + distortion * 0.07F, 0x180000);
		renderScanlines(guiGraphics, width, height, 4, 0x0C000000, 0.16F + distortion * 0.06F);
		renderFog(guiGraphics, width, height, time, 0.18F + distortion * 0.08F, 0x141418, 0.22F, 0.18F);
		renderFog(guiGraphics, width, height, time + 580L, 0.13F + distortion * 0.05F, 0x170F12, 0.46F, 0.14F);
		renderTreelineMovement(guiGraphics, width, height, time, 0.14F + distortion * 0.08F);
		renderSideVignette(guiGraphics, width, height, 0.68F, 0x130707, 0x090203);
		renderGrain(guiGraphics, width, height, time, 0.05F + distortion * 0.04F, 11);
	}

	public static void renderSubmenuAtmosphere(GuiGraphics guiGraphics, int width, int height, long time, float distortion) {
		guiGraphics.fill(0, 0, width, height, 0x12000000);
		renderTopAndBottomPressure(guiGraphics, width, height, 0x600F0505, 0x70090505);
		renderCenterPressure(guiGraphics, width, height, time, 0.05F + distortion * 0.04F, 0x120000);
		renderScanlines(guiGraphics, width, height, 5, 0x07000000, 0.11F + distortion * 0.04F);
		renderFog(guiGraphics, width, height, time + 260L, 0.08F + distortion * 0.04F, 0x141418, 0.26F, 0.12F);
		renderTreelineMovement(guiGraphics, width, height, time + 420L, 0.06F + distortion * 0.05F);
		renderSideVignette(guiGraphics, width, height, 0.76F, 0x0D0707, 0x050203);
		renderGrain(guiGraphics, width, height, time, 0.03F + distortion * 0.02F, 13);
	}

	public static void renderTitleStage(GuiGraphics guiGraphics, SanityCraftMenuLayout.TitleLayout layout, int buttonCount, long time, float distortion) {
		int stackHeight = buttonCount * layout.buttonHeight() + Math.max(0, buttonCount - 1) * layout.buttonSpacing();
		int stageLeft = layout.buttonStartX() - Mth.clamp(Math.round(layout.buttonWidth() * 0.24F), 26, 46);
		int stageRight = layout.buttonStartX() + layout.buttonWidth() + Mth.clamp(Math.round(layout.buttonWidth() * 0.24F), 26, 46);
		int stageTop = layout.buttonStartY() - Mth.clamp(Math.round(26.0F * layout.scale()), 18, 32);
		int stageBottom = layout.buttonStartY() + stackHeight + Mth.clamp(Math.round(32.0F * layout.scale()), 22, 40);
		int frameAlpha = 0x28 + (int) ((Math.sin(time / 620.0D) + 1.0D) * (4.0D + distortion * 6.0D));
		int centerGlowWidth = Mth.clamp(Math.round(layout.buttonWidth() * (0.72F + distortion * 0.10F)), 150, 280);

		guiGraphics.fill(stageLeft, stageTop, stageRight, stageBottom, (frameAlpha << 24) | 0x0A0202);
		guiGraphics.fill(stageLeft - 1, stageTop - 1, stageRight + 1, stageBottom + 1, 0x18000000);
		guiGraphics.fill(stageLeft + 8, stageTop + 8, stageRight - 8, stageBottom - 8, 0x12000000);
		guiGraphics.fill(
				layout.screenWidth() / 2 - centerGlowWidth / 2,
				stageTop - 20,
				layout.screenWidth() / 2 + centerGlowWidth / 2,
				stageBottom + 8,
				(10 + (int) (distortion * 16.0F) << 24) | 0x180404);
		renderEdgeGlow(guiGraphics, stageLeft, stageTop, stageRight, stageBottom, 0x3C1A0B0B);
	}

	public static void renderSubmenuPanel(GuiGraphics guiGraphics, SanityCraftMenuLayout.SubmenuLayout layout, long time, float distortion) {
		int outerPad = Mth.clamp(Math.round(18.0F * layout.scale()), 12, 24);
		int innerPad = Mth.clamp(Math.round(8.0F * layout.scale()), 6, 12);
		int pulseAlpha = 14 + (int) ((Math.sin(time / 880.0D) + 1.0D) * (3.0D + distortion * 5.0D));
		int left = layout.contentLeft() - outerPad;
		int right = layout.contentLeft() + layout.contentWidth() + outerPad;
		int top = layout.contentTop() - outerPad;
		int bottom = layout.contentBottom() + outerPad / 2;

		guiGraphics.fill(left, top, right, bottom, ((28 + pulseAlpha) << 24) | 0x070202);
		guiGraphics.fill(left + innerPad, top + innerPad, right - innerPad, bottom - innerPad, 0x16000000);
		guiGraphics.fill(left, top, right, top + 1, 0x38140A0A);
		guiGraphics.fill(left, bottom - 1, right, bottom, 0x280A0505);
		guiGraphics.fill(left, top, left + 1, bottom, 0x240A0505);
		guiGraphics.fill(right - 1, top, right, bottom, 0x20060303);
		renderCenterPressure(
				guiGraphics,
				layout.screenWidth(),
				layout.screenHeight(),
				time + 320L,
				0.03F + distortion * 0.03F,
				0x160404);
	}

	public static void renderLogoGlitchBand(GuiGraphics guiGraphics, int x, int y, int width, int height, int shiftPx, Runnable drawBand) {
		if (shiftPx == 0) {
			return;
		}
		int bandTop = y + height / 3;
		int bandBottom = Math.min(y + height, bandTop + Math.max(6, height / 7));
		guiGraphics.enableScissor(x, bandTop, x + width, bandBottom);
		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().translate(shiftPx, 0.0F);
		drawBand.run();
		guiGraphics.pose().popMatrix();
		guiGraphics.disableScissor();
	}

	private static void renderBackdropGrade(GuiGraphics guiGraphics, int width, int height, long time, float distortion, boolean titleScreen) {
		int topAlpha = titleScreen ? 86 : 72;
		int bottomAlpha = titleScreen ? 116 : 90;
		guiGraphics.fill(0, 0, width, height, 0x18000000);
		guiGraphics.fill(0, 0, width, Math.max(28, height / 8), (topAlpha << 24) | 0x090304);
		guiGraphics.fill(0, height - Math.max(42, height / 6), width, height, (bottomAlpha << 24) | 0x050102);

		int horizon = Math.round(height * (titleScreen ? 0.67F : 0.72F));
		int driftAlpha = 18 + (int) ((Math.sin(time / 1700.0D) + 1.0D) * (5.0D + distortion * 7.0D));
		guiGraphics.fill(0, horizon - 24, width, horizon + 52, (driftAlpha << 24) | 0x060203);
	}

	private static void renderTopAndBottomPressure(GuiGraphics guiGraphics, int width, int height, int topColor, int bottomColor) {
		guiGraphics.fill(0, 0, width, 34, topColor);
		guiGraphics.fill(0, height - 46, width, height, bottomColor);
	}

	private static void renderCenterPressure(GuiGraphics guiGraphics, int width, int height, long time, float strength, int rgb) {
		int columnWidth = Math.max(120, Math.round(width * 0.20F));
		int centerX = width / 2 + (int) (Math.sin(time / 2900.0D) * 8.0D);
		for (int x = centerX - columnWidth / 2; x < centerX + columnWidth / 2; x++) {
			float distance = Math.abs(x - centerX) / (float) Math.max(1, columnWidth / 2);
			float falloff = 1.0F - distance * distance;
			int alpha = Math.round((10.0F + width * 0.002F) * strength * falloff);
			if (alpha > 0) {
				guiGraphics.fill(x, 0, x + 1, height, (alpha << 24) | rgb);
			}
		}
	}

	private static void renderSideVignette(GuiGraphics guiGraphics, int width, int height, float falloffStart, int nearRgb, int farRgb) {
		int edgeWidth = Math.max(100, Math.round(width * 0.18F));
		for (int offset = 0; offset < edgeWidth; offset++) {
			float normalized = offset / (float) edgeWidth;
			float falloff = normalized < falloffStart ? 1.0F : 1.0F - ((normalized - falloffStart) / Math.max(0.001F, 1.0F - falloffStart));
			int nearAlpha = Math.max(0, Math.round(34.0F * falloff));
			int farAlpha = Math.max(0, Math.round(22.0F * falloff));
			guiGraphics.fill(offset, 0, offset + 1, height, (nearAlpha << 24) | nearRgb);
			guiGraphics.fill(width - offset - 1, 0, width - offset, height, (nearAlpha << 24) | nearRgb);
			if ((offset & 3) == 0) {
				guiGraphics.fill(offset, 0, offset + 1, height, (farAlpha << 24) | farRgb);
				guiGraphics.fill(width - offset - 1, 0, width - offset, height, (farAlpha << 24) | farRgb);
			}
		}
	}

	private static void renderScanlines(GuiGraphics guiGraphics, int width, int height, int step, int color, float intensity) {
		int alpha = Math.max(3, Math.min(18, Math.round(255.0F * intensity)));
		int tinted = (alpha << 24) | (color & 0x00FFFFFF);
		for (int y = 0; y < height; y += step) {
			guiGraphics.fill(0, y, width, y + 1, tinted);
		}
	}

	private static void renderTreelineMovement(GuiGraphics guiGraphics, int width, int height, long time, float strength) {
		int horizon = Math.round(height * 0.69F);
		int bandHeight = Math.max(34, Math.round(height * 0.12F));
		int clusters = Math.max(5, width / 360);

		for (int cluster = 0; cluster < clusters; cluster++) {
			double wave = Math.sin(time / (2400.0D + cluster * 160.0D) + cluster * 1.13D);
			int centerX = Math.round((cluster + 0.5F) * width / clusters + (float) (wave * (14.0D + strength * 28.0D)));
			int clusterWidth = Math.max(44, Math.round(width * (0.045F + cluster * 0.003F)));
			int top = horizon - Math.max(10, clusterWidth / 7);
			int bottom = Math.min(height, horizon + bandHeight);
			for (int x = centerX - clusterWidth / 2; x < centerX + clusterWidth / 2; x++) {
				if (x < 0 || x >= width) {
					continue;
				}
				float distance = Math.abs(x - centerX) / (float) Math.max(1, clusterWidth / 2);
				float falloff = 1.0F - distance * distance;
				int alpha = Math.max(0, Math.round((18.0F + cluster * 2.0F) * strength * falloff));
				if (alpha > 0) {
					guiGraphics.fill(x, top, x + 1, bottom, (alpha << 24) | 0x040102);
				}
			}
		}
	}

	private static void renderFog(GuiGraphics guiGraphics, int width, int height, long time, float alphaScale, int rgb, float anchorY, float driftScale) {
		for (int i = 0; i < 4; i++) {
			double wave = Math.sin(time / (1800.0D + i * 260.0D) + i * 0.85D);
			int centerY = (int) (height * (anchorY + i * 0.12F) + wave * (10.0D + height * 0.01D * driftScale));
			int bandHeight = 24 + i * 12;
			int half = Math.max(1, bandHeight / 2);
			for (int y = Math.max(0, centerY - half); y < Math.min(height, centerY + half); y++) {
				float distance = Math.abs((y - centerY) / (float) half);
				float falloff = 1.0F - distance * distance;
				int alpha = (int) ((18 + i * 7) * alphaScale * falloff);
				if (alpha > 0) {
					guiGraphics.fill(0, y, width, y + 1, (alpha << 24) | rgb);
				}
			}
		}
	}

	private static void renderGrain(GuiGraphics guiGraphics, int width, int height, long time, float intensity, int step) {
		int seed = (int) ((time / 50L) & 0x7FFFFFFF);
		int alphaBase = Math.max(3, Math.min(16, Math.round(intensity * 255.0F)));
		for (int y = 0; y < height; y += step) {
			for (int x = 0; x < width; x += step) {
				int hash = seed + x * 7349 + y * 9151;
				if ((hash & 3) != 0) {
					continue;
				}
				int alpha = alphaBase + (hash & 5);
				guiGraphics.fill(x, y, Math.min(width, x + 2), Math.min(height, y + 2), (alpha << 24) | 0x101010);
			}
		}
	}

	private static void renderEdgeGlow(GuiGraphics guiGraphics, int left, int top, int right, int bottom, int color) {
		guiGraphics.fill(left, top, right, top + 1, color);
		guiGraphics.fill(left, bottom - 1, right, bottom, color & 0x66FFFFFF);
		guiGraphics.fill(left, top, left + 1, bottom, color & 0x66FFFFFF);
		guiGraphics.fill(right - 1, top, right, bottom, color & 0x44FFFFFF);
	}
}
