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

package mclachlan.crusader;

import java.util.*;

/**
 *
 */
public class Wall
{
	/** True if this wall is visible*/
	boolean visible;
	/** True if this wall is solid and prevent*/
	boolean solid;
	/** height of this wall, in blocks */
	int height;
	/** The textures to map onto this wall, indexed by height from ground up*/
	Texture[] textures;
	/** Any textures to mask over this wall, indexed by height from ground up*/
	Texture[] maskTextures;
	/** Any script to run when the user clicks on this wall*/
	MouseClickScript mouseClickScript;
	/** Any script to run when the user clicks on the mask texture on this wall*/
	MouseClickScript maskTextureMouseClickScript;
	/** Any internal script run by other means */
	MouseClickScript internalScript;

	/*-------------------------------------------------------------------------*/
	/**
	 * @param textures
	 * 	The textures to map onto this wall, indexed by height from ground up
	 * @param maskTextures
	 * 	Any textures to mask over this wall, indexed by height from ground up
	 * @param visible
	 * 	True if this wall is visible
	 * @param solid
	 * 	True if this wall is solid and can't be moved through
	 * @param height
	 * 	Height of this wall in blocks
	 * @param mouseClickScript
	 * 	Any script to run when the user clicks on this wall
	 * @param maskTextureMouseClickScript
	 * 	Any script to run when the user clicks on the mask texture on this wall
	 * @param internalScript
	 * 	Any internal script run by other means
	 */
	public Wall(
		Texture[] textures,
		Texture[] maskTextures,
		boolean visible,
		boolean solid,
		int height,
		MouseClickScript mouseClickScript,
		MouseClickScript maskTextureMouseClickScript,
		MouseClickScript internalScript)
	{
		this.height = height;
		this.maskTextureMouseClickScript = maskTextureMouseClickScript;
		this.mouseClickScript = mouseClickScript;
		this.textures = textures;
		this.maskTextures = maskTextures;
		this.visible = visible;
		this.solid = solid;
		this.internalScript = internalScript;
	}

	public Wall()
	{
	}

	/*-------------------------------------------------------------------------*/
	public Texture getMaskTexture(int height)
	{
		return maskTextures == null ? null : maskTextures[height];
	}

	public Texture[] getMaskTextures()
	{
		return maskTextures;
	}

	public void setMaskTextures(Texture[] maskTextures)
	{
		this.maskTextures = maskTextures;
	}

	/*-------------------------------------------------------------------------*/
	public Texture getTexture(int height)
	{
		return textures[height];
	}

	public Texture[] getTextures()
	{
		return textures;
	}

	public void setTextures(Texture[] textures)
	{
		this.textures = textures;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isVisible()
	{
		return visible;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isSolid()
	{
		return solid;
	}

	/*-------------------------------------------------------------------------*/
	public MouseClickScript getMaskTextureMouseClickScript()
	{
		return maskTextureMouseClickScript;
	}

	/*-------------------------------------------------------------------------*/
	public MouseClickScript getMouseClickScript()
	{
		return mouseClickScript;
	}

	/*-------------------------------------------------------------------------*/
	public void setMaskTexture(int height, Texture maskTexture)
	{
		this.maskTextures[height] = maskTexture;
	}

	/*-------------------------------------------------------------------------*/
	public void setMaskTextureMouseClickScript(MouseClickScript maskTextureMouseClickScript)
	{
		this.maskTextureMouseClickScript = maskTextureMouseClickScript;
	}

	/*-------------------------------------------------------------------------*/
	public void setMouseClickScript(MouseClickScript mouseClickScript)
	{
		this.mouseClickScript = mouseClickScript;
	}

	/*-------------------------------------------------------------------------*/

	public MouseClickScript getInternalScript()
	{
		return internalScript;
	}

	public void setInternalScript(MouseClickScript internalScript)
	{
		this.internalScript = internalScript;
	}

	/*-------------------------------------------------------------------------*/
	public void setTexture(int height, Texture texture)
	{
		this.textures[height] = texture;
	}

	/*-------------------------------------------------------------------------*/
	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	/*-------------------------------------------------------------------------*/
	public void setSolid(boolean solid)
	{
		this.solid = solid;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof Wall))
		{
			return false;
		}

		Wall wall = (Wall)o;

		if (isVisible() != wall.isVisible())
		{
			return false;
		}
		if (isSolid() != wall.isSolid())
		{
			return false;
		}
		if (getHeight() != wall.getHeight())
		{
			return false;
		}
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		if (!Arrays.equals(getTextures(), wall.getTextures()))
		{
			return false;
		}
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		if (!Arrays.equals(getMaskTextures(), wall.getMaskTextures()))
		{
			return false;
		}
		if (getMouseClickScript() != null ? !getMouseClickScript().equals(wall.getMouseClickScript()) : wall.getMouseClickScript() != null)
		{
			return false;
		}
		if (getMaskTextureMouseClickScript() != null ? !getMaskTextureMouseClickScript().equals(wall.getMaskTextureMouseClickScript()) : wall.getMaskTextureMouseClickScript() != null)
		{
			return false;
		}
		return getInternalScript() != null ? getInternalScript().equals(wall.getInternalScript()) : wall.getInternalScript() == null;
	}

	@Override
	public int hashCode()
	{
		int result = (isVisible() ? 1 : 0);
		result = 31 * result + (isSolid() ? 1 : 0);
		result = 31 * result + getHeight();
		result = 31 * result + Arrays.hashCode(getTextures());
		result = 31 * result + Arrays.hashCode(getMaskTextures());
		result = 31 * result + (getMouseClickScript() != null ? getMouseClickScript().hashCode() : 0);
		result = 31 * result + (getMaskTextureMouseClickScript() != null ? getMaskTextureMouseClickScript().hashCode() : 0);
		result = 31 * result + (getInternalScript() != null ? getInternalScript().hashCode() : 0);
		return result;
	}

	@Override
	public String toString()
	{
		return "Wall{" +
			"visible=" + visible +
			", solid=" + solid +
			", height=" + height +
			", texture=" + Arrays.toString(textures) +
			", maskTexture=" + Arrays.toString(maskTextures) +
			", mouseClickScript=" + mouseClickScript +
			", maskTextureMouseClickScript=" + maskTextureMouseClickScript +
			", internalScript=" + internalScript +
			'}';
	}
}
