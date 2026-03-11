package net.nekuzaky.sanitycraft.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class HorrorUiOverlays {
	private HorrorUiOverlays() {
	}

	public static void renderGlobalFearOverlay(GuiGraphics guiGraphics, int sanity) {
		int width = guiGraphics.guiWidth();
		int height = guiGraphics.guiHeight();
		int fear = 100 - sanity;
		if (fear <= 0) {
			return;
		}

		int darknessAlpha = Math.min(90, fear);
		int tintAlpha = Math.min(70, fear / 2);
		guiGraphics.fill(0, 0, width, height, (darknessAlpha << 24));
		guiGraphics.fill(0, 0, width, 18, (tintAlpha << 24) | 0x220000);
		guiGraphics.fill(0, height - 18, width, height, (tintAlpha << 24) | 0x220000);
		guiGraphics.fill(0, 0, 18, height, (tintAlpha << 24) | 0x220000);
		guiGraphics.fill(width - 18, 0, width, height, (tintAlpha << 24) | 0x220000);

		if (sanity <= 35) {
			int pulse = (int) ((Math.sin(System.currentTimeMillis() / 130.0D) + 1.0D) * 0.5D * 60.0D);
			guiGraphics.fill(0, 0, width, height, (Math.min(60, pulse) << 24) | 0x330000);
		}
		if (sanity <= 25) {
			int scanAlpha = 18 + (int) ((Math.sin(System.currentTimeMillis() / 45.0D) + 1.0D) * 0.5D * 14.0D);
			for (int y = 0; y < height; y += 4) {
				guiGraphics.fill(0, y, width, y + 1, (scanAlpha << 24) | 0x101010);
			}
		}
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
	}
}
