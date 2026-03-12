package net.nekuzaky.sanitycraft.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;

public class MainMenuHorrorOverlay {
	private static final String[] MENU_LINES = {
			"you are not alone",
			"the menu is listening",
			"leave the lights on",
			"something followed you home",
			"the quiet is fake"
	};
	private static boolean registered = false;
	private static final String WINDOW_TITLE = "SanityCraft";

	private MainMenuHorrorOverlay() {
	}

	public static void register() {
		if (registered) {
			return;
		}
		registered = true;

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.getWindow() == null || client.screen == null) {
				return;
			}
			if (client.screen.getClass() == TitleScreen.class) {
				client.setScreen(new SanitycraftTitleScreen());
				return;
			}
			if (client.screen instanceof SanitycraftTitleScreen) {
				client.getWindow().setTitle(WINDOW_TITLE);
			}
		});
	}

	public static void renderOverlay(GuiGraphics guiGraphics, TitleScreen screen, float partialTick) {
		int width = guiGraphics.guiWidth();
		int height = guiGraphics.guiHeight();
		long time = System.currentTimeMillis();
		int lineIndex = (int) ((time / 3800L) % MENU_LINES.length);

		int vignetteAlpha = 138;
		guiGraphics.fill(0, 0, width, 24, (vignetteAlpha << 24) | 0x140000);
		guiGraphics.fill(0, height - 24, width, height, (vignetteAlpha << 24) | 0x140000);
		guiGraphics.fill(0, 0, 24, height, (vignetteAlpha << 24) | 0x140000);
		guiGraphics.fill(width - 24, 0, width, height, (vignetteAlpha << 24) | 0x140000);
		guiGraphics.fill(0, 0, width, height, 0x43000000);

		renderMovingCurtains(guiGraphics, width, height, time);

		int scanAlpha = 18 + (int) ((Math.sin(time / 50.0D) + 1.0D) * 0.5D * 20.0D);
		for (int y = 0; y < height; y += 4) {
			guiGraphics.fill(0, y, width, y + 1, (scanAlpha << 24) | 0x101010);
		}

		renderMenuWatcher(guiGraphics, width, height, time);
		renderStaticGlitches(guiGraphics, width, height, time);

		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		int jitter = (int) (Math.sin(time / 110.0D) * 2.0D);
		int titleY = 18 + (int) (Math.cos(time / 420.0D) * 2.0D);
		guiGraphics.drawString(font, "SANITYCRAFT", 18 + jitter + 1, titleY, 0xA0000000, true);
		guiGraphics.drawString(font, "SANITYCRAFT", 18 + jitter, titleY, 0xFFE6DCDC, true);
		guiGraphics.drawString(font, MENU_LINES[lineIndex], 18, 34, 0xFFB98080, false);
		guiGraphics.drawString(font, "singleplayer is safer than silence", 18, height - 28, 0xFF9A7A7A, false);
		guiGraphics.drawString(font, "Press a button. Regret it later.", 18, height - 16, 0xFF7A5C5C, false);
		drawRightSideLore(guiGraphics, font, width, height, lineIndex, time);
	}

	private static void renderMovingCurtains(GuiGraphics guiGraphics, int width, int height, long time) {
		for (int i = 0; i < 4; i++) {
			double wave = Math.sin(time / (1100.0D + i * 180.0D) + i * 0.8D);
			int bandWidth = 34 + i * 12;
			int alpha = 12 + i * 6;
			int left = (int) (wave * 12.0D) - i * 4;
			int right = width - bandWidth + (int) (-wave * 10.0D) + i * 3;
			guiGraphics.fill(left, 0, left + bandWidth, height, (alpha << 24) | 0x140000);
			guiGraphics.fill(right, 0, right + bandWidth, height, (alpha << 24) | 0x140000);
		}
	}

	private static void renderMenuWatcher(GuiGraphics guiGraphics, int width, int height, long time) {
		if ((time / 240L) % 13L > 1L) {
			return;
		}
		int cx = width / 2 + (int) (Math.sin(time / 90.0D) * 24.0D);
		int top = height / 2 - 84;
		int color = 0x94000000;
		guiGraphics.fill(cx - 15, top, cx + 15, top + 58, color);
		guiGraphics.fill(cx - 27, top + 18, cx - 15, top + 54, color);
		guiGraphics.fill(cx + 15, top + 18, cx + 27, top + 54, color);
		guiGraphics.fill(cx - 12, top + 58, cx - 2, top + 94, color);
		guiGraphics.fill(cx + 2, top + 58, cx + 12, top + 94, color);
		guiGraphics.fill(cx - 8, top + 14, cx - 3, top + 17, 0xB08A0000);
		guiGraphics.fill(cx + 3, top + 14, cx + 8, top + 17, 0xB08A0000);
	}

	private static void renderStaticGlitches(GuiGraphics guiGraphics, int width, int height, long time) {
		int phase = (int) (time / 130L);
		for (int i = 0; i < 6; i++) {
			int seed = mix(phase + i * 31);
			int blockWidth = 16 + Math.abs(seed % 50);
			int blockHeight = 3 + Math.abs((seed >> 4) % 14);
			int x = Math.abs((seed >> 8) % Math.max(1, width - blockWidth));
			int y = Math.abs((seed >> 15) % Math.max(1, height - blockHeight));
			int alpha = 10 + Math.abs((seed >> 20) % 36);
			guiGraphics.fill(x, y, x + blockWidth, y + blockHeight, (alpha << 24) | (((seed >> 3) & 1) == 0 ? 0x130000 : 0x101010));
		}
	}

	private static void drawRightSideLore(GuiGraphics guiGraphics, Font font, int width, int height, int lineIndex, long time) {
		String[] rightLines = {
				"Day " + (13 + lineIndex),
				"Sleep quality: poor",
				"Windows checked: no",
				"Noise source: unknown"
		};
		int x = width - 144;
		int baseY = height - 74;
		for (int i = 0; i < rightLines.length; i++) {
			int drift = (int) (Math.sin(time / 260.0D + i * 0.9D) * 2.0D);
			guiGraphics.drawString(font, rightLines[i], x + drift, baseY + i * 11, 0x907C6666, false);
		}
	}

	private static int mix(int value) {
		int seed = value;
		seed ^= seed << 13;
		seed ^= seed >>> 17;
		seed ^= seed << 5;
		return seed;
	}
}
