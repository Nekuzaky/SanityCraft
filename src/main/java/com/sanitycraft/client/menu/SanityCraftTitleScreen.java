package com.sanitycraft.client.menu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public final class SanityCraftTitleScreen extends Screen {
	private static final int PRIMARY_BUTTON_COUNT = 3;
	private static final String DISCORD_URL = "https://discord.gg/7ysG9h5yh2";
	private SanityCraftMenuButton playButton;
	private SanityCraftMenuLayout.TitleLayout layout;

	public SanityCraftTitleScreen() {
		super(Component.translatable("sanitycraft.menu.title"));
	}

	@Override
	protected void init() {
		clearWidgets();
		layoutElements();
		SanityCraftMenuEffects.activate(this.minecraft);
		int secondaryLeftX = this.width / 2 - layout.secondaryButtonGap() / 2 - layout.secondaryButtonWidth();
		int secondaryRightX = this.width / 2 + layout.secondaryButtonGap() / 2;

		playButton = addRenderableWidget(new SanityCraftMenuButton(
				layout.buttonStartX(),
				layout.buttonStartY(),
				layout.buttonWidth(),
				layout.buttonHeight(),
				SanityCraftMenuEffects.getPlayLabel(),
				SanityCraftMenuButton.Accent.PRIMARY,
				button -> this.minecraft.setScreen(new SelectWorldScreen(this))));
		addRenderableWidget(new SanityCraftMenuButton(
				layout.buttonStartX(),
				layout.buttonStartY() + (layout.buttonHeight() + layout.buttonSpacing()),
				layout.buttonWidth(),
				layout.buttonHeight(),
				Component.translatable("menu.multiplayer"),
				SanityCraftMenuButton.Accent.STANDARD,
				button -> this.minecraft.setScreen(new SanityCraftMultiplayerScreen(this))));
		addRenderableWidget(new SanityCraftMenuButton(
				layout.buttonStartX(),
				layout.buttonStartY() + (layout.buttonHeight() + layout.buttonSpacing()) * 2,
				layout.buttonWidth(),
				layout.buttonHeight(),
				Component.translatable("menu.options"),
				SanityCraftMenuButton.Accent.STANDARD,
				button -> this.minecraft.setScreen(new SanityCraftOptionsScreen(this, this.minecraft.options))));
		addRenderableWidget(new SanityCraftMenuButton(
				secondaryLeftX,
				layout.secondaryButtonY(),
				layout.secondaryButtonWidth(),
				layout.secondaryButtonHeight(),
				Component.translatable("sanitycraft.menu.discord"),
				SanityCraftMenuButton.Accent.COMMUNITY,
				button -> Util.getPlatform().openUri(DISCORD_URL)));
		addRenderableWidget(new SanityCraftMenuButton(
				secondaryRightX,
				layout.secondaryButtonY(),
				layout.secondaryButtonWidth(),
				layout.secondaryButtonHeight(),
				Component.translatable("sanitycraft.menu.quit_short"),
				SanityCraftMenuButton.Accent.EXIT,
				button -> this.minecraft.stop()));
	}

	@Override
	public void tick() {
		super.tick();
		SanityCraftMenuEffects.tick(this.minecraft);
		if (playButton != null) {
			playButton.setMessage(SanityCraftMenuEffects.getPlayLabel());
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		long time = System.currentTimeMillis();
		float distortion = SanityCraftMenuEffects.getDistortionStrength();
		renderBackground(guiGraphics, mouseX, mouseY, partialTick);
		renderAtmosphere(guiGraphics, time, distortion);
		renderTitle(guiGraphics, time);
		SanityCraftMenuAtmosphereRenderer.renderTitleStage(guiGraphics, layout, PRIMARY_BUTTON_COUNT, time, distortion);
		if (playButton != null && playButton.isMouseOver(mouseX, mouseY)) {
			SanityCraftMenuEffects.normalizePlayLabel();
			playButton.setMessage(SanityCraftMenuEffects.getPlayLabel());
		}
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		renderPhantomButton(guiGraphics, mouseX, mouseY);
		renderFooter(guiGraphics);
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		long time = System.currentTimeMillis();
		float distortion = SanityCraftMenuEffects.getDistortionStrength();
		SanityCraftMenuAtmosphereRenderer.renderForestBackdrop(guiGraphics, this.width, this.height, time, distortion, true);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public void removed() {
		SanityCraftMenuEffects.deactivate(this.minecraft);
		super.removed();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && SanityCraftMenuEffects.hasPhantomButton() && isMouseOverPhantomButton(mouseX, mouseY)) {
			SanityCraftMenuEffects.dismissPhantomButton();
			this.minecraft.setScreen(new SelectWorldScreen(this));
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private void layoutElements() {
		layout = SanityCraftMenuLayout.createTitleLayout(this.width, this.height, PRIMARY_BUTTON_COUNT, this.font.lineHeight);
	}

	private void renderTitle(GuiGraphics guiGraphics, long time) {
		int flicker = (int) ((Math.sin(time / 95.0D) + 1.0D) * 6.0D);
		float logoBrightness = SanityCraftMenuEffects.getLogoBrightness();
		int outerPadX = Mth.clamp(Math.round(layout.titleWidth() * 0.08F), 10, 46);
		int outerPadY = Mth.clamp(Math.round(layout.titleHeight() * 0.16F), 6, 22);
		int innerPadX = Mth.clamp(Math.round(layout.titleWidth() * 0.03F), 4, 18);
		int innerPadY = Mth.clamp(Math.round(layout.titleHeight() * 0.05F), 2, 10);
		guiGraphics.fill(
				layout.titleX() - outerPadX,
				layout.titleY() - outerPadY,
				layout.titleX() + layout.titleWidth() + outerPadX,
				layout.titleY() + layout.titleHeight() + outerPadY,
				((34 + flicker) << 24) | 0x0C0000);
		guiGraphics.fill(
				layout.titleX() - innerPadX,
				layout.titleY() - innerPadY,
				layout.titleX() + layout.titleWidth() + innerPadX,
				layout.titleY() + layout.titleHeight() + innerPadY,
				0x18000000);
		SanityCraftMenuLayout.renderLogo(guiGraphics, layout.titleX(), layout.titleY(), layout.titleWidth(), layout.titleHeight());
		if (SanityCraftMenuEffects.hasHorizontalGlitch()) {
			SanityCraftMenuAtmosphereRenderer.renderLogoGlitchBand(
					guiGraphics,
					layout.titleX(),
					layout.titleY(),
					layout.titleWidth(),
					layout.titleHeight(),
					SanityCraftMenuEffects.getHorizontalGlitchShift(),
					() -> SanityCraftMenuLayout.renderLogo(guiGraphics, layout.titleX(), layout.titleY(), layout.titleWidth(), layout.titleHeight()));
		}
		if (logoBrightness > 1.0F) {
			int alpha = Math.max(0, Math.min(26, Math.round((logoBrightness - 1.0F) * 260.0F)));
			guiGraphics.fill(
					layout.titleX(),
					layout.titleY(),
					layout.titleX() + layout.titleWidth(),
					layout.titleY() + layout.titleHeight(),
					(alpha << 24) | 0xFFFFFF);
		} else if (logoBrightness < 1.0F) {
			int alpha = Math.max(0, Math.min(26, Math.round((1.0F - logoBrightness) * 260.0F)));
			guiGraphics.fill(
					layout.titleX(),
					layout.titleY(),
					layout.titleX() + layout.titleWidth(),
					layout.titleY() + layout.titleHeight(),
					(alpha << 24));
		}
		guiGraphics.drawCenteredString(this.font, SanityCraftMenuEffects.getSubtitle(), this.width / 2, layout.subtitleY(), 0xFFD4A3A1);
		int subtitleRuleWidth = Mth.clamp(Math.round(layout.titleWidth() * 0.26F), 48, 148);
		int subtitleRuleY = layout.subtitleY() + this.font.lineHeight + Math.max(3, Math.round(layout.scale() * 4.0F));
		guiGraphics.fill(this.width / 2 - subtitleRuleWidth / 2, subtitleRuleY, this.width / 2 + subtitleRuleWidth / 2, subtitleRuleY + 1, 0x5AA87070);
		guiGraphics.fill(this.width / 2 - subtitleRuleWidth / 4, subtitleRuleY + 1, this.width / 2 + subtitleRuleWidth / 4, subtitleRuleY + 2, 0x342A0D0D);
	}

	private void renderFooter(GuiGraphics guiGraphics) {
		SanityCraftMenuLayout.renderFooter(guiGraphics, this.font, layout.footer());
	}

	private void renderAtmosphere(GuiGraphics guiGraphics, long time, float distortion) {
		SanityCraftMenuAtmosphereRenderer.renderTitleAtmosphere(guiGraphics, guiGraphics.guiWidth(), guiGraphics.guiHeight(), time, distortion);
	}

	private void renderPhantomButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (!SanityCraftMenuEffects.hasPhantomButton()) {
			return;
		}

		int x = layout.buttonStartX() + SanityCraftMenuEffects.getPhantomButtonXOffset();
		int stride = layout.buttonHeight() + layout.buttonSpacing();
		int y = layout.buttonStartY() + SanityCraftMenuEffects.getPhantomButtonRow() * stride;
		if (SanityCraftMenuEffects.isPhantomButtonBetweenRows()) {
			y += stride / 2;
		}
		y = Math.max(16, Math.min(this.height - layout.buttonHeight() - 24, y));
		int centerX = x + layout.buttonWidth() / 2;
		int centerY = y + layout.buttonHeight() / 2;
		int dx = mouseX - centerX;
		int dy = mouseY - centerY;
		if (dx * dx + dy * dy < 52 * 52) {
			SanityCraftMenuEffects.dismissPhantomButton();
			return;
		}

		float pulse = 0.62F + (float) ((Math.sin(System.currentTimeMillis() / 70.0D) + 1.0D) * 0.08D);
		SanityCraftMenuButton.renderDecorative(
				guiGraphics,
				x,
				y,
				layout.buttonWidth(),
				layout.buttonHeight(),
				SanityCraftMenuEffects.getPhantomButtonLabel(),
				SanityCraftMenuButton.Accent.PHANTOM,
				false,
				pulse);
	}

	private boolean isMouseOverPhantomButton(double mouseX, double mouseY) {
		if (!SanityCraftMenuEffects.hasPhantomButton()) {
			return false;
		}
		int stride = layout.buttonHeight() + layout.buttonSpacing();
		int x = layout.buttonStartX() + SanityCraftMenuEffects.getPhantomButtonXOffset();
		int y = layout.buttonStartY() + SanityCraftMenuEffects.getPhantomButtonRow() * stride;
		if (SanityCraftMenuEffects.isPhantomButtonBetweenRows()) {
			y += stride / 2;
		}
		y = Math.max(16, Math.min(this.height - layout.buttonHeight() - 24, y));
		return mouseX >= x && mouseX < x + layout.buttonWidth() && mouseY >= y && mouseY < y + layout.buttonHeight();
	}
}
