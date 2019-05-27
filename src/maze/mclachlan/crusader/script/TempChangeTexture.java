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

package mclachlan.crusader.script;

import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Map;
import mclachlan.crusader.MapScript;

/**
 * Temporarily changes EngineObject texture, for the duration of the texture
 * animation.
 */
public class TempChangeTexture extends MapScript
{
	EngineObject obj;
	long timeToRemove;
	CrusaderEngine engine;
	int txt;

	/*-------------------------------------------------------------------------*/
	public TempChangeTexture(
		EngineObject obj,
		int texture,
		CrusaderEngine engine)
	{
		this.engine = engine;
		this.obj = obj;
		this.txt = obj.getCurrentTexture();
		this.obj.setCurrentTexture(texture);

		long now = System.currentTimeMillis();
		obj.setTextureLastChanged(now);
		timeToRemove = now + (obj.getTextures()[texture].getAnimationDelay() * 
			obj.getTextures()[texture].getImages().length);
	}

	/*-------------------------------------------------------------------------*/
	public void execute(long framecount, Map map)
	{
		// check if it's time to remove ourselves:
		if (System.currentTimeMillis() > timeToRemove)
		{
			obj.setCurrentTexture(txt);
			map.removeScript(this);
		}
	}
}
