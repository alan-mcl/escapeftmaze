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

package mclachlan.maze.campaign.def.stat.magic;

import mclachlan.maze.data.Database;
import mclachlan.maze.stat.ItemTemplate;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class GreaterAmmoStashSpellResult extends AmmoStashSpellResult
{
	@Override
	protected ItemTemplate getAmmoItemTemplate(ItemTemplate.AmmoType ammoType,
		ItemTemplate ammoTemplate)
	{
		switch (ammoType)
		{
			case ARROW: ammoTemplate = Database.getInstance().getItemTemplate("Shrike Arrow");
				break;
			case BOLT: ammoTemplate = Database.getInstance().getItemTemplate("Fletched Bolt");
				break;
			case STONE: ammoTemplate = Database.getInstance().getItemTemplate("Spike Stone");
				break;
			case SELF: // can't do anything here
				break;
			case SHOT: ammoTemplate = Database.getInstance().getItemTemplate("Silver Bullet");
				break;
			case STAR: ammoTemplate = Database.getInstance().getItemTemplate("Hiraken");
				break;
			case DART: ammoTemplate = Database.getInstance().getItemTemplate("Poison Dart");
				break;
			case JAVELIN: ammoTemplate = Database.getInstance().getItemTemplate("Harpoon");
				break;
			case HAMMER: ammoTemplate = Database.getInstance().getItemTemplate("Dwarven Thrower");
				break;
			case AXE: ammoTemplate = Database.getInstance().getItemTemplate("Hurlblat");
				break;
			case KNIFE: ammoTemplate = Database.getInstance().getItemTemplate("Bite Dagger");
				break;
			default:
				throw new MazeException(""+ammoType);
		}

		return ammoTemplate;
	}
}
