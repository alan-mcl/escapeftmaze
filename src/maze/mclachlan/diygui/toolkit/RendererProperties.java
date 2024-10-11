package mclachlan.diygui.toolkit;

import java.awt.image.BufferedImage;

/**
 *
 */
public interface RendererProperties
{
	enum Property
	{
		TRANSPARENT_PANEL_BORDER,
		PANEL_HEAVY_BORDER,
		PANEL_MED_BORDER,
		PANEL_LIGHT_BORDER,
		IMAGE_BACK_PANEL_BORDER,
		DIALOG_BORDER,

		INSET,
		TITLE_PANE_HEIGHT,
		BUTTON_PANE_HEIGHT,

		SCROLLBAR_WIDTH,
		SLIDER_WIDTH,
		SLIDER_HEIGHT,

		ITEM_WIDGET_SIZE,
		CONDITION_ICON_SIZE,

		PCW_PORTRAIT_FRAME_BORDER,
		PCW_PORTRAIT_WIDTH,
		PCW_PORTRAIT_HEIGHT,
	}

	int getProperty(Property p);

	BufferedImage getImageResource(String imageId);
}
