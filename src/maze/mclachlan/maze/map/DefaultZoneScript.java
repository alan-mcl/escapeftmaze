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

import java.awt.Color;
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
	 * When not {@code -1}, enables the day/night cycle (value is legacy; timing
	 * follows global {@link mclachlan.maze.game.GameTime}).
	 */
	private int turnsBetweenChange;

	/**
	 * Maximum ambient light reduction at midnight when day/night is enabled.
	 */
	private int lightLevelDiff;

	/**
	 * Optional night fog shade target; null uses engine default.
	 */
	private Color nightShadeTargetColor;

	/**
	 * Optional night sky gradient bottom colour; null uses engine default.
	 */
	private Color nightSkyBottomColor;

	/**
	 * Optional night sky gradient top colour; null uses engine default.
	 */
	private Color nightSkyTopColor;

	/**
	 * Any ambient sounds/animations/etc scripts.
	 * Should not sum to 100.
	 */
	private PercentageTable<String> ambientScripts;

	/** Captured sky gradient anchors for this zone instance; not persisted. */
	private transient DayNightPresentation.GradientAnchors gradientAnchors;

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
		applyDayNight(zone, turnNr);

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
		applyDayNight(zone, turnNr);

		return null;
	}

	/*-------------------------------------------------------------------------*/
	private void applyDayNight(Zone zone, long turnNr)
	{
		if (turnsBetweenChange == -1 || lightLevelDiff <= 0)
		{
			return;
		}

		if (gradientAnchors == null)
		{
			gradientAnchors = DayNightPresentation.captureGradientAnchors(zone.getMap());
		}

		DayNightPresentation.apply(
			zone,
			turnNr,
			lightLevelDiff,
			gradientAnchors,
			DayNightPresentation.NightPalette.from(this));
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

	public Color getNightShadeTargetColor()
	{
		return nightShadeTargetColor;
	}

	public Color getNightSkyBottomColor()
	{
		return nightSkyBottomColor;
	}

	public Color getNightSkyTopColor()
	{
		return nightSkyTopColor;
	}

	public void setTurnsBetweenChange(int turnsBetweenChange)
	{
		this.turnsBetweenChange = turnsBetweenChange;
	}

	public void setLightLevelDiff(int lightLevelDiff)
	{
		this.lightLevelDiff = lightLevelDiff;
	}

	public void setNightShadeTargetColor(Color nightShadeTargetColor)
	{
		this.nightShadeTargetColor = nightShadeTargetColor;
	}

	public void setNightSkyBottomColor(Color nightSkyBottomColor)
	{
		this.nightSkyBottomColor = nightSkyBottomColor;
	}

	public void setNightSkyTopColor(Color nightSkyTopColor)
	{
		this.nightSkyTopColor = nightSkyTopColor;
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
		if (!Objects.equals(getNightShadeTargetColor(), that.getNightShadeTargetColor()))
		{
			return false;
		}
		if (!Objects.equals(getNightSkyBottomColor(), that.getNightSkyBottomColor()))
		{
			return false;
		}
		if (!Objects.equals(getNightSkyTopColor(), that.getNightSkyTopColor()))
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
		result = 31 * result + (getNightShadeTargetColor() != null ? getNightShadeTargetColor().hashCode() : 0);
		result = 31 * result + (getNightSkyBottomColor() != null ? getNightSkyBottomColor().hashCode() : 0);
		result = 31 * result + (getNightSkyTopColor() != null ? getNightSkyTopColor().hashCode() : 0);
		result = 31 * result + (getAmbientScripts() != null ? getAmbientScripts().hashCode() : 0);
		return result;
	}
}
