package com.sanitycraft.client.mixin;

import com.sanitycraft.client.events.SanityClientEventState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(AbstractContainerScreen.class)
abstract class AbstractContainerScreenHallucinationMixin {
	@Inject(method = "renderSlot", at = @At("TAIL"))
	private void sanitycraft$renderHallucinatedSlot(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
		SanityClientEventState.renderInventoryHallucination(guiGraphics, slot);
	}
}
