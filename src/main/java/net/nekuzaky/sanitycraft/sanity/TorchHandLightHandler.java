package net.nekuzaky.sanitycraft.sanity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TorchHandLightHandler {
	private TorchHandLightHandler() {
	}

	public static void tick(ServerPlayer player, SanityConfig config) {
		if (!config.torchHandLightEnabled) {
			return;
		}
		if (!isTorchInHand(player.getMainHandItem()) && !isTorchInHand(player.getOffhandItem())) {
			return;
		}
		int duration = Math.max(20, config.torchHandLightVisionDurationTicks);
		MobEffectInstance existing = player.getEffect(MobEffects.NIGHT_VISION);
		if (existing == null || existing.getDuration() < Math.max(20, duration / 2)) {
			player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, duration, 0, true, false, false));
		}
	}

	private static boolean isTorchInHand(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		return stack.is(Items.TORCH) || stack.is(Items.SOUL_TORCH) || stack.is(Items.REDSTONE_TORCH);
	}
}
