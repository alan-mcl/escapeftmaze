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

import mclachlan.crusader.MouseClickScript;
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
	@Override
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
	@Override
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
	@Override
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
	@Override
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
	@Override
	public int getLightLevel()
	{
		int ll = crusaderTiles.get(0).getLightLevel();
		
		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			if (ll != t.getLightLevel())
			{
				// differing light levels amongst the group
				return -1;
			}
		}
		
		return ll;
	}

	@Override
	public int getCeilingHeight()
	{
		int ll = crusaderTiles.get(0).getCeilingHeight();

		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			if (ll != t.getCeilingHeight())
			{
				// differing heights amongst the group
				return -1;
			}
		}

		return ll;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setCeilingTexture(Texture ceilingTexture)
	{
		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			t.setCeilingTexture(ceilingTexture);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setCeilingMaskTexture(Texture ceilingTexture)
	{
		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			t.setCeilingMaskTexture(ceilingTexture);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setFloorTexture(Texture floorTexture)
	{
		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			t.setFloorTexture(floorTexture);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setFloorMaskTexture(Texture floorTexture)
	{
		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			t.setFloorMaskTexture(floorTexture);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setLightLevel(int lightLevel)
	{
		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			t.setLightLevel(lightLevel);
		}
	}

	@Override
	public void setCeilingHeight(int height)
	{
		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			t.setCeilingHeight(height);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public mclachlan.maze.map.Tile.RestingDanger getRestingDanger()
	{
		mclachlan.maze.map.Tile.RestingDanger x = mazeTiles.get(0).getRestingDanger();

		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			if (!x.equals(t.getRestingDanger()))
			{
				return null;
			}
		}

		return x;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public mclachlan.maze.map.Tile.RestingEfficiency getRestingEfficiency()
	{
		mclachlan.maze.map.Tile.RestingEfficiency x = mazeTiles.get(0).getRestingEfficiency();

		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			if (!x.equals(t.getRestingEfficiency()))
			{
				return null;
			}
		}

		return x;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setRestingDanger(
		mclachlan.maze.map.Tile.RestingDanger restingDanger)
	{
		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			t.setRestingDanger(restingDanger);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setRestingEfficiency(
		mclachlan.maze.map.Tile.RestingEfficiency restingEfficiency)
	{
		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			t.setRestingEfficiency(restingEfficiency);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
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
	@Override
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
	@Override
	public mclachlan.maze.map.Tile.TerrainType getTerrainType()
	{
		mclachlan.maze.map.Tile.TerrainType x = mazeTiles.get(0).getTerrainType();
		
		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			if (!x.equals(t.getTerrainType()))
			{
				return null;
			}
		}
		
		return x;
	}

	/*-------------------------------------------------------------------------*/
	@Override
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
	@Override
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
	@Override
	public List<TileScript> getScripts()
	{
		// todo??
		return null;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setRandomEncounterChance(int randomEncounterChance)
	{
		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			t.setRandomEncounterChance(randomEncounterChance);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setRandomEncounters(EncounterTable randomEncounters)
	{
		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			t.setRandomEncounters(randomEncounters);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setScripts(List<TileScript> scripts)
	{
		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			t.setScripts(scripts);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setStatModifier(StatModifier statModifier)
	{
		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			t.setStatModifier(statModifier);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setTerrainSubType(String terrainSubType)
	{
		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			t.setTerrainSubType(terrainSubType);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setTerrainType(mclachlan.maze.map.Tile.TerrainType terrainType)
	{
		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			t.setTerrainType(terrainType);
		}
	}

	@Override
	public void setSector(String sector)
	{
		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			t.setSector(sector);
		}
	}

	@Override
	public String getSector()
	{
		String x = mazeTiles.get(0).getSector();

		for (mclachlan.maze.map.Tile t : mazeTiles)
		{
			if (x == null)
			{
				if (t.getSector() != null)
				{
					return null;
				}
			}
			else
			{
				if (!x.equals(t.getSector()))
				{
					return null;
				}
			}
		}

		return x;
	}

	@Override
	public MouseClickScript getFloorMouseClickScript()
	{
		MouseClickScript x = crusaderTiles.get(0).getFloorMouseClickScript();

		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			if (!x.equals(t.getFloorMouseClickScript()))
			{
				return null;
			}
		}

		return x;
	}

	@Override
	public void setFloorMouseClickScript(MouseClickScript floorMouseClickScript)
	{
		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			t.setFloorMouseClickScript(floorMouseClickScript);
		}
	}

	@Override
	public MouseClickScript getFloorMaskTextureMouseClickScript()
	{
		MouseClickScript x = crusaderTiles.get(0).getFloorMaskTextureMouseClickScript();

		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			if (!x.equals(t.getFloorMaskTextureMouseClickScript()))
			{
				return null;
			}
		}

		return x;
	}

	@Override
	public void setFloorMaskTextureMouseClickScript(
		MouseClickScript floorMaskTextureMouseClickScript)
	{
		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			t.setFloorMaskTextureMouseClickScript(floorMaskTextureMouseClickScript);
		}
	}

	@Override
	public MouseClickScript getCeilingMouseClickScript()
	{
		MouseClickScript x = crusaderTiles.get(0).getCeilingMouseClickScript();

		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			if (!x.equals(t.getCeilingMouseClickScript()))
			{
				return null;
			}
		}

		return x;
	}

	@Override
	public void setCeilingMouseClickScript(
		MouseClickScript ceilingMouseClickScript)
	{
		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			t.setCeilingMouseClickScript(ceilingMouseClickScript);
		}
	}

	@Override
	public MouseClickScript getCeilingMaskTextureMouseClickScript()
	{
		MouseClickScript x = crusaderTiles.get(0).getCeilingMaskTextureMouseClickScript();

		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			if (!x.equals(t.getCeilingMaskTextureMouseClickScript()))
			{
				return null;
			}
		}

		return x;
	}

	@Override
	public void setCeilingMaskTextureMouseClickScript(
		MouseClickScript ceilingMaskTextureMouseClickScript)
	{
		for (mclachlan.crusader.Tile t : crusaderTiles)
		{
			t.setCeilingMaskTextureMouseClickScript(ceilingMaskTextureMouseClickScript);
		}
	}
}
