package com.sanitycraft.client.menu;

import java.text.Normalizer;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public final class SanityCraftMenuWidgetSkin {
	private SanityCraftMenuWidgetSkin() {
	}

	public static boolean shouldSkinCurrentScreen() {
		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		return screen instanceof SanityCraftMultiplayerScreen
				|| screen instanceof SanityCraftOptionsScreen
				|| screen instanceof SelectWorldScreen;
	}

	public static void renderButtonWidget(GuiGraphics guiGraphics, AbstractWidget widget) {
		SanityCraftMenuButton.renderSkinned(
				guiGraphics,
				widget.getX(),
				widget.getY(),
				widget.getWidth(),
				widget.getHeight(),
				widget.getMessage(),
				resolveAccent(widget),
				widget.isHoveredOrFocused(),
				widget.active,
				1.0F);
	}

	public static void renderSliderWidget(GuiGraphics guiGraphics, AbstractWidget widget, double value) {
		renderButtonWidget(guiGraphics, widget);

		int insetX = Math.min(16, Math.max(10, widget.getWidth() / 9));
		int trackLeft = widget.getX() + insetX;
		int trackRight = widget.getRight() - insetX;
		int trackTop = widget.getBottom() - Math.min(8, Math.max(5, widget.getHeight() / 3));
		int trackBottom = trackTop + 3;
		if (trackRight <= trackLeft) {
			return;
		}

		int knobHalfWidth = 3;
		int knobCenterX = Mth.clamp(
				trackLeft + Math.round((trackRight - trackLeft) * (float) value),
				trackLeft,
				trackRight);

		guiGraphics.fill(trackLeft, trackTop, trackRight, trackBottom, widget.active ? 0x58110808 : 0x38110808);
		guiGraphics.fill(trackLeft, trackTop, knobCenterX, trackBottom, widget.active ? 0xA0C86D58 : 0x606E4A4A);
		guiGraphics.fill(knobCenterX - knobHalfWidth, trackTop - 2, knobCenterX + knobHalfWidth, trackBottom + 2, widget.active ? 0xE8F5D5C8 : 0xB0B59490);
		guiGraphics.fill(knobCenterX - knobHalfWidth - 1, trackTop - 3, knobCenterX + knobHalfWidth + 1, trackBottom + 3, 0x50000000);
	}

	public static void renderListBackground(GuiGraphics guiGraphics, AbstractWidget widget) {
		int left = widget.getX();
		int top = widget.getY();
		int right = widget.getRight();
		int bottom = widget.getBottom();

		guiGraphics.fill(left, top, right, bottom, 0x4A090202);
		guiGraphics.fill(left + 2, top + 2, right - 2, bottom - 2, 0x22000000);
		guiGraphics.fill(left, top, right, top + 1, 0x5A492020);
		guiGraphics.fill(left, bottom - 1, right, bottom, 0x40100606);
		guiGraphics.fill(left, top, left + 1, bottom, 0x30100606);
		guiGraphics.fill(right - 1, top, right, bottom, 0x24060303);

		for (int y = top + 10; y < bottom - 8; y += 18) {
			guiGraphics.fill(left + 3, y, right - 3, y + 1, 0x12000000);
		}
	}

	private static SanityCraftMenuButton.Accent resolveAccent(AbstractWidget widget) {
		String label = normalizeLabel(widget.getMessage().getString());
		if (label.contains("delete") || label.contains("supprimer")) {
			return SanityCraftMenuButton.Accent.EXIT;
		}
		if (label.contains("select")
				|| label.contains("join")
				|| label.contains("play")
				|| label.contains("rejoindre")
				|| label.contains("jouer")
				|| label.contains("ajouter")
				|| label.contains("add")
				|| label.contains("creer")
				|| label.contains("create")
				|| label.contains("new")
				|| label.contains("direct")) {
			return SanityCraftMenuButton.Accent.PRIMARY;
		}
		return SanityCraftMenuButton.Accent.STANDARD;
	}

	private static String normalizeLabel(String label) {
		String normalized = Normalizer.normalize(label, Normalizer.Form.NFD)
				.replaceAll("\\p{M}+", "");
		return normalized.toLowerCase(Locale.ROOT);
	}
}
