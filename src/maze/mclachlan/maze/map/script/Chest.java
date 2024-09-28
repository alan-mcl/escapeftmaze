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

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Map;
import mclachlan.crusader.MouseClickScript;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.game.event.CheckPartyStatusEvent;
import mclachlan.maze.game.event.MazeScriptEvent;
import mclachlan.maze.game.event.SetChestStateEvent;
import mclachlan.maze.game.event.SetStateEvent;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.Trap;
import mclachlan.maze.stat.PercentageTable;
import mclachlan.maze.stat.SpellTarget;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.ui.diygui.ChestOptionsCallback;

/**
 * Initiates player interaction with a chest.
 */
public class Chest extends TileScript implements SpellTarget, ChestOptionsCallback, LockOrTrap
{
	private final TileScript chestContents;
	private final PercentageTable<Trap> traps;
	private final String mazeVariable;
	private final String northTexture, southTexture, eastTexture, westTexture;
	private MazeScript preScript;
	
	private final EngineObject engineObject;
	private Trap currentTrap;

	/*-------------------------------------------------------------------------*/
	/**
	 * @param chestContents
	 * 	What happens when this chest is opened
	 * @param traps
	 * @param mazeVariable
* 	The maze variable storing the state of this chest
	 * @param preScript
	 */
	public Chest(
		TileScript chestContents,
		PercentageTable<Trap> traps,
		String mazeVariable,
		String northTexture,
		String southTexture,
		String eastTexture,
		String westTexture, 
		MazeScript preScript)
	{
		this.preScript = preScript;
		this.chestContents = chestContents;
		this.traps = traps;
		this.mazeVariable = mazeVariable;
		this.northTexture = northTexture;
		this.southTexture = southTexture;
		this.eastTexture = eastTexture;
		this.westTexture = westTexture;
		
		this.engineObject = new EngineObject(
			null,
			Database.getInstance().getMazeTexture(northTexture).getTexture(), 
			Database.getInstance().getMazeTexture(southTexture).getTexture(), 
			Database.getInstance().getMazeTexture(eastTexture).getTexture(), 
			Database.getInstance().getMazeTexture(westTexture).getTexture(),
			0,
			false,
			null,
			null,
			EngineObject.Alignment.BOTTOM);

		this.engineObject.setMouseClickScript(
			new MouseClickScript()
			{
				@Override
				public void initialise(Map map)
				{
				}

				@Override
				public void execute(Map map)
				{
					Maze.getInstance().processPlayerAction(
						TileScript.PlayerAction.LOCKS,
						Maze.getInstance().getFacing());
				}

				@Override
				public int getMaxDist()
				{
					return 1;
				}
			}
		);
	}
	
	/*-------------------------------------------------------------------------*/
	public void initialise(Maze maze, Point tile, int tileIndex)
	{
		if (!State.EMPTY.equalsIgnoreCase(MazeVariables.get(this.mazeVariable)))
		{
			engineObject.setTileIndex(tileIndex);
			maze.addObject(engineObject);
		}
	}

	/*-------------------------------------------------------------------------*/
	public java.util.List<MazeEvent> handlePlayerAction(
		Maze maze, Point tile, int facing, int playerAction)
	{
		if (playerAction != PlayerAction.LOCKS)
		{
			return null;
		}

		if (State.EMPTY.equalsIgnoreCase(MazeVariables.get(this.mazeVariable)))
		{
			return null;
		}

		maze.encounterChest(this);
		return null;
	}
	
	/*-------------------------------------------------------------------------*/
	public java.util.List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		if (preScript != null &&
			!State.EMPTY.equalsIgnoreCase(MazeVariables.get(this.mazeVariable)))
		{
			return preScript.getEvents();
		}
		else
		{
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public TileScript getChestContents()
	{
		return chestContents;
	}

	public String getMazeVariable()
	{
		return mazeVariable;
	}

	public PercentageTable<Trap> getTraps()
	{
		return traps;
	}

	@Override
	public Trap getCurrentTrap()
	{
		return currentTrap;
	}

	@Override
	public List<MazeEvent> executeTrapDisarmed()
	{
		return executeChestContents();
	}

	public void refreshCurrentTrap()
	{
		this.currentTrap = traps.getRandomItem();
	}

	public EngineObject getEngineObject()
	{
		return engineObject;
	}

	public String getEastTexture()
	{
		return eastTexture;
	}

	public String getNorthTexture()
	{
		return northTexture;
	}

	public String getSouthTexture()
	{
		return southTexture;
	}

	public String getWestTexture()
	{
		return westTexture;
	}

	public MazeScript getPreScript()
	{
		return preScript;
	}

	public void setPreScript(MazeScript preScript)
	{
		this.preScript = preScript;
	}
	
	/*-------------------------------------------------------------------------*/
	public void setState(String chestState)
	{
		MazeVariables.set(this.mazeVariable, chestState);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public java.util.List<MazeEvent> springTrap()
	{
		Maze maze = Maze.getInstance();

		java.util.List<MazeEvent> result = new ArrayList<MazeEvent>();

		Point tile = maze.getTile();
		int facing = maze.getFacing();

		if (getTraps() != null &&
			getTraps().size()>0 &&
			getCurrentTrap() != null)
		{
			result.addAll(
				getCurrentTrap().getPayload().execute(
					maze, tile, tile, facing));
		}

		if (maze.getCurrentCombat() != null)
		{
			// something has started a combat
			//leave the chest basically unopened
			return result;
		}

		// check if trap has killed the party
		result.add(new CheckPartyStatusEvent());

		if (Maze.getInstance().getParty() != null && Maze.getInstance().getParty().numAlive() > 0)
		{
			result.addAll(executeChestContents());
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean isTrapped()
	{
		return getCurrentTrap() != null;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean isLocked()
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public BitSet getPickLockToolsRequired()
	{
		return new BitSet(8);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean canSpellPick()
	{
		return false;
	}

	@Override
	public int[] getPickLockDifficulty()
	{
		return new int[8];
	}

	@Override
	public void setLockState(String state)
	{
		// no op
	}

	@Override
	public String getLockState()
	{
		return Portal.State.UNLOCKED;
	}

	@Override
	public boolean canManualPick()
	{
		return false;
	}

	@Override
	public BitSet getAlreadyLockPicked()
	{
		return new BitSet();
	}

	@Override
	public int[] getPickLockToolStatus()
	{
		return new int[8];
	}

	@Override
	public int getHitPointCostToForceLock()
	{
		return 0;
	}

	@Override
	public boolean canForceOpen()
	{
		return false;
	}

	@Override
	public int getResistForceOpen()
	{
		return 0;
	}

	@Override
	public String getKeyItem()
	{
		// not supported yet
		return null;
	}

	@Override
	public boolean isConsumeKeyItem()
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public java.util.List<MazeEvent> executeChestContents()
	{
		Maze maze = Maze.getInstance();

		java.util.List<MazeEvent> result = new ArrayList<MazeEvent>();

		Point tile = maze.getTile();
		int facing = maze.getFacing();

		// chest opens
		result.add(new MazeScriptEvent("_OPEN_CHEST_"));

		// chest contents
		result.addAll(getChestContents().execute(maze, tile, tile, facing));

		// clean up and back to movement
		result.add(new SetChestStateEvent(this, Chest.State.EMPTY));
		result.add(new RemoveObjectEvent(getEngineObject()));
		result.add(new SetStateEvent(maze, Maze.State.MOVEMENT));

		return result;
	}

	@Override
	public String getDisplayName()
	{
		return "Treasure Chest";
	}

	@Override
	public int getModifier(Stats.Modifier modifier)
	{
		return 0;
	}

	/*-------------------------------------------------------------------------*/
	public static class State
	{
		public static final String UNTOUCHED = "untouched";
		public static final String EMPTY = "empty";
	}
}
