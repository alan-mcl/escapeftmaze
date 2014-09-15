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

package mclachlan.maze.editor.swing.map;

import mclachlan.crusader.Texture;
import mclachlan.crusader.MouseClickScript;
import mclachlan.crusader.Wall;

/**
 *
 */
public class SingleWallProxy extends WallProxy
{
	Wall wall;

	public SingleWallProxy(Wall wall)
	{
		this.wall = wall;
	}

	public Texture getMaskTexture()
	{
		return wall.getMaskTexture();
	}

	public Texture getTexture()
	{
		return wall.getTexture();
	}

	public boolean isVisible()
	{
		return wall.isVisible();
	}

	public MouseClickScript getMaskTextureMouseClickScript()
	{
		return wall.getMaskTextureMouseClickScript();
	}

	public MouseClickScript getMouseClickScript()
	{
		return wall.getMouseClickScript();
	}

	public void setMaskTexture(Texture maskTexture)
	{
		wall.setMaskTexture(maskTexture);
	}

	public void setMaskTextureMouseClickScript(MouseClickScript maskTextureMouseClickScript)
	{
		wall.setMaskTextureMouseClickScript(maskTextureMouseClickScript);
	}

	public void setMouseClickScript(MouseClickScript mouseClickScript)
	{
		wall.setMouseClickScript(mouseClickScript);
	}

	public void setTexture(Texture texture)
	{
		wall.setTexture(texture);
	}

	public void setVisible(boolean visible)
	{
		wall.setVisible(visible);
	}
}
