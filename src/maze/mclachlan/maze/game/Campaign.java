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

package mclachlan.maze.game;

/**
 *
 */
public class Campaign
{
	private String name;
	private String displayName;
	private String description;
	private String startingScript;
	private String defaultRace;
	private String defaultPortrait;
	private String introScript;
	private String parentCampaign;

	/*-------------------------------------------------------------------------*/
	public Campaign(
		String name,
		String displayName,
		String description,
		String parentCampaign,
		String startingScript,
		String defaultRace,
		String defaultPortrait, 
		String introScript)
	{
		this.parentCampaign = "".equalsIgnoreCase(parentCampaign)?null:parentCampaign;
		this.introScript = introScript;
		this.defaultPortrait = defaultPortrait;
		this.defaultRace = defaultRace;
		this.description = description;
		this.displayName = displayName;
		this.startingScript = startingScript;
		this.name = name;
	}

	/*-------------------------------------------------------------------------*/
	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getStartingScript()
	{
		return startingScript;
	}

	public void setStartingScript(String startingScript)
	{
		this.startingScript = startingScript;
	}

	public String getDefaultPortrait()
	{
		return defaultPortrait;
	}

	public void setDefaultPortrait(String defaultPortrait)
	{
		this.defaultPortrait = defaultPortrait;
	}

	public String getDefaultRace()
	{
		return defaultRace;
	}

	public void setDefaultRace(String defaultRace)
	{
		this.defaultRace = defaultRace;
	}

	public String getIntroScript()
	{
		return introScript;
	}

	public void setIntroScript(String introScript)
	{
		this.introScript = introScript;
	}

	public String getParentCampaign()
	{
		return parentCampaign;
	}

	public void setParentCampaign(String parentCampaign)
	{
		this.parentCampaign = parentCampaign;
	}

	@Override
	public String toString()
	{
		return "Campaign{" +
			"name='" + name + '\'' +
			'}';
	}
}
