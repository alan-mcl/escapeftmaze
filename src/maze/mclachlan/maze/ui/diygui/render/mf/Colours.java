package mclachlan.maze.ui.diygui.render.mf;

import java.awt.Color;
import mclachlan.maze.ui.diygui.Constants;

/**
 *
 */
public class Colours
{
//	public static final Color TEXT_AREA_TEXT = new Color(0x836854);
	public static final Color TEXT_AREA_TEXT = Color.LIGHT_GRAY;

	public static final Color LABEL_TEXT = TEXT_AREA_TEXT;
	public static final Color LABEL_TEXT_DISABLED = LABEL_TEXT.darker();
	public static final Color LABEL_TEXT_HIGHLIGHTED = Constants.Colour.GOLD;

	public static final Color LIST_BOX_TEXT = TEXT_AREA_TEXT;
	public static final Color LIST_BOX_TEXT_SELECTED = Color.DARK_GRAY;
	public static final Color LIST_BOX_TEXT_DISABLED = TEXT_AREA_TEXT.darker();
	public static final Color LIST_BOX_TEXT_HIGHLIGHTED = Constants.Colour.GOLD;

	public static final Color MF_BROWN = new Color(0x493026);
}
