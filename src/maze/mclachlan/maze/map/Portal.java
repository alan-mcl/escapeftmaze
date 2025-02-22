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

	/** A tile script to execute on state change */
	private TileScript stateChangeScript;

	//
	// Volatile data
	//

	private int[] toolStatus = new int[8];
	private BitSet picked = new BitSet(8);

	public Portal()
	{
	}

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
		String mazeScript,
		TileScript stateChangeScript)
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
		this.stateChangeScript = stateChangeScript;

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
	public void initialise(long turnNr, Maze maze)
	{
		if (stateChangeScript != null)
		{
			stateChangeScript.initialise(maze, this.from, maze.getCurrentZone().getTileIndex(this.from));
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

		if (stateChangeScript != null)
		{
			Maze.getInstance().appendEvents(
				stateChangeScript.execute(
					Maze.getInstance(), this.from, this.from, Maze.getInstance().getFacing()));
		}
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
	public String getLockState()
	{
		return getState();
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

	public TileScript getStateChangeScript()
	{
		return stateChangeScript;
	}

	public void setStateChangeScript(TileScript stateChangeScript)
	{
		this.stateChangeScript = stateChangeScript;
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
		return new ArrayList<>();
	}

	@Override
	public List<MazeEvent> springTrap()
	{
		return new ArrayList<>();
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

	public boolean isCanForce()
	{
		return canForce;
	}

	public boolean isCanPick()
	{
		return canPick;
	}

	public boolean isCanSpellPick()
	{
		return canSpellPick;
	}

	public int getHitPointCostToForce()
	{
		return hitPointCostToForce;
	}

	public int getResistForce()
	{
		return resistForce;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof Portal))
		{
			return false;
		}

		Portal portal = (Portal)o;

		if (isTwoWay() != portal.isTwoWay())
		{
			return false;
		}
		if (getFromFacing() != portal.getFromFacing())
		{
			return false;
		}
		if (getToFacing() != portal.getToFacing())
		{
			return false;
		}
		if (isCanForce() != portal.isCanForce())
		{
			return false;
		}
		if (isCanPick() != portal.isCanPick())
		{
			return false;
		}
		if (isCanSpellPick() != portal.isCanSpellPick())
		{
			return false;
		}
		if (getHitPointCostToForce() != portal.getHitPointCostToForce())
		{
			return false;
		}
		if (getResistForce() != portal.getResistForce())
		{
			return false;
		}
		if (isConsumeKeyItem() != portal.isConsumeKeyItem())
		{
			return false;
		}
		if (getMazeVariable() != null ? !getMazeVariable().equals(portal.getMazeVariable()) : portal.getMazeVariable() != null)
		{
			return false;
		}
		if (getInitialState() != null ? !getInitialState().equals(portal.getInitialState()) : portal.getInitialState() != null)
		{
			return false;
		}
		if (getFrom() != null ? !getFrom().equals(portal.getFrom()) : portal.getFrom() != null)
		{
			return false;
		}
		if (getTo() != null ? !getTo().equals(portal.getTo()) : portal.getTo() != null)
		{
			return false;
		}
		if (!Arrays.equals(getDifficulty(), portal.getDifficulty()))
		{
			return false;
		}
		if (getRequired() != null ? !getRequired().equals(portal.getRequired()) : portal.getRequired() != null)
		{
			return false;
		}
		if (getKeyItem() != null ? !getKeyItem().equals(portal.getKeyItem()) : portal.getKeyItem() != null)
		{
			return false;
		}
		if (getMazeScript() != null ? !getMazeScript().equals(portal.getMazeScript()) : portal.getMazeScript() != null)
		{
			return false;
		}
		if (getStateChangeScript() != null ? !getStateChangeScript().equals(portal.getStateChangeScript()) : portal.getStateChangeScript() != null)
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = getMazeVariable() != null ? getMazeVariable().hashCode() : 0;
		result = 31 * result + (isTwoWay() ? 1 : 0);
		result = 31 * result + (getInitialState() != null ? getInitialState().hashCode() : 0);
		result = 31 * result + (getFrom() != null ? getFrom().hashCode() : 0);
		result = 31 * result + getFromFacing();
		result = 31 * result + (getTo() != null ? getTo().hashCode() : 0);
		result = 31 * result + getToFacing();
		result = 31 * result + (isCanForce() ? 1 : 0);
		result = 31 * result + (isCanPick() ? 1 : 0);
		result = 31 * result + (isCanSpellPick() ? 1 : 0);
		result = 31 * result + getHitPointCostToForce();
		result = 31 * result + getResistForce();
		result = 31 * result + Arrays.hashCode(getDifficulty());
		result = 31 * result + (getRequired() != null ? getRequired().hashCode() : 0);
		result = 31 * result + (getKeyItem() != null ? getKeyItem().hashCode() : 0);
		result = 31 * result + (isConsumeKeyItem() ? 1 : 0);
		result = 31 * result + (getMazeScript() != null ? getMazeScript().hashCode() : 0);
		result = 31 * result + (getStateChangeScript() != null ? getStateChangeScript().hashCode() : 0);
		return result;
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
