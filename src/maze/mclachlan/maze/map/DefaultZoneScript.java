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

package mclachlan.maze.map;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.PercentageTable;

/**
 * The default zone script simply advances the sky texture of the map at regular
 * intervals: providing basic support for a day/night cycle.
 */
public class DefaultZoneScript extends ZoneScript
{
	/**
	 * The number of game turns before the texture changes to the next one.
	 */
	private int turnsBetweenChange;

	/**
	 * The zone light level diff with each change in sky texture. Sky textures
	 * are assumed to follow a light...dark...light cycle
	 */
	private int lightLevelDiff;

	/**
	 * Any ambient sounds/animations/etc scripts.
	 * Should not sum to 100.
	 */
	private PercentageTable<String> ambientScripts;

	public DefaultZoneScript()
	{
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param turnsBetweenChange
	 * 	The number of turns between changing the sky image, or -1 if the image
	 * @param lightLevelDiff
	 * 	The light level change every time the sky changes
	 * @param ambientScripts
	 * 	Any ambient scripts to be executed as the player moves around the zone,
	 * 	may be null.
	 */
	public DefaultZoneScript(
		int turnsBetweenChange,
		int lightLevelDiff,
		PercentageTable<String> ambientScripts)
	{
		this.turnsBetweenChange = turnsBetweenChange;
		this.lightLevelDiff = lightLevelDiff;
		this.ambientScripts = ambientScripts;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> endOfTurn(Zone zone, long turnNr)
	{
		if (turnsBetweenChange != -1)
		{
			//
			// Sky texture change: todo
			//
/*
			int nrSkyImages = zone.getMap().getSkyTexture().getNrFrames();
			int skyImageIndex = (int)((turnNr/turnsBetweenChange)%nrSkyImages);

			zone.getMap().getSkyTexture().setCurrentFrame(skyImageIndex);

			//
			// Light level adjustments
			// This relies on there being an even nr of skyImages
			//
			if (lightLevelDiff > 0 && (turnNr % turnsBetweenChange == 0))
			{
				double half = nrSkyImages/2.0;
				if (skyImageIndex < half)
				{
					// dec light level
					zone.getMap().incLightLevel(-lightLevelDiff);
				}
				else
				{
					// inc light level
					zone.getMap().incLightLevel(lightLevelDiff);
				}
			}
*/
		}

		//
		// Ambient scripts, only when the party is moving
		//
		if (Maze.getInstance().getState() == Maze.State.MOVEMENT && ambientScripts != null)
		{
			String scriptName = ambientScripts.getRandomItem();
			if (scriptName != null)
			{
				MazeScript script = Database.getInstance().getMazeScript(scriptName);
				return script.getEvents();
			}
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> init(Zone zone, long turnNr)
	{
		this.endOfTurn(zone, turnNr);

		return null;
	}
	
	/*-------------------------------------------------------------------------*/
	public int getTurnsBetweenChange()
	{
		return turnsBetweenChange;
	}

	/*-------------------------------------------------------------------------*/
	public int getLightLevelDiff()
	{
		return lightLevelDiff;
	}

	public void setTurnsBetweenChange(int turnsBetweenChange)
	{
		this.turnsBetweenChange = turnsBetweenChange;
	}

	public void setLightLevelDiff(int lightLevelDiff)
	{
		this.lightLevelDiff = lightLevelDiff;
	}

	public void setAmbientScripts(
		PercentageTable<String> ambientScripts)
	{
		this.ambientScripts = ambientScripts;
	}

	/*-------------------------------------------------------------------------*/
	public PercentageTable<String> getAmbientScripts()
	{
		return ambientScripts;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof DefaultZoneScript))
		{
			return false;
		}

		DefaultZoneScript that = (DefaultZoneScript)o;

		if (getTurnsBetweenChange() != that.getTurnsBetweenChange())
		{
			return false;
		}
		if (getLightLevelDiff() != that.getLightLevelDiff())
		{
			return false;
		}
		return getAmbientScripts() != null ? getAmbientScripts().equals(that.getAmbientScripts()) : that.getAmbientScripts() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getTurnsBetweenChange();
		result = 31 * result + getLightLevelDiff();
		result = 31 * result + (getAmbientScripts() != null ? getAmbientScripts().hashCode() : 0);
		return result;
	}
}