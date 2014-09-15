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
import mclachlan.crusader.Tile;
import java.util.List;
import mclachlan.maze.map.EncounterTable;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.stat.StatModifier;

/**
 *
 */
public class MultipleTileProxy extends TileProxy
{
	List<mclachlan.crusader.Tile> crusaderTiles;
	List<mclachlan.maze.map.Tile> mazeTiles;

	/*-------------------------------------------------------------------------*/
	public MultipleTileProxy(List<Tile> crusaderTiles, List<mclachlan.maze.map.Tile> mazeTiles)
	{
		this.crusaderTiles = crusaderTiles;
		this.mazeTiles = mazeTiles;
	}
	
	/*-------------------------------------------------------------------------*/
	public Texture getFloorTexture()
	{
		Texture txt = crusaderTiles.get(0).getFloorTexture();
		
		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			if (txt != t.getFloorTexture())
			{
				// differing textures amongst the group
				return null;
			}
		}
		
		return txt;
	}

	/*-------------------------------------------------------------------------*/
	public Texture getFloorMaskTexture()
	{
		Texture txt = crusaderTiles.get(0).getFloorMaskTexture();

		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			if (txt != t.getFloorMaskTexture())
			{
				// differing textures amongst the group
				return null;
			}
		}

		return txt;
	}

	/*-------------------------------------------------------------------------*/
	public Texture getCeilingTexture()
	{
		Texture txt = crusaderTiles.get(0).getCeilingTexture();
		
		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			if (txt != t.getCeilingTexture())
			{
				// differing textures amongst the group
				return null;
			}
		}
		
		return txt;
	}

	/*-------------------------------------------------------------------------*/
	public Texture getCeilingMaskTexture()
	{
		Texture txt = crusaderTiles.get(0).getCeilingMaskTexture();

		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			if (txt != t.getCeilingMaskTexture())
			{
				// differing textures amongst the group
				return null;
			}
		}

		return txt;
	}

	/*-------------------------------------------------------------------------*/
	public int getLightLevel()
	{
		int ll = crusaderTiles.get(0).getLightLevel();
		
		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			if (ll != t.getLightLevel())
			{
				// differing textures amongst the group
				return -1;
			}
		}
		
		return ll;
	}

	/*-------------------------------------------------------------------------*/
	public void setCeilingTexture(Texture ceilingTexture)
	{
		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			t.setCeilingTexture(ceilingTexture);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setCeilingMaskTexture(Texture ceilingTexture)
	{
		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			t.setCeilingMaskTexture(ceilingTexture);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setFloorTexture(Texture floorTexture)
	{
		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			t.setFloorTexture(floorTexture);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setFloorMaskTexture(Texture floorTexture)
	{
		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			t.setFloorMaskTexture(floorTexture);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setLightLevel(int lightLevel)
	{
		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			t.setLightLevel(lightLevel);
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public StatModifier getStatModifier()
	{
		StatModifier x = mazeTiles.get(0).getStatModifier();
		
		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			if (!x.equals(t.getStatModifier()))
			{
				return null;
			}
		}
		
		return x;
	}

	/*-------------------------------------------------------------------------*/
	public String getTerrainSubType()
	{
		String x = mazeTiles.get(0).getTerrainSubType();
		
		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			if (!x.equals(t.getStatModifier()))
			{
				return null;
			}
		}
		
		return x;
	}

	/*-------------------------------------------------------------------------*/
	public String getTerrainType()
	{
		String x = mazeTiles.get(0).getTerrainType();
		
		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			if (!x.equals(t.getStatModifier()))
			{
				return null;
			}
		}
		
		return x;
	}

	/*-------------------------------------------------------------------------*/
	public int getRandomEncounterChance()
	{
		int x = mazeTiles.get(0).getRandomEncounterChance();
		
		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			if (x != t.getRandomEncounterChance())
			{
				return -1;
			}
		}
		
		return x;
	}

	/*-------------------------------------------------------------------------*/
	public EncounterTable getRandomEncounters()
	{
		EncounterTable x = mazeTiles.get(0).getRandomEncounters();
		
		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			if (!x.equals(t.getRandomEncounters()))
			{
				return null;
			}
		}
		
		return x;
	}

	/*-------------------------------------------------------------------------*/
	public List<TileScript> getScripts()
	{
		// todo??
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public void setRandomEncounterChance(int randomEncounterChance)
	{
		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			t.setRandomEncounterChance(randomEncounterChance);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setRandomEncounters(EncounterTable randomEncounters)
	{
		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			t.setRandomEncounters(randomEncounters);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setScripts(List<TileScript> scripts)
	{
		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			t.setScripts(scripts);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setStatModifier(StatModifier statModifier)
	{
		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			t.setStatModifier(statModifier);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setTerrainSubType(String terrainSubType)
	{
		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			t.setTerrainSubType(terrainSubType);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setTerrainType(String terrainType)
	{
		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			t.setTerrainType(terrainType);
		}
	}
}
