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

import java.awt.*;
import java.util.*;
import java.util.List;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.script.LockOrTrap;
import mclachlan.maze.util.MazeException;

/**
 * A Portal can represent a door, a portcullis or a one-way wall.
 */
public class Portal implements LockOrTrap
{
	/** The maze variable that records the state of this portal */
	private String mazeVariable;

	/** false if this portal can only be passed in the direction
	 * {@link Portal#from} -> {@link Portal#to} */
	private boolean twoWay;

	/** the initial state of this portal, from {@link State} */
	private String initialState;

	/** The map coords on one side of this portal */
	private Point from;
	
	/** The facing that the player must step from the FROM tile*/
	private int fromFacing;

	/** The map coords on the other side of this portal */
	private Point to;
	
	/** The facing that the player must step from the TO tile*/
	private int toFacing;

	/** true if this portal can be forced open */
	private boolean canForce;

	/** true if this portal can be picked manually */
	private boolean canPick;

	/** true if this portal can be picked by a spell or magic item */
	private boolean canSpellPick;

	/** the cost in HP to force this portal open */
	private int hitPointCostToForce;

	/** a modifier to attempts to force this portal */
	private int resistForce;

	/** Indices correspond to Tool constants; value indicates difficulty with that
	 *  tool.  0 indicates not required. */
	private int[] difficulty;

	/** Indices indicate if a given tool is required (1) or not (0). */
	private BitSet required;

	/** The name of the item that can be used as a key to open this door */
	private String keyItem;

	/** True if unlocking the portal consumes the key item */
	private boolean consumeKeyItem;
	
	/** A maze script to execute on activation of this portal */
	private String mazeScript;

	//
	// Volatile data
	//

	private int[] toolStatus = new int[8];
	private BitSet picked = new BitSet(8);

	/*-------------------------------------------------------------------------*/
	public Portal(
		String mazeVariable,
		String initialState,
		Point from,
		int fromFacing,
		Point to,
		int toFacing,
		boolean twoWay,
		boolean canForce,
		boolean canPick,
		boolean canSpellPick,
		int hitPointCostToForce,
		int resistForce,
		int[] difficulty,
		BitSet required,
		String keyItem,
		boolean consumeKeyItem,
		String mazeScript)
	{
		this.mazeVariable = mazeVariable;
		this.initialState = initialState;
		this.from = from;
		this.fromFacing = fromFacing;
		this.to = to;
		this.toFacing = toFacing;
		this.twoWay = twoWay;
		this.canForce = canForce;
		this.canPick = canPick;
		this.canSpellPick = canSpellPick;
		this.hitPointCostToForce = hitPointCostToForce;
		this.resistForce = resistForce;
		this.difficulty = difficulty;
		this.required = required;
		this.keyItem = keyItem;
		this.consumeKeyItem = consumeKeyItem;
		this.mazeScript = mazeScript;

		for (int i = 0; i < toolStatus.length; i++)
		{
			if (getRequired() != null && getRequired().get(i))
			{
				toolStatus[i] = Trap.InspectionResult.PRESENT;
			}
			else
			{
				toolStatus[i] = Trap.InspectionResult.NOT_PRESENT;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public String getState()
	{
		if (this.mazeVariable == null || "".equals(this.mazeVariable))
		{
			// assume that portals without a maze variable are always unlocked
			if (State.UNLOCKED.equals(this.initialState))
			{
				return this.initialState;
			}
			else
			{
				throw new MazeException("Portal with no maze variable configured " +
					"has an initial state that is not UNLOCKED. ["+this.getFrom()+
					"] ["+this.getTo()+"] ["+this.initialState+"]");
			}
		}

		String result = MazeVariables.get(this.mazeVariable);
		if (result == null)
		{
			result = initialState;
			MazeVariables.set(this.mazeVariable, result);
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void setState(String state)
	{
		MazeVariables.set(this.mazeVariable, state);
	}

	/*-------------------------------------------------------------------------*/
	public boolean canForce()
	{
		return canForce;
	}

	public boolean canPick()
	{
		return canPick;
	}

	public boolean canSpellPick()
	{
		return canSpellPick;
	}

	@Override
	public int[] getPickLockDifficulty()
	{
		return getDifficulty();
	}

	@Override
	public void setLockState(String state)
	{
		setState(state);
	}

	@Override
	public boolean canManualPick()
	{
		return canPick();
	}

	@Override
	public BitSet getAlreadyLockPicked()
	{
		return getPicked();
	}

	@Override
	public int[] getPickLockToolStatus()
	{
		return getToolStatus();
	}

	public int getHitPointCostToForceLock()
	{
		return hitPointCostToForce;
	}

	@Override
	public boolean canForceOpen()
	{
		return canForce();
	}

	public int getResistForceOpen()
	{
		return resistForce;
	}

	public int[] getDifficulty()
	{
		return difficulty;
	}

	public BitSet getRequired()
	{
		return required;
	}

	public String getKeyItem()
	{
		return keyItem;
	}

	public boolean consumeKeyItem()
	{
		return consumeKeyItem;
	}

	public boolean isConsumeKeyItem()
	{
		return consumeKeyItem;
	}

	public Point getFrom()
	{
		return from;
	}

	public String getInitialState()
	{
		return initialState;
	}

	public String getMazeVariable()
	{
		return mazeVariable;
	}

	public Point getTo()
	{
		return to;
	}

	public boolean isTwoWay()
	{
		return twoWay;
	}

	public int getFromFacing()
	{
		return fromFacing;
	}

	public void setFromFacing(int fromFacing)
	{
		this.fromFacing = fromFacing;
	}

	public int getToFacing()
	{
		return toFacing;
	}

	public void setToFacing(int toFacing)
	{
		this.toFacing = toFacing;
	}

	public void setCanForce(boolean canForce)
	{
		this.canForce = canForce;
	}

	public void setCanPick(boolean canPick)
	{
		this.canPick = canPick;
	}

	public void setCanSpellPick(boolean canSpellPick)
	{
		this.canSpellPick = canSpellPick;
	}

	public void setConsumeKeyItem(boolean consumeKeyItem)
	{
		this.consumeKeyItem = consumeKeyItem;
	}

	public void setDifficulty(int[] difficulty)
	{
		this.difficulty = difficulty;
	}

	public void setFrom(Point from)
	{
		this.from = from;
	}

	public void setHitPointCostToForce(int hitPointCostToForce)
	{
		this.hitPointCostToForce = hitPointCostToForce;
	}

	public void setInitialState(String initialState)
	{
		this.initialState = initialState;
	}

	public void setKeyItem(String keyItem)
	{
		this.keyItem = keyItem;
	}

	public void setMazeVariable(String mazeVariable)
	{
		this.mazeVariable = mazeVariable;
	}

	public void setRequired(BitSet required)
	{
		this.required = required;
	}

	public void setResistForce(int resistForce)
	{
		this.resistForce = resistForce;
	}

	public void setTo(Point to)
	{
		this.to = to;
	}

	public void setTwoWay(boolean twoWay)
	{
		this.twoWay = twoWay;
	}

	public String getMazeScript()
	{
		return mazeScript;
	}

	public void setMazeScript(String mazeScript)
	{
		this.mazeScript = mazeScript;
	}

	public int[] getToolStatus()
	{
		return toolStatus;
	}

	public void setToolStatus(int[] toolStatus)
	{
		this.toolStatus = toolStatus;
	}

	public BitSet getPicked()
	{
		return picked;
	}

	public void setPicked(BitSet picked)
	{
		this.picked = picked;
	}

	@Override
	public Trap getCurrentTrap()
	{
		return null;
	}

	@Override
	public List<MazeEvent> executeTrapDisarmed()
	{
		return new ArrayList<MazeEvent>();
	}

	@Override
	public List<MazeEvent> springTrap()
	{
		return new ArrayList<MazeEvent>();
	}

	@Override
	public boolean isTrapped()
	{
		return false;
	}

	@Override
	public boolean isLocked()
	{
		return State.LOCKED.equals(getState());
	}

	@Override
	public BitSet getPickLockToolsRequired()
	{
		return required;
	}

	/*-------------------------------------------------------------------------*/
	public static class State
	{
		public static final String LOCKED = "locked";
		public static final String UNLOCKED = "unlocked";
		public static final String WALL_LIKE = "wall-like";
	}

	/*-------------------------------------------------------------------------*/
	public static class ForceResult
	{
		public static final int SUCCESS = 1;
		public static final int FAILED_NO_DAMAGE = 2;
		public static final int FAILED_DAMAGE = 3;
	}
}
