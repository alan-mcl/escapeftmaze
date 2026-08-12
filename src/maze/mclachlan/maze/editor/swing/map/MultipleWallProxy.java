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

import java.util.*;
import mclachlan.crusader.Texture;
import mclachlan.crusader.MouseClickScript;
import mclachlan.crusader.Wall;

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
	public Texture getMaskTexture(int index)
	{
		Texture[] firstMasks = walls.get(0).getMaskTextures();
		if (firstMasks == null || firstMasks.length <= index)
		{
			return null;
		}

		Texture x = firstMasks[index];

		for (Wall w : walls)
		{
			Texture[] masks = w.getMaskTextures();
			if (masks == null || masks.length <= index || masks[index] != x)
			{
				return null;
			}
		}

		return x;
	}

	@Override
	public Texture[] getTextures()
	{
		Texture[] first = walls.get(0).getTextures();
		if (first == null)
		{
			return new Texture[0];
		}

		// Preserve height indices. Skipping mismatched slots used to pack
		// surviving textures toward index 0 and desync the editor dropdowns.
		Texture[] result = new Texture[first.length];
		for (int i = 0; i < first.length; i++)
		{
			result[i] = getTexture(i);
		}
		return result;
	}

	@Override
	public Texture[] getMaskTextures()
	{
		Texture[] first = walls.get(0).getMaskTextures();
		if (first == null)
		{
			return null;
		}

		Texture[] result = new Texture[first.length];
		for (int i = 0; i < first.length; i++)
		{
			result[i] = getMaskTexture(i);
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Texture getTexture(int index)
	{
		Texture[] firstTextures = walls.get(0).getTextures();
		if (firstTextures == null || firstTextures.length <= index)
		{
			return null;
		}

		Texture x = firstTextures[index];

		for (Wall w : walls)
		{
			Texture[] textures = w.getTextures();
			if (textures == null || textures.length <= index || textures[index] != x)
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
			if (w.getMaskTextureMouseClickScript() != x)
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
			if (w.getMouseClickScript() != x)
			{
				return null;
			}
		}

		return x;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public MouseClickScript getInternalScript()
	{
		MouseClickScript x = walls.get(0).getInternalScript();

		for (Wall w : walls)
		{
			if (w.getInternalScript() != x)
			{
				return null;
			}
		}

		return x;
	}

	@Override
	public int getHeight()
	{
		int ll = walls.get(0).getHeight();

		for (Wall w : walls)
		{
			if (ll != w.getHeight())
			{
				// differing heights amongst the group
				return -1;
			}
		}

		return ll;
	}

	@Override
	public void setHeight(int height)
	{
		for (Wall w : walls)
		{
			w.setHeight(height);
		}
	}

	@Override
	public void setTextures(Texture[] textures)
	{
		for (Wall w : walls)
		{
			w.setTextures(textures);
		}
	}

	@Override
	public void setMaskTextures(Texture[] maskTextures)
	{
		for (Wall w : walls)
		{
			w.setMaskTextures(maskTextures);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setMaskTexture(Texture maskTexture)
	{
		for (Wall w : walls)
		{
			w.setMaskTexture(0, maskTexture); // todo wall height
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setMaskTextureMouseClickScript(
		MouseClickScript maskTextureMouseClickScript)
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
	public void setInternalScript(MouseClickScript mouseClickScript)
	{
		for (Wall w : walls)
		{
			w.setInternalScript(mouseClickScript);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setTexture(Texture texture)
	{
		for (Wall w : walls)
		{
			w.setTexture(0, texture); // todo: height
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
