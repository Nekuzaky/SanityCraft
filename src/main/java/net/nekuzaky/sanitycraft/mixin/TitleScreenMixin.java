package net.nekuzaky.sanitycraft.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {
	@Inject(method = "render", at = @At("TAIL"))
	private void sanitycraft$renderHorrorMainMenu(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		int width = guiGraphics.guiWidth();
		int height = guiGraphics.guiHeight();
		long time = System.currentTimeMillis();

		int vignetteAlpha = 120;
		guiGraphics.fill(0, 0, width, 24, (vignetteAlpha << 24) | 0x140000);
		guiGraphics.fill(0, height - 24, width, height, (vignetteAlpha << 24) | 0x140000);
		guiGraphics.fill(0, 0, 24, height, (vignetteAlpha << 24) | 0x140000);
		guiGraphics.fill(width - 24, 0, width, height, (vignetteAlpha << 24) | 0x140000);

		// Soft global darkening to push horror mood.
		guiGraphics.fill(0, 0, width, height, 0x35000000);

		// Glitch scanlines.
		int scanAlpha = 18 + (int) ((Math.sin(time / 50.0D) + 1.0D) * 0.5D * 20.0D);
		for (int y = 0; y < height; y += 4) {
			guiGraphics.fill(0, y, width, y + 1, (scanAlpha << 24) | 0x101010);
		}

		// Occasional silhouette flash.
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

		Font font = Minecraft.getInstance().font;
		int jitter = (int) (Math.sin(time / 110.0D) * 2.0D);
		guiGraphics.drawString(font, "SANITYCRAFT", 18 + jitter, 18, 0xFFE2D2D2, true);
		guiGraphics.drawString(font, "you are not alone", 18, 32, 0xFFB98080, false);
		guiGraphics.drawString(font, "Press a button. Regret it later.", 18, height - 18, 0xFF9A7A7A, false);
	}
}
