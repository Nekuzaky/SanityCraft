package net.nekuzaky.sanitycraft.mixin;

import net.nekuzaky.sanitycraft.client.HorrorUiOverlays;
import net.nekuzaky.sanitycraft.client.SanityClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
	@Inject(method = "render", at = @At("TAIL"), require = 0)
	private void sanitycraft$renderGlobalContainerHorrorOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) (Object) this;
		Font font = Minecraft.getInstance().font;
		HorrorUiOverlays.renderInventoryHorrorSkin(guiGraphics, accessor.sanitycraft$getLeftPos(), accessor.sanitycraft$getTopPos(), accessor.sanitycraft$getImageWidth(),
				accessor.sanitycraft$getImageHeight(), SanityClientState.getSanity(), font);
	}
}
