package net.nekuzaky.sanitycraft.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.nekuzaky.sanitycraft.init.SanitycraftModItems;

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
		int iconSize = 16;
		int iconX = 8;
		int iconY = 5;
		int barWidth = 82;
		int barHeight = 5;
		int x = iconX + iconSize + 6;
		int y = 8;

		int fill = (int) Math.round((sanity / 100.0D) * barWidth);
		int color = getColor(sanity);
		int jitter = sanity <= 20 ? (int) ((Math.sin(System.currentTimeMillis() / 70.0D)) * 1.0D) : 0;

		renderPillBadge(guiGraphics, iconX + jitter, iconY + jitter);
		renderMetalContainer(guiGraphics, x + jitter, y + jitter, barWidth, barHeight);
		renderMetalFill(guiGraphics, x + jitter, y + jitter, fill, barHeight, color);

		Font font = minecraft.font;
		if (sanity <= 40) {
			guiGraphics.drawString(font, getStageLabel(sanity), x + 2 + jitter, y + barHeight + 3 + jitter, 0xCF9EA6AF, false);
		}
		if (SanityClientState.isZeroSanityActive()) {
			renderZeroSanityHudGlitch(guiGraphics, minecraft, sanity);
		}
		HorrorUiOverlays.renderGlobalFearOverlay(guiGraphics, sanity);
	}

	private static int getColor(int sanity) {
		if (sanity > 80) {
			return 0xFFD9DEE4;
		}
		if (sanity > 60) {
			return 0xFFBEC5CE;
		}
		if (sanity > 40) {
			return 0xFFA0A7B0;
		}
		if (sanity > 20) {
			return 0xFF7E858F;
		}
		return 0xFF5F656E;
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
		guiGraphics.fill(x - 2, y - 2, x + width + 2, y + height + 2, 0x6A050505);
		guiGraphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xC84A4A4A);
		guiGraphics.fill(x, y, x + width, y + height, 0xC21A1A1A);

		for (int i = 0; i < height; i++) {
			int tone = 42 + (int) ((i / (double) Math.max(1, height)) * 32.0D);
			int line = 0xCC000000 | (tone << 16) | (tone << 8) | tone;
			guiGraphics.fill(x, y + i, x + width, y + i + 1, line);
		}

		for (int i = 0; i < width; i += 6) {
			int streak = 0x12000000 | (0x22 << 16) | (0x22 << 8) | 0x22;
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

	private static void renderPillBadge(GuiGraphics guiGraphics, int x, int y) {
		guiGraphics.fill(x - 2, y - 2, x + 18, y + 18, 0x64080808);
		guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, 0xB6484D55);
		guiGraphics.fill(x, y, x + 16, y + 16, 0xC1202328);
		guiGraphics.fill(x + 1, y + 1, x + 15, y + 2, 0x55FFFFFF);

		if (SanitycraftModItems.PILL != null) {
			try {
				// Pill texture is currently portrait (1024x1536), so we draw with aspect compensation
				// to avoid visible horizontal stretch in a 16x16 HUD slot.
				float scaleX = 0.67F;
				float drawOffsetX = (16.0F - 16.0F * scaleX) * 0.5F;
				guiGraphics.pose().pushMatrix();
				guiGraphics.pose().translate(x + drawOffsetX, y);
				guiGraphics.pose().scale(scaleX, 1.0F);
				guiGraphics.renderItem(new ItemStack(SanitycraftModItems.PILL), 0, 0);
				guiGraphics.pose().popMatrix();
			} catch (Exception ignored) {
			}
		}
	}

	private static void renderZeroSanityHudGlitch(GuiGraphics guiGraphics, Minecraft minecraft, int sanity) {
		float pulse = SanityClientState.getZeroSanityPulse();
		int width = guiGraphics.guiWidth();
		int height = guiGraphics.guiHeight();
		int flashAlpha = clamp(18 + (int) (pulse * 86.0F), 0, 140);
		guiGraphics.fill(0, 0, width, height, (flashAlpha << 24) | 0x240000);

		renderHeartBlinkOverlay(guiGraphics, width, height, pulse);
		renderDurabilityBlinkOverlay(guiGraphics, minecraft, width, height, pulse);

		Font font = minecraft.font;
		int eta = Math.max(0, 30 - SanityClientState.getZeroSanityElapsedSeconds());
		guiGraphics.drawString(font, "MENTAL COLLAPSE IN " + eta + "s", width / 2 - 56, height - 60, 0xFFD35A5A, true);
	}

	private static void renderHeartBlinkOverlay(GuiGraphics guiGraphics, int width, int height, float pulse) {
		int heartsX = width / 2 - 91;
		int heartsY = height - 39;
		int blinkAlpha = clamp(42 + (int) (pulse * 130.0F), 0, 180);
		int color = (blinkAlpha << 24) | 0x8A0000;
		for (int i = 0; i < 10; i++) {
			int hx = heartsX + i * 8;
			guiGraphics.fill(hx, heartsY, hx + 7, heartsY + 7, color);
		}
	}

	private static void renderDurabilityBlinkOverlay(GuiGraphics guiGraphics, Minecraft minecraft, int width, int height, float pulse) {
		int hotbarX = width / 2 - 91;
		int hotbarY = height - 22;
		int alpha = clamp(50 + (int) (pulse * 120.0F), 0, 190);
		for (int slot = 0; slot < 9; slot++) {
			ItemStack stack = minecraft.player.getInventory().getItem(slot);
			if (stack.isEmpty() || !stack.isDamageableItem()) {
				continue;
			}
			int max = Math.max(1, stack.getMaxDamage());
			int current = max - stack.getDamageValue();
			float ratio = Math.max(0.05F, Math.min(1.0F, current / (float) max));
			int fakeLen = Math.max(1, (int) Math.floor(13.0F * ratio * (0.6F + pulse * 0.4F)));
			int x = hotbarX + slot * 20 + 2;
			int y = hotbarY + 16;
			guiGraphics.fill(x, y, x + 13, y + 2, 0x99000000);
			guiGraphics.fill(x, y, x + fakeLen, y + 2, (alpha << 24) | 0xB00000);
		}
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
