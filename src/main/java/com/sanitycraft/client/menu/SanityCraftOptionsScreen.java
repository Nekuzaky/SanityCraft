package com.sanitycraft.client.menu;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;

@Environment(EnvType.CLIENT)
public final class SanityCraftOptionsScreen extends OptionsScreen {
	private SanityCraftMenuLayout.SubmenuLayout layout;

	public SanityCraftOptionsScreen(Screen lastScreen, Options options) {
		super(lastScreen, options);
	}

	@Override
	protected void init() {
		super.init();
		applyResponsiveLayout();
		SanityCraftMenuEffects.activate(this.minecraft);
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		applyResponsiveLayout();
	}

	@Override
	public void tick() {
		super.tick();
		SanityCraftMenuEffects.tick(this.minecraft);
	}

	@Override
	public void removed() {
		SanityCraftMenuEffects.deactivate(this.minecraft);
		super.removed();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		long time = System.currentTimeMillis();
		float distortion = SanityCraftMenuEffects.getDistortionStrength();
		SanityCraftMenuAtmosphereRenderer.renderForestBackdrop(guiGraphics, this.width, this.height, time, distortion, false);
		if (layout != null) {
			SanityCraftMenuAtmosphereRenderer.renderSubmenuPanel(guiGraphics, layout, time, distortion);
		}
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		SanityCraftMenuAtmosphereRenderer.renderSubmenuAtmosphere(
				guiGraphics,
				this.width,
				this.height,
				time,
				distortion);
		if (layout != null) {
			SanityCraftMenuLayout.renderFooter(guiGraphics, this.font, layout.footer());
		}
	}

	private void applyResponsiveLayout() {
		if (this.font == null || this.width <= 0 || this.height <= 0) {
			return;
		}

		layout = SanityCraftMenuLayout.createSubmenuLayout(this.width, this.height, this.font.lineHeight, false);
		SanityCraftMenuLayout.reflowSubmenuWidgets(this, layout, widget -> widget instanceof StringWidget);
		nudgeTopControlRow();
	}

	private void nudgeTopControlRow() {
		if (layout == null) {
			return;
		}

		int minimumTop = Math.max(42, Math.min(60, Math.round(this.height * 0.052F)));
		int rowCutoff = layout.contentTop() - 12;
		List<AbstractWidget> topWidgets = new ArrayList<>();
		int topY = Integer.MAX_VALUE;

		for (var listener : this.children()) {
			if (!(listener instanceof AbstractWidget widget) || !widget.visible || widget instanceof StringWidget) {
				continue;
			}

			if (widget.getY() >= rowCutoff) {
				continue;
			}

			topWidgets.add(widget);
			topY = Math.min(topY, widget.getY());
		}

		if (topWidgets.isEmpty() || topY >= minimumTop) {
			return;
		}

		int deltaY = minimumTop - topY;
		for (AbstractWidget widget : topWidgets) {
			widget.setY(widget.getY() + deltaY);
		}
	}
}
