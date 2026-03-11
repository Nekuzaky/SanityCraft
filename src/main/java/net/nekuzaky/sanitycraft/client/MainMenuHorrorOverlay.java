package net.nekuzaky.sanitycraft.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;

public class MainMenuHorrorOverlay {
	private static boolean registered = false;
	private static final String WINDOW_TITLE = "SanityCraft";

	private MainMenuHorrorOverlay() {
	}

	public static void register() {
		if (registered) {
			return;
		}
		registered = true;

		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!(screen instanceof TitleScreen)) {
				return;
			}
			ScreenEvents.afterRender(screen).register((current, guiGraphics, mouseX, mouseY, deltaTracker) -> renderOverlay(guiGraphics));
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.getWindow() == null || client.screen == null) {
				return;
			}
			if (!(client.screen instanceof TitleScreen)) {
				return;
			}
			client.getWindow().setTitle(WINDOW_TITLE);
		});
	}

	private static void renderOverlay(GuiGraphics guiGraphics) {
		int width = guiGraphics.guiWidth();
		int height = guiGraphics.guiHeight();
		long time = System.currentTimeMillis();

		int vignetteAlpha = 120;
		guiGraphics.fill(0, 0, width, 24, (vignetteAlpha << 24) | 0x140000);
		guiGraphics.fill(0, height - 24, width, height, (vignetteAlpha << 24) | 0x140000);
		guiGraphics.fill(0, 0, 24, height, (vignetteAlpha << 24) | 0x140000);
		guiGraphics.fill(width - 24, 0, width, height, (vignetteAlpha << 24) | 0x140000);
		guiGraphics.fill(0, 0, width, height, 0x35000000);

		int scanAlpha = 18 + (int) ((Math.sin(time / 50.0D) + 1.0D) * 0.5D * 20.0D);
		for (int y = 0; y < height; y += 4) {
			guiGraphics.fill(0, y, width, y + 1, (scanAlpha << 24) | 0x101010);
		}

		if ((time / 260L) % 9L == 0L) {
			int cx = width / 2 + (int) (Math.sin(time / 90.0D) * 24.0D);
			int top = height / 2 - 70;
			int color = 0x8A000000;
			guiGraphics.fill(cx - 14, top, cx + 14, top + 52, color);
			guiGraphics.fill(cx - 24, top + 16, cx - 14, top + 48, color);
			guiGraphics.fill(cx + 14, top + 16, cx + 24, top + 48, color);
			guiGraphics.fill(cx - 11, top + 52, cx - 2, top + 84, color);
			guiGraphics.fill(cx + 2, top + 52, cx + 11, top + 84, color);
			guiGraphics.fill(cx - 7, top + 12, cx - 3, top + 15, 0xB08A0000);
			guiGraphics.fill(cx + 3, top + 12, cx + 7, top + 15, 0xB08A0000);
		}

		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		int jitter = (int) (Math.sin(time / 110.0D) * 2.0D);
		guiGraphics.drawString(font, "SANITYCRAFT", 18 + jitter, 18, 0xFFE2D2D2, true);
		guiGraphics.drawString(font, "you are not alone", 18, 32, 0xFFB98080, false);
		guiGraphics.drawString(font, "Press a button. Regret it later.", 18, height - 18, 0xFF9A7A7A, false);
	}
}
