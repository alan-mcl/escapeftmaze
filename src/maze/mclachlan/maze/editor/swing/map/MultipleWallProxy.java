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
import java.util.List;

/**
 *
 */
public class MultipleWallProxy extends WallProxy
{
	List<Wall> walls;

	/*-------------------------------------------------------------------------*/
	public MultipleWallProxy(List<Wall> walls)
	{
		this.walls = walls;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Texture getMaskTexture()
	{
		Texture x = walls.get(0).getMaskTexture();
		
		for (Wall w : walls)
		{
			if (w.getMaskTexture() != x)
			{
				return null;
			}
		}
		
		return x;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Texture getTexture()
	{
		Texture x = walls.get(0).getTexture();
		
		for (Wall w : walls)
		{
			if (w.getTexture() != x)
			{
				return null;
			}
		}
		
		return x;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean isVisible()
	{
		boolean x = walls.get(0).isVisible();
		
		for (Wall w : walls)
		{
			if (w.isVisible() != x)
			{
				return false;
			}
		}
		
		return x;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean isSolid()
	{
		boolean x = walls.get(0).isSolid();

		for (Wall w : walls)
		{
			if (w.isSolid() != x)
			{
				return false;
			}
		}

		return x;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public MouseClickScript getMaskTextureMouseClickScript()
	{
		MouseClickScript x = walls.get(0).getMaskTextureMouseClickScript();
		
		for (Wall w : walls)
		{
			if (w.getMaskTextureMouseClickScript() != walls)
			{
				return null;
			}
		}
		
		return x;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public MouseClickScript getMouseClickScript()
	{
		MouseClickScript x = walls.get(0).getMouseClickScript();
		
		for (Wall w : walls)
		{
			if (w.getMouseClickScript() != walls)
			{
				return null;
			}
		}
		
		return x;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setMaskTexture(Texture maskTexture)
	{
		for (Wall w : walls)
		{
			w.setMaskTexture(maskTexture);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setMaskTextureMouseClickScript(MouseClickScript maskTextureMouseClickScript)
	{
		for (Wall w : walls)
		{
			w.setMaskTextureMouseClickScript(maskTextureMouseClickScript);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setMouseClickScript(MouseClickScript mouseClickScript)
	{
		for (Wall w : walls)
		{
			w.setMouseClickScript(mouseClickScript);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setTexture(Texture texture)
	{
		for (Wall w : walls)
		{
			w.setTexture(texture);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setVisible(boolean visible)
	{
		for (Wall w : walls)
		{
			w.setVisible(visible);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setSolid(boolean solid)
	{
		for (Wall w : walls)
		{
			w.setSolid(solid);
		}
	}
}
