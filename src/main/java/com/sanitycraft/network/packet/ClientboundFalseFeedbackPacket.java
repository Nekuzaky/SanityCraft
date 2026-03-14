package com.sanitycraft.network.packet;

import com.sanitycraft.SanityCraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundFalseFeedbackPacket(String message) implements CustomPacketPayload {
	public static final Type<ClientboundFalseFeedbackPacket> TYPE = new Type<>(SanityCraft.id("client_false_feedback"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundFalseFeedbackPacket> CODEC =
			StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ClientboundFalseFeedbackPacket::message, ClientboundFalseFeedbackPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
