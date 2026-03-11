package net.nekuzaky.sanitycraft.sanity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.nekuzaky.sanitycraft.SanitycraftMod;

public record SanityJumpscarePayload(int variant, int durationTicks) implements CustomPacketPayload {
	public static final Type<SanityJumpscarePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SanitycraftMod.MODID, "sanity_jumpscare"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SanityJumpscarePayload> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, SanityJumpscarePayload::variant,
			ByteBufCodecs.VAR_INT, SanityJumpscarePayload::durationTicks, SanityJumpscarePayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
