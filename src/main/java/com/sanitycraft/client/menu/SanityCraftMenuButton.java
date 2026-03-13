package com.sanitycraft.client.menu;

import com.sanitycraft.SanityCraft;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public final class SanityCraftMenuButton extends Button {
	private static final ResourceLocation BUTTON_TEXTURE = SanityCraft.id("textures/gui/menu/main_menue_button.png");
	private static final int TEXTURE_WIDTH = 622;
	private static final int TEXTURE_HEIGHT = 117;

	private final Accent accent;
	private boolean hoveredLastFrame;

	public SanityCraftMenuButton(int x, int y, int width, int height, Component message, OnPress onPress) {
		this(x, y, width, height, message, Accent.STANDARD, onPress);
	}

	public SanityCraftMenuButton(int x, int y, int width, int height, Component message, Accent accent, OnPress onPress) {
		super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
		this.accent = accent;
	}

	@Override
	protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		boolean hovered = isHoveredOrFocused();
		maybePlayHoverCue(hovered);
		renderButton(guiGraphics, getX(), getY(), getWidth(), getHeight(), getMessage(), this.accent, hovered, isActive(), 1.0F);
		hoveredLastFrame = hovered;
	}

	@Override
	public void playDownSound(net.minecraft.client.sounds.SoundManager soundManager) {
		soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), getAccentPitch(this.accent), 0.25F));
	}

	public static void renderSkinned(
			GuiGraphics guiGraphics,
			int x,
			int y,
			int width,
			int height,
			Component message,
			Accent accent,
			boolean hovered,
			boolean active,
			float alpha) {
		renderButton(guiGraphics, x, y, width, height, message, accent, hovered, active, alpha);
	}

	static void renderDecorative(GuiGraphics guiGraphics, int x, int y, int width, int height, Component message, Accent accent, boolean hovered, float alpha) {
		renderSkinned(guiGraphics, x, y, width, height, message, accent, hovered, true, alpha);
	}

	private void maybePlayHoverCue(boolean hovered) {
		if (!hovered || hoveredLastFrame || !this.active || !this.visible) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.getSoundManager() == null) {
			return;
		}

		float pitch = getAccentPitch(this.accent) + (this.accent == Accent.PRIMARY ? -0.08F : 0.04F);
		float volume = this.accent == Accent.PRIMARY ? 0.055F : 0.042F;
		minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), pitch, volume));
	}

	private static void renderButton(
			GuiGraphics guiGraphics,
			int x,
			int y,
			int width,
			int height,
			Component message,
			Accent accent,
			boolean hovered,
			boolean active,
			float alpha) {
		long time = System.currentTimeMillis();
		float hoverPulse = hovered ? 0.62F + (float) ((Math.sin((time + x * 17L) / 160.0D) + 1.0D) * 0.09D) : 0.0F;
		int depthLift = hovered ? 1 : 0;
		int frameAlpha = Math.max(0, Math.min(255, Math.round(alpha * 255.0F)));
		ButtonPalette palette = ButtonPalette.forAccent(accent, hovered, active);
		int jitterX = hovered ? (int) (Math.sin((time + x * 13L) / 120.0D) * 0.9D) : 0;
		int jitterY = hovered ? (int) (Math.cos((time + y * 9L) / 145.0D) * 0.7D) : 0;
		int shadowSpread = hovered ? 6 : 4;
		int top = y - depthLift;
		int bottom = top + height;

		guiGraphics.fill(
				x - shadowSpread,
				top - 2,
				x + width + shadowSpread,
				bottom + shadowSpread + 3,
				applyAlpha(adjustAlpha(palette.shadowColor(), hovered ? 1.15F : 1.0F), frameAlpha));
		guiGraphics.fill(x - 2, top - 2, x + width + 2, bottom + 2, applyAlpha(palette.outerFrameColor(), frameAlpha));
		guiGraphics.fill(x - 1, top - 1, x + width + 1, bottom + 1, applyAlpha(palette.innerFrameColor(), frameAlpha));

		guiGraphics.blit(
				RenderPipelines.GUI_TEXTURED,
				BUTTON_TEXTURE,
				x + jitterX,
				top + jitterY,
				0.0F,
				0.0F,
				width,
				height,
				TEXTURE_WIDTH,
				TEXTURE_HEIGHT,
				TEXTURE_WIDTH,
				TEXTURE_HEIGHT);

		guiGraphics.fill(x + 5, top + 5, x + width - 5, bottom - 5, applyAlpha(palette.innerVeilColor(), frameAlpha));
		guiGraphics.fill(x + 8, top + 4, x + width - 8, top + Math.max(8, height / 2), applyAlpha(palette.sheenColor(), frameAlpha));
		guiGraphics.fill(x + 8, bottom - Math.max(5, height / 4), x + width - 8, bottom - 4, applyAlpha(palette.lowerTintColor(), frameAlpha));

		if (!active) {
			guiGraphics.fill(x, top, x + width, bottom, applyAlpha(0x8A060606, frameAlpha));
		}

		if (hovered) {
			int hoverAlpha = Math.max(0, Math.min(255, Math.round((66.0F + hoverPulse * 92.0F) * alpha)));
			guiGraphics.renderOutline(x - 1, top - 1, width + 2, height + 2, applyAlpha((hoverAlpha << 24) | (palette.hoverOutlineColor() & 0x00FFFFFF), 255));
			guiGraphics.fill(x + 10, top + 2, x + width - 10, top + 4, applyAlpha(adjustAlpha(palette.sheenColor(), 1.35F), frameAlpha));
		}

		int textShadow = applyAlpha(0x76000000, frameAlpha);
		int textColor = applyAlpha(palette.textColor(), frameAlpha);
		int textY = top + (height - 8) / 2;
		Minecraft minecraft = Minecraft.getInstance();
		guiGraphics.drawCenteredString(minecraft.font, message, x + width / 2 + (hovered ? 1 : 0), textY + 1, textShadow);
		guiGraphics.drawCenteredString(minecraft.font, message, x + width / 2, textY, textColor);
	}

	private static float getAccentPitch(Accent accent) {
		return switch (accent) {
			case PRIMARY -> 0.94F;
			case COMMUNITY -> 1.04F;
			case EXIT -> 0.88F;
			case PHANTOM -> 0.90F;
			case STANDARD -> 0.98F;
		};
	}

	private static int applyAlpha(int color, int alpha) {
		int rgb = color & 0x00FFFFFF;
		int sourceAlpha = color >>> 24;
		int scaledAlpha = sourceAlpha * alpha / 255;
		return (scaledAlpha << 24) | rgb;
	}

	private static int adjustAlpha(int color, float multiplier) {
		int rgb = color & 0x00FFFFFF;
		int sourceAlpha = color >>> 24;
		int adjustedAlpha = Mth.clamp(Math.round(sourceAlpha * multiplier), 0, 255);
		return (adjustedAlpha << 24) | rgb;
	}

	public enum Accent {
		PRIMARY,
		STANDARD,
		COMMUNITY,
		EXIT,
		PHANTOM
	}

	private record ButtonPalette(
			int shadowColor,
			int outerFrameColor,
			int innerFrameColor,
			int innerVeilColor,
			int sheenColor,
			int lowerTintColor,
			int hoverOutlineColor,
			int textColor) {
		private static ButtonPalette forAccent(Accent accent, boolean hovered, boolean active) {
			if (!active) {
				return new ButtonPalette(0x44000000, 0x30110808, 0x1E0D0707, 0x220A0505, 0x1A231010, 0x260E0505, 0x72544B4B, 0xFF8E7470);
			}

			return switch (accent) {
				case PRIMARY -> new ButtonPalette(
						hovered ? 0x6A160707 : 0x54100505,
						hovered ? 0x8A381111 : 0x70301111,
						hovered ? 0x65341818 : 0x50311616,
						hovered ? 0x2F170B0B : 0x25100606,
						hovered ? 0x302B1412 : 0x221B0E0C,
						hovered ? 0x401A0808 : 0x34140505,
						0xE0D98B82,
						hovered ? 0xFFF6EBE4 : 0xFFE2C0B4);
				case COMMUNITY -> new ButtonPalette(
						hovered ? 0x58110E16 : 0x42100C14,
						hovered ? 0x7A2B2936 : 0x60303236,
						hovered ? 0x5A2D2C38 : 0x4931303C,
						hovered ? 0x26101114 : 0x1F0E0E12,
						hovered ? 0x24222D33 : 0x1B1A2329,
						hovered ? 0x28111316 : 0x21101215,
						0xD0AAB7D8,
						hovered ? 0xFFF0EEF6 : 0xFFD6CCDE);
				case EXIT -> new ButtonPalette(
						hovered ? 0x62110505 : 0x4F120707,
						hovered ? 0x88341515 : 0x70311818,
						hovered ? 0x652B1717 : 0x56301919,
						hovered ? 0x2C120909 : 0x240E0606,
						hovered ? 0x241D1212 : 0x1C170D0D,
						hovered ? 0x46130808 : 0x38110606,
						0xD8D18282,
						hovered ? 0xFFF2E6E2 : 0xFFD7BBB2);
				case PHANTOM -> new ButtonPalette(
						hovered ? 0x50121012 : 0x40111011,
						hovered ? 0x7A474A56 : 0x64363842,
						hovered ? 0x5C3E434D : 0x4A32353D,
						hovered ? 0x26121416 : 0x1F0F1012,
						hovered ? 0x22262E34 : 0x1A20262C,
						hovered ? 0x24111415 : 0x1E101112,
						0xD2D8D8E6,
						hovered ? 0xFFF1F1F6 : 0xFFD6D0D8);
				case STANDARD -> new ButtonPalette(
						hovered ? 0x5A170707 : 0x460F0505,
						hovered ? 0x7E322121 : 0x68301818,
						hovered ? 0x602E1A1A : 0x4E2D1919,
						hovered ? 0x29130A0A : 0x21100707,
						hovered ? 0x261E1211 : 0x1D170E0D,
						hovered ? 0x35130707 : 0x2A100505,
						0xD8BF8A82,
						hovered ? 0xFFF1E6DF : 0xFFD9B9AE);
			};
		}
	}
}
