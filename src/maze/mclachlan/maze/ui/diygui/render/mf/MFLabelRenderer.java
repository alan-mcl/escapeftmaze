package mclachlan.maze.ui.diygui.render.mf;

import mclachlan.maze.ui.diygui.render.maze.MazeLabelRenderer;

/**
 *
 */
public class MFLabelRenderer extends MazeLabelRenderer
{
	public MFLabelRenderer()
	{
		super.labelForegroundEnabled = Colours.LABEL_TEXT;
		super.labelForegroundEnabledHover = Colours.LABEL_TEXT_HIGHLIGHTED;
		super.labelForegroundDisabled = Colours.LABEL_TEXT_DISABLED;
	}
}