/*
 * Copyright (c) 2012 Alan McLachlan
 *
 * This file is part of Escape From The Maze.
 *
 * Escape From The Maze is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mclachlan.maze.ui.diygui;

import java.awt.Rectangle;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.DIYTextArea;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;

public class TipOfTheDayWidget extends DIYPane
{
	private final DIYTextArea tip;

	/*-------------------------------------------------------------------------*/
	public TipOfTheDayWidget(Rectangle bounds)
	{
		super(bounds);

		DIYLabel header = new DIYLabel("Tip of the Day", DIYToolkit.Align.LEFT);
		header.setForegroundColour(Constants.Colour.GOLD);
		int headerHeight = 20;
		header.setBounds(x, y, width, headerHeight);

		tip = new DIYTextArea("");
		tip.setTransparent(true);
		tip.setBounds(x, y+headerHeight, width, height-headerHeight);

		this.add(header);
		this.add(tip);
	}
	
	/*-------------------------------------------------------------------------*/
	public void refresh()
	{
		int index = Maze.getInstance().getUserConfig().getCurrentTipIndex();

		String text = StringUtil.getTipOfTheDayText(index);

		if (text == null)
		{
			index = 0;
			text = StringUtil.getTipOfTheDayText(index);
		}
		Maze.getInstance().getUserConfig().setCurrentTipIndex(index+1);
		Maze.getInstance().saveUserConfig();

		tip.setText(text);
	}
}
