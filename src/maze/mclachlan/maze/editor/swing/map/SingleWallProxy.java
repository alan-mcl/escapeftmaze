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

	@Override
	public Texture getMaskTexture()
	{
		return wall.getMaskTexture();
	}

	@Override
	public Texture getTexture()
	{
		return wall.getTexture();
	}

	@Override
	public boolean isVisible()
	{
		return wall.isVisible();
	}

	@Override
	public boolean isSolid()
	{
		return wall.isSolid();
	}

	@Override
	public MouseClickScript getMaskTextureMouseClickScript()
	{
		return wall.getMaskTextureMouseClickScript();
	}

	@Override
	public MouseClickScript getMouseClickScript()
	{
		return wall.getMouseClickScript();
	}

	@Override
	public MouseClickScript getInternalScript()
	{
		return wall.getInternalScript();
	}

	@Override
	public int getHeight()
	{
		return wall.getHeight();
	}

	@Override
	public void setHeight(int height)
	{
		wall.setHeight(height);
	}

	@Override
	public void setMaskTexture(Texture maskTexture)
	{
		wall.setMaskTexture(maskTexture);
	}

	@Override
	public void setMaskTextureMouseClickScript(MouseClickScript maskTextureMouseClickScript)
	{
		wall.setMaskTextureMouseClickScript(maskTextureMouseClickScript);
	}

	@Override
	public void setMouseClickScript(MouseClickScript mouseClickScript)
	{
		wall.setMouseClickScript(mouseClickScript);
	}

	@Override
	public void setInternalScript(MouseClickScript mouseClickScript)
	{
		wall.setInternalScript(mouseClickScript);
	}

	@Override
	public void setTexture(Texture texture)
	{
		wall.setTexture(texture);
	}

	@Override
	public void setVisible(boolean visible)
	{
		wall.setVisible(visible);
	}

	@Override
	public void setSolid(boolean solid)
	{
		wall.setSolid(solid);
	}


}
