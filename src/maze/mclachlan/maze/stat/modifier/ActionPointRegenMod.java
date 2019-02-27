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

package mclachlan.maze.stat.modifier;

import java.util.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.Tile;
import mclachlan.maze.stat.ModifierValue;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;

/**
 *
 */
public class ActionPointRegenMod extends ModifierModification
{
	@Override
	public void getModification(UnifiedActor actor, List<ModifierValue> result)
	{
		// terrain type modifiers
		Tile currentTile = Maze.getInstance().getCurrentTile();
		if (currentTile != null)
		{
			Tile.TerrainType terrainType = currentTile.getTerrainType();

			switch (terrainType)
			{
				case FAKE:
					break;
				case URBAN:
					if (actor.getModifier(Stats.Modifier.ACTION_REGEN_URBAN) > 0)
					{
						result.add(new ModifierValue(
							StringUtil.getModifierName(Stats.Modifier.ACTION_REGEN_URBAN),
							actor.getModifier(Stats.Modifier.ACTION_REGEN_URBAN)));
					}
					break;
				case DUNGEON:
					if (actor.getModifier(Stats.Modifier.ACTION_REGEN_DUNGEON) > 0)
					{
						result.add(new ModifierValue(
							StringUtil.getModifierName(Stats.Modifier.ACTION_REGEN_DUNGEON),
							actor.getModifier(Stats.Modifier.ACTION_REGEN_DUNGEON)));
					}
					break;
				case WILDERNESS:
					if (actor.getModifier(Stats.Modifier.ACTION_REGEN_WILDERNESS) > 0)
					{
						result.add(new ModifierValue(
							StringUtil.getModifierName(Stats.Modifier.ACTION_REGEN_WILDERNESS),
							actor.getModifier(Stats.Modifier.ACTION_REGEN_WILDERNESS)));
					}
					break;
				case WASTELAND:
					if (actor.getModifier(Stats.Modifier.ACTION_REGEN_WASTELAND) > 0)
					{
						result.add(new ModifierValue(
							StringUtil.getModifierName(Stats.Modifier.ACTION_REGEN_WASTELAND),
							actor.getModifier(Stats.Modifier.ACTION_REGEN_WASTELAND)));
					}
					break;
			}
		}

		// cursed power
		if (actor.getModifier(Stats.Modifier.CURSED_POWER) > 0 &&
			actor.isHexed())
		{
			result.add(new ModifierValue(
				StringUtil.getModifierName(Stats.Modifier.CURSED_POWER),
				10));

		}
	}
}
