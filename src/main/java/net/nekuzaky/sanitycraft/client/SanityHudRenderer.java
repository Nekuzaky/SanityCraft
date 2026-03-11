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

		guiGraphics.fill(x - 4 + jitter, y - 4 + jitter, x + barWidth + 4 + jitter, y + barHeight + 24 + jitter, 0x9C080808);
		guiGraphics.fill(x + jitter, y + jitter, x + barWidth + jitter, y + barHeight + jitter, 0xFF111111);
		guiGraphics.fill(x + jitter, y + jitter, x + fill + jitter, y + barHeight + jitter, color);

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
}
