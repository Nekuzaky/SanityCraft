package net.nekuzaky.sanitycraft.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
	@Accessor("leftPos")
	int sanitycraft$getLeftPos();

	@Accessor("topPos")
	int sanitycraft$getTopPos();

	@Accessor("imageWidth")
	int sanitycraft$getImageWidth();

	@Accessor("imageHeight")
	int sanitycraft$getImageHeight();
}
