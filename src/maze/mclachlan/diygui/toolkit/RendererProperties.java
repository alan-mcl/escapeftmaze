package mclachlan.diygui.toolkit;

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
		BUTTON_PANE_HEIGHT
	}

	int getProperty(Property p);
}
