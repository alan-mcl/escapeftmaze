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
 * Temporarily replaces an EngineObject.
 */
public class TempReplaceObject extends MapScript
{
	EngineObject oldObj, newObj;
	long timeToRemove;
	CrusaderEngine engine;
	
	/*-------------------------------------------------------------------------*/
	public TempReplaceObject(
		EngineObject oldObj, 
		EngineObject newObj,
		CrusaderEngine engine)
	{
		this.oldObj = oldObj;
		this.newObj = newObj;
		this.engine = engine;
		Texture txt = oldObj.getNorthTexture();
		
		newObj.setTileIndex(oldObj.getTileIndex());
		newObj.setGridX(oldObj.getGridX());
		newObj.setGridY(oldObj.getGridY());
		newObj.setXPos(oldObj.getXPos());
		newObj.setYPos(oldObj.getYPos());
		
		engine.removeObject(oldObj);
		engine.addObject(newObj, false);
		
		timeToRemove = System.currentTimeMillis()+
			(txt.getAnimationDelay() * txt.getImages().length);
	}

	/*-------------------------------------------------------------------------*/
	public void execute(int framecount, Map map)
	{
		// check if it's time to remove ourselves:
		if (System.currentTimeMillis() > timeToRemove)
		{
			engine.removeObject(newObj);
			engine.addObject(oldObj, false);
			map.removeScript(this);
		}
	}
}
