package com.sanitycraft.client.menu;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;

@Environment(EnvType.CLIENT)
public final class SanityCraftScreenStyling {
	private SanityCraftScreenStyling() {
	}

	public static void renderMenuBackdrop(GuiGraphics guiGraphics, int width, int height) {
		long time = System.currentTimeMillis();
		float distortion = SanityCraftMenuEffects.getDistortionStrength();
		SanityCraftMenuAtmosphereRenderer.renderForestBackdrop(guiGraphics, width, height, time, distortion, false);
		SanityCraftMenuAtmosphereRenderer.renderSubmenuAtmosphere(guiGraphics, width, height, time, distortion);
	}

	public static void renderLoadingBackdrop(GuiGraphics guiGraphics, int width, int height) {
		long time = System.currentTimeMillis();
		float distortion = SanityCraftMenuEffects.getDistortionStrength();
		SanityCraftMenuAtmosphereRenderer.renderForestBackdrop(guiGraphics, width, height, time, distortion, false);
		SanityCraftMenuAtmosphereRenderer.renderSubmenuAtmosphere(guiGraphics, width, height, time, distortion);

		int panelWidth = Math.max(240, Math.round(width * 0.38F));
		int panelHeight = Math.max(110, Math.round(height * 0.18F));
		int left = width / 2 - panelWidth / 2;
		int right = width / 2 + panelWidth / 2;
		int top = height / 2 - panelHeight / 2;
		int bottom = height / 2 + panelHeight / 2;

		guiGraphics.fill(left, top, right, bottom, 0x46080404);
		guiGraphics.fill(left + 3, top + 3, right - 3, bottom - 3, 0x16000000);
		guiGraphics.fill(left, top, right, top + 1, 0x6A7A3B3B);
		guiGraphics.fill(left, bottom - 1, right, bottom, 0x420C0505);
		guiGraphics.fill(left, top, left + 1, bottom, 0x30100606);
		guiGraphics.fill(right - 1, top, right, bottom, 0x24060303);
		guiGraphics.fill(width / 2 - panelWidth / 4, top - 14, width / 2 + panelWidth / 4, top - 12, 0x3CC48A80);
	}
}
