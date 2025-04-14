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

import mclachlan.crusader.EngineObject;
import mclachlan.crusader.MouseClickScript;
import mclachlan.crusader.ObjectScript;
import mclachlan.crusader.Texture;

public abstract class ObjectProxy
{
	public abstract String getName();
	public abstract void setName(String name);
	public abstract Texture getNorthTexture();
	public abstract void setNorthTexture(Texture northTexture);
	public abstract Texture getSouthTexture();
	public abstract void setSouthTexture(Texture southTexture);
	public abstract Texture getEastTexture();
	public abstract void setEastTexture(Texture eastTexture);
	public abstract Texture getWestTexture();
	public abstract void setWestTexture(Texture westTexture);
	public abstract boolean isLightSource();
	public abstract void setLightSource(boolean lightSource);
	public abstract MouseClickScript getMouseClickScript();
	public abstract void setMouseClickScript(MouseClickScript mouseClickScript);
	public abstract EngineObject.Alignment getVerticalAlignment();
	public abstract void setVerticalAlignment(EngineObject.Alignment verticalAlignment);
	public abstract ObjectScript[] getScripts();
	public abstract void setScripts(ObjectScript[] scripts);
	public abstract int getXPos();
	public abstract void setXPos(int x);
	public abstract int getYPos();
	public abstract void setYPos(int y);
}
