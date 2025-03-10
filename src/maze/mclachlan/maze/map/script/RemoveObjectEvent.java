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

package mclachlan.maze.map.script;

import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.crusader.EngineObject;

/**
 *
 */
public class RemoveObjectEvent extends MazeEvent
{
	private String objectName;
	private EngineObject engineObject;

	public RemoveObjectEvent()
	{
	}

	/*-------------------------------------------------------------------------*/
	public RemoveObjectEvent(String objectName)
	{
		this.objectName = objectName;
	}

	/*-------------------------------------------------------------------------*/
	public RemoveObjectEvent(EngineObject engineObject)
	{
		this.engineObject = engineObject;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		if (objectName != null)
		{
			Maze.getInstance().removeObject(objectName);
		}
		else if (engineObject != null)
		{
			Maze.getInstance().removeObject(engineObject);
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/

	public String getObjectName()
	{
		return objectName;
	}

	public void setObjectName(String objectName)
	{
		this.objectName = objectName;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		RemoveObjectEvent that = (RemoveObjectEvent)o;

		if (getObjectName() != null ? !getObjectName().equals(that.getObjectName()) : that.getObjectName() != null)
		{
			return false;
		}
		return engineObject != null ? engineObject.equals(that.engineObject) : that.engineObject == null;
	}

	@Override
	public int hashCode()
	{
		int result = getObjectName() != null ? getObjectName().hashCode() : 0;
		result = 31 * result + (engineObject != null ? engineObject.hashCode() : 0);
		return result;
	}
}
