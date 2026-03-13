package com.sanitycraft.network.packet;

import com.sanitycraft.SanityCraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundSanitySyncPacket(int sanity) implements CustomPacketPayload {
	public static final Type<ClientboundSanitySyncPacket> TYPE = new Type<>(SanityCraft.id("sanity_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSanitySyncPacket> CODEC =
			StreamCodec.composite(ByteBufCodecs.VAR_INT, ClientboundSanitySyncPacket::sanity, ClientboundSanitySyncPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
