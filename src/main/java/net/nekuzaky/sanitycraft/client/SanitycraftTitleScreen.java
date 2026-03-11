package net.nekuzaky.sanitycraft.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;

public class SanitycraftTitleScreen extends TitleScreen {
	public SanitycraftTitleScreen() {
		super();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		MainMenuHorrorOverlay.renderOverlay(guiGraphics);
	}
}
