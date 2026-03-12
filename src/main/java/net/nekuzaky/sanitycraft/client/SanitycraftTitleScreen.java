package net.nekuzaky.sanitycraft.client;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.TitleScreen;

public class SanitycraftTitleScreen extends TitleScreen {
	private final Map<AbstractWidget, WidgetAnchor> widgetAnchors = new IdentityHashMap<>();

	public SanitycraftTitleScreen() {
		super();
	}

	@Override
	protected void init() {
		super.init();
		captureWidgetAnchors();
	}

	@Override
	public void tick() {
		super.tick();
		long time = System.currentTimeMillis();
		int index = 0;
		for (GuiEventListener child : children()) {
			if (!(child instanceof AbstractWidget widget)) {
				continue;
			}
			WidgetAnchor anchor = widgetAnchors.get(widget);
			if (anchor == null) {
				continue;
			}
			int driftX = (int) (Math.sin(time / (680.0D + index * 45.0D) + index * 0.45D) * 2.0D);
			int driftY = (int) (Math.cos(time / (830.0D + index * 60.0D) + index * 0.30D) * 1.0D);
			widget.setX(anchor.x + driftX);
			widget.setY(anchor.y + driftY);
			index++;
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		MainMenuHorrorOverlay.renderOverlay(guiGraphics, this, partialTick);
	}

	private void captureWidgetAnchors() {
		widgetAnchors.clear();
		for (GuiEventListener child : children()) {
			if (child instanceof AbstractWidget widget) {
				widgetAnchors.put(widget, new WidgetAnchor(widget.getX(), widget.getY()));
			}
		}
	}

	private record WidgetAnchor(int x, int y) {
	}
}
