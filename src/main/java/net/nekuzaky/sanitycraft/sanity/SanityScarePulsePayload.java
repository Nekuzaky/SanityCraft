package net.nekuzaky.sanitycraft.sanity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.nekuzaky.sanitycraft.SanitycraftMod;

public record SanityScarePulsePayload(int durationTicks, int intensity) implements CustomPacketPayload {
	public static final Type<SanityScarePulsePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SanitycraftMod.MODID, "sanity_scare_pulse"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SanityScarePulsePayload> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, SanityScarePulsePayload::durationTicks,
			ByteBufCodecs.VAR_INT, SanityScarePulsePayload::intensity, SanityScarePulsePayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
