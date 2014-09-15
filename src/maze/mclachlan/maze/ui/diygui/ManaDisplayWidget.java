/*
 * Copyright (c) 2011 Alan McLachlan
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

import java.awt.Color;
import java.util.*;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.DIYFlowLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.ManaRequirement;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ManaDisplayWidget extends DIYPane
{
	private DIYLabel red, black, purple, gold, white, green, blue;
	
	/*-------------------------------------------------------------------------*/
	public ManaDisplayWidget()
	{
		super(new DIYFlowLayout(10,1, DIYToolkit.Align.CENTER));

		red = createLabel("screen/mana_icon_red", Color.BLACK);
		black = createLabel("screen/mana_icon_black", Color.WHITE);
		purple = createLabel("screen/mana_icon_purple", Color.YELLOW);
		gold = createLabel("screen/mana_icon_gold", Color.BLACK);
		white = createLabel("screen/mana_icon_white", Color.BLACK);
		green = createLabel("screen/mana_icon_green", Color.BLACK);
		blue = createLabel("screen/mana_icon_blue", Color.WHITE);
	}
	
	/*-------------------------------------------------------------------------*/
	private DIYLabel createLabel(String imageName, Color fore)
	{
		DIYLabel label = new DIYLabel("  ", DIYToolkit.Align.CENTER);
		label.setForegroundColour(fore);
		label.setIcon(Database.getInstance().getImage(imageName));

		this.add(label);

		return label;
	}
	
	/*-------------------------------------------------------------------------*/
	public void refresh(
		int red, 
		int black, 
		int purple, 
		int gold, 
		int white, 
		int green, 
		int blue)
	{
		this.red.setText(""+red);
		this.black.setText(""+black);
		this.purple.setText(""+purple);
		this.gold.setText(""+gold);
		this.white.setText(""+white);
		this.green.setText(""+green);
		this.blue.setText(""+blue);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(List<ManaRequirement> requirementsToCast)
	{
		this.refresh(0, 0, 0, 0, 0, 0, 0);
		
		if (requirementsToCast != null)
		{
			for (ManaRequirement m : requirementsToCast)
			{
				switch (m.getColour())
				{
					case MagicSys.ManaType.RED: red.setText(""+m.getAmount()); break;
					case MagicSys.ManaType.BLACK: black.setText(""+m.getAmount()); break;
					case MagicSys.ManaType.PURPLE: purple.setText(""+m.getAmount()); break;
					case MagicSys.ManaType.GOLD: gold.setText(""+m.getAmount()); break;
					case MagicSys.ManaType.WHITE: white.setText(""+m.getAmount()); break;
					case MagicSys.ManaType.GREEN: green.setText(""+m.getAmount()); break;
					case MagicSys.ManaType.BLUE: blue.setText(""+m.getAmount()); break;
					default: throw new MazeException("invalid ["+m.getColour()+"]");
				}
			}
		}
	}
}
