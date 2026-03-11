package net.nekuzaky.sanitycraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public abstract class MinecraftTitleMixin {
	@Inject(method = "createTitle", at = @At("RETURN"), cancellable = true, require = 0)
	private void sanitycraft$appendWindowTitle(CallbackInfoReturnable<String> cir) {
		cir.setReturnValue(cir.getReturnValue() + " | SanityCraft");
	}
}
