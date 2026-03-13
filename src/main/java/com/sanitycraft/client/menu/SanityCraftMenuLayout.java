package com.sanitycraft.client.menu;

import com.sanitycraft.SanityCraft;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public final class SanityCraftMenuLayout {
	static final ResourceLocation TITLE_TEXTURE = SanityCraft.id("textures/gui/menu/title_sanitycraft.png");
	static final int TITLE_TEXTURE_WIDTH = 777;
	static final int TITLE_TEXTURE_HEIGHT = 200;

	private static final float BASE_WIDTH = 1920.0F;
	private static final float BASE_HEIGHT = 1080.0F;
	private static final float LOGO_ASPECT = TITLE_TEXTURE_WIDTH / (float) TITLE_TEXTURE_HEIGHT;

	private SanityCraftMenuLayout() {
	}

	public static TitleLayout createTitleLayout(int width, int height, int buttonCount, int fontLineHeight) {
		float scale = viewportScale(width, height, 0.50F, 1.36F);
		float compact = Mth.clamp((360.0F - height) / 140.0F, 0.0F, 1.0F);
		int sideInset = clamp(Math.round(Mth.lerp(compact, width * 0.050F, width * 0.040F)), 18, 180);
		int topInset = clamp(Math.round(Mth.lerp(compact, 18.0F * scale + height * 0.014F, 8.0F)), 8, 84);
		int subtitleGap = clamp(Math.round(Mth.lerp(compact, 16.0F * scale, 5.0F)), 5, 24);
		int subtitleToButtonsGap = clamp(Math.round(Mth.lerp(compact, 18.0F * scale, 7.0F)), 7, 28);
		int buttonBottomGap = clamp(Math.round(Mth.lerp(compact, height * 0.030F, 8.0F)), 8, 34);
		int availableWidth = Math.max(120, width - sideInset * 2);
		int buttonWidth = Math.min(availableWidth, clamp(Math.round(Mth.lerp(compact, 208.0F * scale + width * 0.010F, 176.0F)), 160, 292));
		int buttonHeight = clamp(Math.round(Mth.lerp(compact, 27.0F * scale, 18.0F)), 18, 38);
		int buttonSpacing = clamp(Math.round(Mth.lerp(compact, 14.0F * scale, 6.0F)), 6, 20);
		int secondaryRowGap = clamp(Math.round(Mth.lerp(compact, 12.0F * scale, 6.0F)), 6, 16);
		int secondaryButtonGap = clamp(Math.round(Mth.lerp(compact, 10.0F * scale, 6.0F)), 6, 14);
		int secondaryButtonMaxWidth = Math.max(84, (buttonWidth - secondaryButtonGap) / 2);
		int secondaryButtonWidth = Math.min(secondaryButtonMaxWidth, Math.max(84, Math.round(buttonWidth * Mth.lerp(compact, 0.46F, 0.44F))));
		int secondaryButtonHeight = clamp(Math.round(Mth.lerp(compact, 22.0F * scale, 20.0F)), 20, buttonHeight);
		FooterLayout footer = createFooterLayout(width, height, fontLineHeight, scale, compact);
		int primaryStackHeight = buttonCount * buttonHeight + Math.max(0, buttonCount - 1) * buttonSpacing;
		int actionHeight = primaryStackHeight + secondaryRowGap + secondaryButtonHeight;
		int maxButtonY = height - footer.reservedHeight() - buttonBottomGap - actionHeight;
		int preferredTitleWidth = clamp(Math.round(Mth.lerp(compact, 392.0F * (0.92F + scale * 0.28F), 216.0F + width * 0.06F)), 160, 560);
		int availableTitleHeight = Math.max(1, maxButtonY - topInset - subtitleGap - subtitleToButtonsGap - fontLineHeight);
		int maxTitleWidth = Math.max(1, Math.min(availableWidth, Math.round(availableTitleHeight * LOGO_ASPECT)));
		int minTitleWidth = Math.min(clamp(Math.round(Mth.lerp(compact, 208.0F * scale, 136.0F)), 120, 220), maxTitleWidth);
		int titleWidth = clamp(preferredTitleWidth, minTitleWidth, maxTitleWidth);
		int titleHeight = Math.max(1, Math.round(titleWidth / LOGO_ASPECT));

		int titleX = (width - titleWidth) / 2;
		int titleY = topInset;
		int subtitleY = titleY + titleHeight + subtitleGap;
		int minButtonY = subtitleY + fontLineHeight + subtitleToButtonsGap;
		int preferredButtonY = Math.round(Mth.lerp(compact, height * 0.57F, height * 0.46F));
		int buttonStartY = maxButtonY >= minButtonY ? clamp(preferredButtonY, minButtonY, maxButtonY) : maxButtonY;
		int secondaryButtonY = buttonStartY + primaryStackHeight + secondaryRowGap;

		return new TitleLayout(
				width,
				height,
				scale,
				titleX,
				titleY,
				titleWidth,
				titleHeight,
				subtitleY,
				(width - buttonWidth) / 2,
				buttonStartY,
				buttonWidth,
				buttonHeight,
				buttonSpacing,
				secondaryButtonWidth,
				secondaryButtonHeight,
				secondaryButtonGap,
				secondaryButtonY,
				footer);
	}

	public static SubmenuLayout createSubmenuLayout(int width, int height, int fontLineHeight) {
		float scale = viewportScale(width, height, 0.54F, 0.96F);
		int sideInset = clamp(Math.round(width * 0.045F), 20, 120);
		int headerTop = clamp(Math.round(height * 0.020F), 8, 22);
		int subtitleGap = clamp(Math.round(6.0F * scale), 4, 9);
		int subtitleToContentGap = clamp(Math.round(12.0F * scale), 8, 16);
		int logoWidth = Math.min(width - sideInset * 2, clamp(Math.round(212.0F * (0.84F + scale * 0.16F)), 156, 262));
		int logoHeight = Math.max(1, Math.round(logoWidth / LOGO_ASPECT));
		int logoX = (width - logoWidth) / 2;
		int subtitleY = headerTop + logoHeight + subtitleGap;
		FooterLayout footer = createFooterLayout(width, height, fontLineHeight, scale, 0.0F);
		int contentTop = subtitleY + fontLineHeight + subtitleToContentGap;
		int contentBottom = height - footer.reservedHeight() - clamp(Math.round(height * 0.022F), 12, 24);
		int panelWidth = Math.min(width - sideInset * 2, clamp(Math.round(width * 0.66F), 620, 1180));
		int contentLeft = (width - panelWidth) / 2;
		int sectionGap = clamp(Math.round(12.0F * scale), 8, 16);
		int minimumListHeight = clamp(Math.round(height * 0.20F), 126, 220);

		return new SubmenuLayout(
				width,
				height,
				scale,
				logoX,
				headerTop,
				logoWidth,
				logoHeight,
				subtitleY,
				contentLeft,
				contentTop,
				panelWidth,
				Math.max(contentTop + minimumListHeight + sectionGap, contentBottom),
				sectionGap,
				minimumListHeight,
				footer);
	}

	public static void reflowSubmenuWidgets(Screen screen, SubmenuLayout layout, Predicate<AbstractWidget> hiddenWidgetPredicate) {
		List<AbstractWidget> widgets = new ArrayList<>();
		AbstractSelectionList<?> selectionList = null;

		for (GuiEventListener listener : screen.children()) {
			if (!(listener instanceof AbstractWidget widget)) {
				continue;
			}

			if (hiddenWidgetPredicate.test(widget)) {
				widget.visible = false;
				widget.active = false;
				continue;
			}

			if (!widget.visible) {
				continue;
			}

			if (widget instanceof AbstractSelectionList<?> list) {
				selectionList = list;
				continue;
			}

			widgets.add(widget);
		}

		int contentCenterX = layout.contentLeft() + layout.contentWidth() / 2;

		if (selectionList != null) {
			WidgetBounds actionBounds = measureBounds(widgets);
			int actionHeight = actionBounds.height();
			int actionTop = clamp(
					layout.contentBottom() - actionHeight,
					layout.contentTop() + layout.minimumListHeight() + layout.sectionGap(),
					layout.contentBottom() - actionHeight);
			translateWidgets(widgets, contentCenterX - actionBounds.centerX(), actionTop - actionBounds.minY());

			int listBottom = Math.max(layout.contentTop() + layout.minimumListHeight(), actionTop - layout.sectionGap());
			int listHeight = Math.max(layout.minimumListHeight(), listBottom - layout.contentTop());
			selectionList.updateSizeAndPosition(layout.contentWidth(), listHeight, layout.contentTop());
			selectionList.setX(layout.contentLeft());
			return;
		}

		WidgetBounds bounds = measureBounds(widgets);
		int maxTop = Math.max(layout.contentTop(), layout.contentBottom() - bounds.height());
		int targetTop = clamp(bounds.minY(), layout.contentTop(), maxTop);
		translateWidgets(widgets, contentCenterX - bounds.centerX(), targetTop - bounds.minY());

		WidgetBounds shiftedBounds = measureBounds(widgets);
		if (shiftedBounds.maxY() > layout.contentBottom()) {
			translateWidgets(widgets, 0, layout.contentBottom() - shiftedBounds.maxY());
		}
	}

	public static void renderLogo(GuiGraphics guiGraphics, int x, int y, int width, int height) {
		guiGraphics.blit(
				RenderPipelines.GUI_TEXTURED,
				TITLE_TEXTURE,
				x,
				y,
				0.0F,
				0.0F,
				width,
				height,
				TITLE_TEXTURE_WIDTH,
				TITLE_TEXTURE_HEIGHT,
				TITLE_TEXTURE_WIDTH,
				TITLE_TEXTURE_HEIGHT);
	}

	public static void renderSubmenuHeader(GuiGraphics guiGraphics, Font font, SubmenuLayout layout, Component subtitle) {
		int outerPadX = clamp(Math.round(layout.logoWidth() * 0.055F), 8, 16);
		int outerPadY = clamp(Math.round(layout.logoHeight() * 0.12F), 4, 10);
		int innerPadX = clamp(Math.round(layout.logoWidth() * 0.020F), 4, 8);
		int innerPadY = clamp(Math.round(layout.logoHeight() * 0.045F), 2, 6);
		int subtitleRuleWidth = clamp(Math.round(layout.logoWidth() * 0.26F), 56, 100);
		int subtitleRuleY = layout.subtitleY() + font.lineHeight + 4;

		guiGraphics.fill(0, 0, layout.screenWidth(), layout.contentTop() - layout.sectionGap() / 2, 0x0F000000);
		guiGraphics.fill(
				layout.logoX() - outerPadX,
				layout.logoY() - outerPadY,
				layout.logoX() + layout.logoWidth() + outerPadX,
				layout.subtitleY() + font.lineHeight + outerPadY,
				0x54100000);
		guiGraphics.fill(
				layout.logoX() - innerPadX,
				layout.logoY() - innerPadY,
				layout.logoX() + layout.logoWidth() + innerPadX,
				layout.logoY() + layout.logoHeight() + innerPadY,
				0x18000000);
		renderLogo(guiGraphics, layout.logoX(), layout.logoY(), layout.logoWidth(), layout.logoHeight());
		guiGraphics.drawCenteredString(font, subtitle, layout.screenWidth() / 2, layout.subtitleY(), 0xFFC49E9D);
		guiGraphics.fill(layout.screenWidth() / 2 - subtitleRuleWidth / 2, subtitleRuleY, layout.screenWidth() / 2 + subtitleRuleWidth / 2, subtitleRuleY + 1, 0x52886A6A);
		guiGraphics.fill(layout.screenWidth() / 2 - subtitleRuleWidth / 4, subtitleRuleY + 1, layout.screenWidth() / 2 + subtitleRuleWidth / 4, subtitleRuleY + 2, 0x28190A0A);
	}

	public static void renderFooter(GuiGraphics guiGraphics, Font font, FooterLayout footer) {
		Component warningLabel = Component.translatable("sanitycraft.menu.footer.warning");
		Component versionLabel = Component.translatable("sanitycraft.menu.footer.version", SharedConstants.getCurrentVersion().name());
		int labelWidth = Math.max(font.width(warningLabel), font.width(versionLabel));
		int right = footer.leftX() + labelWidth + footer.backgroundPaddingX();
		int bottom = footer.versionY() + font.lineHeight + footer.backgroundPaddingY();

		guiGraphics.fill(
				footer.leftX() - footer.backgroundPaddingX(),
				footer.warningY() - footer.backgroundPaddingY(),
				right,
				bottom,
				0x4C100505);
		guiGraphics.fill(
				footer.leftX() - footer.backgroundPaddingX() / 2,
				footer.warningY() - Math.max(3, footer.backgroundPaddingY() / 2),
				right,
				bottom,
				0x16000000);
		guiGraphics.fill(footer.leftX() - footer.backgroundPaddingX(), footer.warningY() - 1, right, footer.warningY(), 0x2C4A1919);
		guiGraphics.drawString(font, warningLabel, footer.leftX(), footer.warningY(), 0x9CB18383, false);
		guiGraphics.drawString(font, versionLabel, footer.leftX(), footer.versionY(), 0xA8C7B5B5, false);
	}

	private static FooterLayout createFooterLayout(int width, int height, int fontLineHeight, float scale, float compact) {
		int leftX = clamp(Math.round(width * 0.010F), 10, 30);
		int bottomPadding = clamp(Math.round(Mth.lerp(compact, height * 0.018F, 8.0F)), 8, 28);
		int lineGap = clamp(Math.round(Mth.lerp(compact, 3.0F + scale * 2.0F, 2.0F)), 2, 5);
		int versionY = height - bottomPadding - fontLineHeight;
		int warningY = versionY - fontLineHeight - lineGap;
		int backgroundPaddingX = clamp(Math.round(Mth.lerp(compact, 8.0F * scale, 5.0F)), 4, 12);
		int backgroundPaddingY = clamp(Math.round(Mth.lerp(compact, 6.0F * scale, 3.0F)), 3, 8);
		int reservedHeight = bottomPadding + fontLineHeight * 2 + lineGap + backgroundPaddingY * 2;

		return new FooterLayout(leftX, warningY, versionY, reservedHeight, backgroundPaddingX, backgroundPaddingY);
	}

	private static WidgetBounds measureBounds(List<AbstractWidget> widgets) {
		if (widgets.isEmpty()) {
			return new WidgetBounds(0, 0, 0, 0);
		}

		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;

		for (AbstractWidget widget : widgets) {
			minX = Math.min(minX, widget.getX());
			maxX = Math.max(maxX, widget.getRight());
			minY = Math.min(minY, widget.getY());
			maxY = Math.max(maxY, widget.getBottom());
		}

		return new WidgetBounds(minX, maxX, minY, maxY);
	}

	private static void translateWidgets(List<AbstractWidget> widgets, int deltaX, int deltaY) {
		if (deltaX == 0 && deltaY == 0) {
			return;
		}

		for (AbstractWidget widget : widgets) {
			widget.setX(widget.getX() + deltaX);
			widget.setY(widget.getY() + deltaY);
		}
	}

	private static float viewportScale(int width, int height, float minScale, float maxScale) {
		float widthScale = width / BASE_WIDTH;
		float heightScale = height / BASE_HEIGHT;
		return Mth.clamp(Math.min(widthScale, heightScale), minScale, maxScale);
	}

	private static int clamp(int value, int minValue, int maxValue) {
		if (minValue > maxValue) {
			return minValue;
		}
		return Mth.clamp(value, minValue, maxValue);
	}

	public record TitleLayout(
			int screenWidth,
			int screenHeight,
			float scale,
			int titleX,
			int titleY,
			int titleWidth,
			int titleHeight,
			int subtitleY,
			int buttonStartX,
			int buttonStartY,
			int buttonWidth,
			int buttonHeight,
			int buttonSpacing,
			int secondaryButtonWidth,
			int secondaryButtonHeight,
			int secondaryButtonGap,
			int secondaryButtonY,
			FooterLayout footer) {
	}

	public record SubmenuLayout(
			int screenWidth,
			int screenHeight,
			float scale,
			int logoX,
			int logoY,
			int logoWidth,
			int logoHeight,
			int subtitleY,
			int contentLeft,
			int contentTop,
			int contentWidth,
			int contentBottom,
			int sectionGap,
			int minimumListHeight,
			FooterLayout footer) {
	}

	public record FooterLayout(
			int leftX,
			int warningY,
			int versionY,
			int reservedHeight,
			int backgroundPaddingX,
			int backgroundPaddingY) {
	}

	private record WidgetBounds(int minX, int maxX, int minY, int maxY) {
		private int centerX() {
			return (this.minX + this.maxX) / 2;
		}

		private int height() {
			return this.maxY - this.minY;
		}
	}
}
