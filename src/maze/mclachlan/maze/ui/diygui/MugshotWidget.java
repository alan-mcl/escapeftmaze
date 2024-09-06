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

import java.awt.Image;
import java.awt.image.BufferedImage;
import mclachlan.diygui.DIYPane;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.ui.diygui.render.maze.MazeRendererFactory;

public class MugshotWidget extends DIYPane
{
	private Image portrait;
	private PlayerCharacter character;
	private boolean selected;

	/*-------------------------------------------------------------------------*/
	@Override
	public String getWidgetName()
	{
		return MazeRendererFactory.MUGSHOT_WIDGET;
	}

	/*-------------------------------------------------------------------------*/
	public Image getPortrait()
	{
		return portrait;
	}

	/*-------------------------------------------------------------------------*/
	public void setPortrait(BufferedImage p)
	{
		portrait = p;
	}

	/*-------------------------------------------------------------------------*/
	public PlayerCharacter getCharacter()
	{
		return character;
	}

	/*-------------------------------------------------------------------------*/
	public void setCharacter(PlayerCharacter character)
	{
		this.character = character;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isSelected()
	{
		return selected;
	}

	/*-------------------------------------------------------------------------*/
	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}
}
