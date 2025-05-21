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

import mclachlan.crusader.*;

/**
 * Temporarily changes EngineObject texture, for the duration of the texture
 * animation.
 */
public class TempChangeTexture extends MapScript
{
	private final EngineObject obj;
	private final long timeToRemove;
	private final Texture previousTexture;

	/*-------------------------------------------------------------------------*/
	public TempChangeTexture(
		EngineObject obj,
		Texture texture)
	{
		this.obj = obj;
		this.previousTexture = obj.getCurrentTexture();
		this.obj.setCurrentTexture(texture);

		long now = System.currentTimeMillis();
		obj.setTextureLastChanged(now);
		timeToRemove = now + ((long)texture.getAnimationDelay() * texture.getImages().length);
	}

	/*-------------------------------------------------------------------------*/
	public void execute(long framecount, Map map)
	{
		// check if it's time to remove ourselves:
		if (System.currentTimeMillis() > timeToRemove)
		{
			obj.setCurrentTexture(previousTexture);
			map.removeScript(this);
		}
	}
}
