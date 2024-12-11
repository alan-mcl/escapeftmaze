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

public abstract class WallProxy
{
	public abstract Texture getMaskTexture();
	public abstract Texture getTexture();
	public abstract boolean isVisible();
	public abstract boolean isSolid();
	public abstract MouseClickScript getMaskTextureMouseClickScript();
	public abstract MouseClickScript getMouseClickScript();

	public abstract MouseClickScript getInternalScript();

	public abstract int getHeight();

	public abstract void setHeight(int height);

	public abstract void setMaskTexture(Texture maskTexture);
	public abstract void setMaskTextureMouseClickScript(MouseClickScript maskTextureMouseClickScript);
	public abstract void setMouseClickScript(MouseClickScript mouseClickScript);

	public abstract void setInternalScript(MouseClickScript mouseClickScript);
	public abstract void setTexture(Texture texture);
	public abstract void setVisible(boolean visible);
	public abstract void setSolid(boolean solid);
}
