package com.sanitycraft.client.mixin;

import com.sanitycraft.client.menu.SanityCraftScreenStyling;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenBackgroundMixin {
	@Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
	private void sanitycraft$renderStyledWorldSelectionBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		Screen screen = (Screen) (Object) this;
		if (!(screen instanceof SelectWorldScreen)) {
			return;
		}

		SanityCraftScreenStyling.renderMenuBackdrop(guiGraphics, guiGraphics.guiWidth(), guiGraphics.guiHeight());
		ci.cancel();
	}
}
