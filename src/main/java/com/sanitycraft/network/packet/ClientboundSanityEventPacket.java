package com.sanitycraft.network.packet;

import com.sanitycraft.SanityCraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundSanityEventPacket(
		String eventId,
		int x,
		int y,
		int z,
		int durationTicks,
		int intensity,
		int variant,
		String text) implements CustomPacketPayload {
	public static final Type<ClientboundSanityEventPacket> TYPE = new Type<>(SanityCraft.id("client_sanity_event"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSanityEventPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8,
			ClientboundSanityEventPacket::eventId,
			ByteBufCodecs.VAR_INT,
			ClientboundSanityEventPacket::x,
			ByteBufCodecs.VAR_INT,
			ClientboundSanityEventPacket::y,
			ByteBufCodecs.VAR_INT,
			ClientboundSanityEventPacket::z,
			ByteBufCodecs.VAR_INT,
			ClientboundSanityEventPacket::durationTicks,
			ByteBufCodecs.VAR_INT,
			ClientboundSanityEventPacket::intensity,
			ByteBufCodecs.VAR_INT,
			ClientboundSanityEventPacket::variant,
			ByteBufCodecs.STRING_UTF8,
			ClientboundSanityEventPacket::text,
			ClientboundSanityEventPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
