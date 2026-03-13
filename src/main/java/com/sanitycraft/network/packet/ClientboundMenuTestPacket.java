package com.sanitycraft.network.packet;

import com.sanitycraft.SanityCraft;
import com.sanitycraft.network.sync.MenuTestType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundMenuTestPacket(int testId) implements CustomPacketPayload {
	public static final Type<ClientboundMenuTestPacket> TYPE = new Type<>(SanityCraft.id("client_menu_test"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundMenuTestPacket> CODEC =
			StreamCodec.composite(ByteBufCodecs.VAR_INT, ClientboundMenuTestPacket::testId, ClientboundMenuTestPacket::new);

	public MenuTestType test() {
		return MenuTestType.byId(testId);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
