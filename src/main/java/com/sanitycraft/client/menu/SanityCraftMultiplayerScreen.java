package com.sanitycraft.client.menu;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;

@Environment(EnvType.CLIENT)
public final class SanityCraftMultiplayerScreen extends JoinMultiplayerScreen {
	private SanityCraftMenuLayout.SubmenuLayout layout;

	public SanityCraftMultiplayerScreen(Screen lastScreen) {
		super(lastScreen);
	}

	@Override
	protected void init() {
		super.init();
		layout = SanityCraftMenuLayout.createSubmenuLayout(this.width, this.height, this.font.lineHeight, false);
		SanityCraftMenuLayout.reflowSubmenuWidgets(this, layout, widget -> false);
		SanityCraftMenuEffects.activate(this.minecraft);
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
}
