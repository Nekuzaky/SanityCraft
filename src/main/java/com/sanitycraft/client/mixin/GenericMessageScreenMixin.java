package com.sanitycraft.client.mixin;

import com.sanitycraft.client.menu.SanityCraftScreenStyling;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GenericMessageScreen.class)
public abstract class GenericMessageScreenMixin {
	@Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
	private void sanitycraft$renderGenericMessageBackdrop(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		SanityCraftScreenStyling.renderLoadingBackdrop(guiGraphics, guiGraphics.guiWidth(), guiGraphics.guiHeight());
		ci.cancel();
	}
}
