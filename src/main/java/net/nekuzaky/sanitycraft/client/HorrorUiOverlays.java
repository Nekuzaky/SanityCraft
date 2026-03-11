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
			renderSilhouetteFlash(guiGraphics, width, height);
		}
		renderScriptedJumpscare(guiGraphics, width, height);
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

		float progress = SanityClientState.getJumpscareProgress();
		int variant = SanityClientState.getJumpscareVariant() % 3;
		int alpha = (int) (230.0F * (1.0F - progress * 0.75F));
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
			int flash = (int) (255.0F * (1.0F - progress / 0.12F));
			guiGraphics.fill(0, 0, width, height, (flash << 24) | 0x00FFFFFF);
		}
	}
}
