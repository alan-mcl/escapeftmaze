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

import java.awt.Point;
import java.util.Arrays;
import java.util.BitSet;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.MouseClickScript;
import mclachlan.crusader.Tile;
import mclachlan.crusader.Wall;
import mclachlan.maze.map.Portal;

/**
 * Deep-clones map elements via domain object copy methods.
 */
public class MapElementCloner
{
	/*-------------------------------------------------------------------------*/
	public static mclachlan.maze.map.Tile cloneMazeTile(mclachlan.maze.map.Tile tile)
	{
		return tile.copyTile();
	}

	/*-------------------------------------------------------------------------*/
	public static Tile cloneCrusaderTile(Tile tile)
	{
		return tile.copyTile();
	}

	/*-------------------------------------------------------------------------*/
	public static Wall cloneWall(Wall wall)
	{
		return wall.copyWall();
	}

	/*-------------------------------------------------------------------------*/
	public static EngineObject cloneObject(EngineObject object)
	{
		return object.copyObject();
	}

	/*-------------------------------------------------------------------------*/
	public static MouseClickScript cloneMouseClickScript(MouseClickScript script)
	{
		return script == null ? null : script.copyScript();
	}

	/*-------------------------------------------------------------------------*/
	public static Portal clonePortal(Portal portal)
	{
		if (portal == null)
		{
			return null;
		}

		Point from = portal.getFrom();
		Point to = portal.getTo();
		int[] difficulty = portal.getDifficulty();
		BitSet required = portal.getRequired();

		Portal copy = new Portal(
			portal.getMazeVariable(),
			portal.getInitialState(),
			from == null ? null : new Point(from),
			portal.getFromFacing(),
			to == null ? null : new Point(to),
			portal.getToFacing(),
			portal.isTwoWay(),
			portal.isCanForce(),
			portal.isCanPick(),
			portal.isCanSpellPick(),
			portal.getHitPointCostToForce(),
			portal.getResistForce(),
			difficulty == null ? null : Arrays.copyOf(difficulty, difficulty.length),
			required == null ? null : (BitSet) required.clone(),
			portal.getKeyItem(),
			portal.isConsumeKeyItem(),
			portal.getMazeScript(),
			portal.getStateChangeScript());

		if (portal.getToolStatus() != null)
		{
			copy.setToolStatus(Arrays.copyOf(portal.getToolStatus(), portal.getToolStatus().length));
		}
		if (portal.getPicked() != null)
		{
			copy.setPicked((BitSet) portal.getPicked().clone());
		}

		return copy;
	}
}
