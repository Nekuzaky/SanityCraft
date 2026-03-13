package com.sanitycraft.client.mixin;

import com.sanitycraft.client.menu.SanityCraftMenuWidgetSkin;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSelectionList.class)
public abstract class AbstractSelectionListSkinMixin {
	@Inject(method = "renderListBackground", at = @At("HEAD"), cancellable = true)
	private void sanitycraft$renderSkinnedListBackground(GuiGraphics guiGraphics, CallbackInfo ci) {
		if (!SanityCraftMenuWidgetSkin.shouldSkinCurrentScreen()) {
			return;
		}

		AbstractWidget widget = (AbstractWidget) (Object) this;
		SanityCraftMenuWidgetSkin.renderListBackground(guiGraphics, widget);
		ci.cancel();
	}

	@Inject(method = "renderListSeparators", at = @At("HEAD"), cancellable = true)
	private void sanitycraft$skipVanillaListSeparators(GuiGraphics guiGraphics, CallbackInfo ci) {
		if (SanityCraftMenuWidgetSkin.shouldSkinCurrentScreen()) {
			ci.cancel();
		}
	}
}
