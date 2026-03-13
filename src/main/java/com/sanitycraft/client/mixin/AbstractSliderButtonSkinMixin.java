package com.sanitycraft.client.mixin;

import com.sanitycraft.client.menu.SanityCraftMenuWidgetSkin;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSliderButton.class)
public abstract class AbstractSliderButtonSkinMixin {
	@Shadow
	protected double value;

	@Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
	private void sanitycraft$renderSkinnedSlider(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		if (!SanityCraftMenuWidgetSkin.shouldSkinCurrentScreen()) {
			return;
		}

		AbstractWidget widget = (AbstractWidget) (Object) this;
		SanityCraftMenuWidgetSkin.renderSliderWidget(guiGraphics, widget, this.value);
		ci.cancel();
	}
}
