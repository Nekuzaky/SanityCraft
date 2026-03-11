package net.nekuzaky.sanitycraft.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class SanityHudRenderer {
	private SanityHudRenderer() {
	}

	public static void register() {
		HudRenderCallback.EVENT.register(SanityHudRenderer::render);
	}

	private static void render(GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.options.hideGui) {
			return;
		}

		int sanity = SanityClientState.getSanity();
		int barWidth = 120;
		int barHeight = 10;
		int x = 12;
		int y = 12;

		int fill = (int) Math.round((sanity / 100.0D) * barWidth);
		int color = getColor(sanity);
		int jitter = sanity <= 30 ? (int) ((Math.sin(System.currentTimeMillis() / 60.0D)) * 2.0D) : 0;

		renderMetalContainer(guiGraphics, x + jitter, y + jitter, barWidth, barHeight);
		renderMetalFill(guiGraphics, x + jitter, y + jitter, fill, barHeight, color);

		Font font = minecraft.font;
		guiGraphics.drawString(font, "SANITY  " + sanity + "/100", x + 2 + jitter, y + barHeight + 5 + jitter, 0xFFE5D9D9, false);
		guiGraphics.drawString(font, getStageLabel(sanity), x + 2 + jitter, y + barHeight + 14 + jitter, 0xFFC49D9D, false);
		HorrorUiOverlays.renderGlobalFearOverlay(guiGraphics, sanity);
	}

	private static int getColor(int sanity) {
		if (sanity > 80) {
			return 0xFF59D665;
		}
		if (sanity > 60) {
			return 0xFFE3D15C;
		}
		if (sanity > 40) {
			return 0xFFE39E5C;
		}
		if (sanity > 20) {
			return 0xFFE06A6A;
		}
		return 0xFFA43A3A;
	}

	private static String getStageLabel(int sanity) {
		if (sanity > 80) {
			return "State: Stable";
		}
		if (sanity > 60) {
			return "State: Uneasy";
		}
		if (sanity > 40) {
			return "State: Disturbed";
		}
		if (sanity > 20) {
			return "State: Unstable";
		}
		return "State: Fractured";
	}

	private static void renderMetalContainer(GuiGraphics guiGraphics, int x, int y, int width, int height) {
		guiGraphics.fill(x - 5, y - 5, x + width + 5, y + height + 26, 0x8A050505);
		guiGraphics.fill(x - 2, y - 2, x + width + 2, y + height + 2, 0xFF4A4A4A);
		guiGraphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFFB9B9B9);
		guiGraphics.fill(x, y, x + width, y + height, 0xFF1A1A1A);

		for (int i = 0; i < height; i++) {
			int tone = 42 + (int) ((i / (double) Math.max(1, height)) * 32.0D);
			int line = 0xFF000000 | (tone << 16) | (tone << 8) | tone;
			guiGraphics.fill(x, y + i, x + width, y + i + 1, line);
		}

		for (int i = 0; i < width; i += 6) {
			int streak = 0x18000000 | (0x22 << 16) | (0x22 << 8) | 0x22;
			guiGraphics.fill(x + i, y, x + i + 1, y + height, streak);
		}
	}

	private static void renderMetalFill(GuiGraphics guiGraphics, int x, int y, int fillWidth, int height, int stageColor) {
		if (fillWidth <= 0) {
			return;
		}
		int clamped = Math.max(1, fillWidth);
		for (int i = 0; i < height; i++) {
			float t = i / (float) Math.max(1, height - 1);
			int alpha = 0xFF;
			int r = (int) (((stageColor >> 16) & 0xFF) * (0.75F + (1.0F - t) * 0.25F));
			int g = (int) (((stageColor >> 8) & 0xFF) * (0.75F + (1.0F - t) * 0.25F));
			int b = (int) ((stageColor & 0xFF) * (0.75F + (1.0F - t) * 0.25F));
			int line = (alpha << 24) | (r << 16) | (g << 8) | b;
			guiGraphics.fill(x, y + i, x + clamped, y + i + 1, line);
		}

		int shineX = x + (int) ((System.currentTimeMillis() / 18L) % Math.max(2, clamped + 10)) - 10;
		guiGraphics.fill(shineX, y, shineX + 2, y + height, 0x55FFFFFF);
	}
}
