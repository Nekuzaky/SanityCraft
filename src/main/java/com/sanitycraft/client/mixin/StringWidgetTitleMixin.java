package com.sanitycraft.client.mixin;

import com.sanitycraft.client.menu.SanityCraftOptionsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StringWidget.class)
public abstract class StringWidgetTitleMixin {
	@Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
	private void sanitycraft$hideOptionsTitle(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		if (!(screen instanceof SanityCraftOptionsScreen)) {
			return;
		}

		StringWidget widget = (StringWidget) (Object) this;
		if (widget.getMessage().getString().equals(screen.getTitle().getString())) {
			ci.cancel();
		}
	}
}
