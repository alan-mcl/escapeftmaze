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

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.game.event.ModifySuppliesEvent;
import mclachlan.maze.map.script.GrantGoldEvent;
import mclachlan.maze.map.script.GrantItemsEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.ItemTemplate;
import mclachlan.maze.stat.PlayerCharacter;

/**
 *
 */
public abstract class TileScript
{
	// these are optional conditions of execution common to every TileScript

	/**
	 * If non-null, this var will be used to ensure that the script is only
	 * executed once.
	 */
	private String executeOnceMazeVariable;

	/**
	 * If non-null, this var determines for which facings the script executes.
	 * Bit indices are the same as {@link mclachlan.crusader.CrusaderEngine.Facing} constants.
	 */
	private BitSet facings;

	/**
	 * Set to true if the script should execute again when the player rotates
	 * on the tile.
	 */
	private boolean reexecuteOnSameTile;

	public static final List<MazeEvent> PREVENT_ACTION = new ArrayList<>();

	/*-------------------------------------------------------------------------*/
	/**
	 * This method is called when the zone is initialised, this tile script must
	 * take any actions needed to get the zone in the required state.
	 * <p>
	 * This default implementation does nothing. 
	 * 
	 * @param maze
	 * 	The maze to execute on
	 */
	public void initialise(Maze maze, Point tile, int tileIndex)
	{
		
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * @param maze
	 * 	The maze to execute on
	 * @param tile
	 * 	The tile that the player is on.
	 * @param previousTile
	 * 	The tile the player was on last move.
	 * @param facing
	 * 	The current facing of the player, a constant from 
	 * 	{@link mclachlan.crusader.CrusaderEngine.Facing}
	 * @param playerAction
	 * @return
	 * 	true if this script should execute, false otherwise
	 */
	public boolean shouldExecute(Maze maze, Point tile, Point previousTile, int facing, int playerAction)
	{
		if (executeOnceMazeVariable != null && 
			MazeVariables.get(executeOnceMazeVariable) != null )
		{
			// script has already executed once
			return false;
		}
		
		if (facings != null)
		{
			if (!(facings.get(facing)))
			{
				// player is not facing the right way
				return false;
			}
		}
		
		if (!reexecuteOnSameTile && tile.equals(previousTile) 
			&& playerAction != PlayerAction.SEARCH
			&& playerAction != PlayerAction.LOCKS)
		{
			// player has simply rotated
			return false;
		}
		
		// otherwise this script is going to execute.
		if (executeOnceMazeVariable != null)
		{
			// might as well set this var now
			MazeVariables.set(executeOnceMazeVariable, "1");
		}
		return true;
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * This default implementation does nothing.
	 *
	 * @param maze
	 * 	The maze to execute on
	 * @param tile
	 * 	The tile that the player is on.
	 * @param previousTile
	 * 	The tile the player was on last move.
	 * @param facing
	 * 	The current facing of the player, a constant from CrusaderEngine.Facing.
	 * @see mclachlan.crusader.CrusaderEngine.Facing
	 */ 
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * This default implementation does nothing.
	 *
	 * @param maze
	 * 	The maze to execute on.
	 * @param tile
 * 	The tile that the player is on.
	 * @param facing
* 	The current facing of the player, a constant from
* 	{@link mclachlan.crusader.CrusaderEngine.Facing}.
	 * @param playerAction
* 	The action that the player has taken, a constant from {@link mclachlan.maze.map.TileScript.PlayerAction}
	 */
	public List<MazeEvent> handlePlayerAction(
		Maze maze,
		Point tile,
		int facing,
		int playerAction)
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * This default implementation does nothing.
	 *
	 * @param maze
	 * 	The maze to execute on
	 * @param tile
	 * 	The tile that the player is on
	 * @param facing
	 * 	The current facing of the player, a constant from
	 * 	{@link mclachlan.crusader.CrusaderEngine.Facing}.
	 * @param item
	 * 	The item involved
	 * @param user
	 * @return
	 */
	public List<MazeEvent> handleUseItem(
		Maze maze,
		Point tile,
		int facing,
		Item item, PlayerCharacter user)
	{
		return null;
	}
	
	/*-------------------------------------------------------------------------*/
	public String getExecuteOnceMazeVariable()
	{
		return executeOnceMazeVariable;
	}

	public void setExecuteOnceMazeVariable(String executeOnceMazeVariable)
	{
		this.executeOnceMazeVariable = executeOnceMazeVariable;
	}

	public BitSet getFacings()
	{
		return facings;
	}

	public void setFacings(BitSet facings)
	{
		this.facings = facings;
	}

	public boolean isReexecuteOnSameTile()
	{
		return reexecuteOnSameTile;
	}

	public void setReexecuteOnSameTile(boolean reexecuteOnSameTile)
	{
		this.reexecuteOnSameTile = reexecuteOnSameTile;
	}

	/*-------------------------------------------------------------------------*/
	// static utility methods!  bah, get thee gone!
	/*-------------------------------------------------------------------------*/
	/**
	 * @param items
	 * 	The list of loot items.
	 * @return
	 * 	An array of events to give all the stuff to the player.
	 */
	public static List<MazeEvent> getLootingEvents(java.util.List<Item> items)
	{
		int totalGold = 0;
		int totalSupplies = 0;

		if (Maze.getInstance().getUserConfig().isAutoAddConsumables())
		{
			totalGold = extractGold(items);
			totalSupplies = extractSupplies(items);
		}

		ArrayList<MazeEvent> result = new ArrayList<MazeEvent>();

		if (totalGold > 0)
		{
			result.add(new GrantGoldEvent(totalGold));
		}
		if (totalSupplies > 0)
		{
			result.add(new ModifySuppliesEvent(totalSupplies));
		}
		if (items.size() > 0)
		{
			result.add(new GrantItemsEvent(items));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param loot
	 * 	The list of loot items. Money items will be removed.
	 * @return
	 * 	The amount of gold contained in the list.
	 */
	public static int extractGold(java.util.List<Item> loot)
	{
		int result = 0;
		ListIterator<Item> lit = loot.listIterator();
		while (lit.hasNext())
		{
			Item item = lit.next();
			if (item.getType() == ItemTemplate.Type.MONEY)
			{
				lit.remove();
				result += item.applyConversionRate();
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param loot
	 * 	The list of loot items. Supply items will be removed
	 * @return
	 * 	The amount of supplies contained in the list.
	 */
	public static int extractSupplies(java.util.List<Item> loot)
	{
		int result = 0;
		ListIterator<Item> lit = loot.listIterator();
		while (lit.hasNext())
		{
			Item item = lit.next();
			if (item.getType() == ItemTemplate.Type.SUPPLIES)
			{
				lit.remove();
				result += item.applyConversionRate();
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	protected List<MazeEvent> getList(MazeEvent... events)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();
		result.addAll(Arrays.asList(events));
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static class PlayerAction
	{
		public static final int SEARCH = 1;
		public static final int LOCKS = 2;
		public static final int REST = 3;
	}
}
