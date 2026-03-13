package com.sanitycraft.network.packet;

import com.sanitycraft.SanityCraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundScarePulsePacket(int durationTicks, int intensity) implements CustomPacketPayload {
	public static final Type<ClientboundScarePulsePacket> TYPE = new Type<>(SanityCraft.id("client_effect_scare_pulse"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundScarePulsePacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT,
			ClientboundScarePulsePacket::durationTicks,
			ByteBufCodecs.VAR_INT,
			ClientboundScarePulsePacket::intensity,
			ClientboundScarePulsePacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
