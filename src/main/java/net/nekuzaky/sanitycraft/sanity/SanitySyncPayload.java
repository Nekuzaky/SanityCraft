package net.nekuzaky.sanitycraft.sanity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.nekuzaky.sanitycraft.SanitycraftMod;

public record SanitySyncPayload(int sanity) implements CustomPacketPayload {
	public static final Type<SanitySyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SanitycraftMod.MODID, "sanity_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SanitySyncPayload> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, SanitySyncPayload::sanity, SanitySyncPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
