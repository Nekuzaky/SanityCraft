package com.sanitycraft.client.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftWindowTitleMixin {
	@Inject(method = "createTitle", at = @At("RETURN"), cancellable = true)
	private void sanitycraft$setWindowTitle(CallbackInfoReturnable<String> cir) {
		cir.setReturnValue("SanityCraft");
	}
}
