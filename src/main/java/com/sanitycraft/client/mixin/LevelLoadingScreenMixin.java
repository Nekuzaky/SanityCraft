package com.sanitycraft.client.mixin;

import com.sanitycraft.client.menu.SanityCraftScreenStyling;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLoadingScreen.class)
public abstract class LevelLoadingScreenMixin {
	@Inject(method = "render", at = @At("HEAD"))
	private void sanitycraft$renderLoadingBackdrop(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		SanityCraftScreenStyling.renderLoadingBackdrop(guiGraphics, guiGraphics.guiWidth(), guiGraphics.guiHeight());
	}
}
