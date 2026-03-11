package net.nekuzaky.sanitycraft.mixin;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
	@Inject(method = "Lnet/minecraft/server/level/ServerPlayer;drop(Z)Z", at = @At("HEAD"), cancellable = true)
	public void drop(boolean dropStack, CallbackInfoReturnable<Boolean> cir) {
		ServerPlayer self = (ServerPlayer) (Object) this;
		Inventory inventory = self.getInventory();
		ItemStack itemstack = inventory.removeFromSelected(dropStack);
		self.containerMenu.findSlot(inventory, inventory.getSelectedSlot()).ifPresent(p_401732_ -> self.containerMenu.setRemoteSlot(p_401732_, inventory.getSelectedItem()));
		cir.setReturnValue(self.drop(itemstack, false, true) != null);
	}
}