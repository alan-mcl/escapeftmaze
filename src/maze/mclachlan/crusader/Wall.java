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
	/** The texture to map onto this wall*/
	Texture texture;
	/** Any texture to mask over this wall*/
	Texture maskTexture;
	/** Any script to run when the user clicks on this wall*/
	MouseClickScript mouseClickScript;
	/** Any script to run when the user clicks on the mask texture on this wall*/
	MouseClickScript maskTextureMouseClickScript;
	/** Any internal script run by other means */
	MouseClickScript internalScript;

	/*-------------------------------------------------------------------------*/
	/**
	 * @param texture
	 * 	The texture to map onto this wall
	 * @param maskTexture
	 * 	Any texture to mask over this wall
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
		Texture texture, 
		Texture maskTexture, 
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
		this.texture = texture;
		this.maskTexture = maskTexture;
		this.visible = visible;
		this.solid = solid;
		this.internalScript = internalScript;
	}

	public Wall()
	{

	}

	/*-------------------------------------------------------------------------*/
	public Texture getMaskTexture()
	{
		return maskTexture;
	}

	/*-------------------------------------------------------------------------*/
	public Texture getTexture()
	{
		return texture;
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
	public void setMaskTexture(Texture maskTexture)
	{
		this.maskTexture = maskTexture;
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
	public void setTexture(Texture texture)
	{
		this.texture = texture;
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

	@Override
	public String toString()
	{
		return "Wall{" +
			"visible=" + visible +
			", texture=" + texture +
			'}';
	}
}
