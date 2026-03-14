package com.sanitycraft.network.packet;

import com.sanitycraft.SanityCraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundHudDistortionPacket(int durationTicks, int intensity, boolean fakeDamageFlash) implements CustomPacketPayload {
	public static final Type<ClientboundHudDistortionPacket> TYPE = new Type<>(SanityCraft.id("client_hud_distortion"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundHudDistortionPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT,
			ClientboundHudDistortionPacket::durationTicks,
			ByteBufCodecs.VAR_INT,
			ClientboundHudDistortionPacket::intensity,
			ByteBufCodecs.BOOL,
			ClientboundHudDistortionPacket::fakeDamageFlash,
			ClientboundHudDistortionPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
