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

package mclachlan.maze.util;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.FoeTemplate;
import mclachlan.maze.stat.ObjectAnimations;

/**
 *
 */
public class UpdateFoes
{
	public static void main(String[] args) throws Exception
	{
		System.out.println("...");

		V2Loader loader = new V2Loader();
		V2Saver saver = new V2Saver();
		Campaign campaign = Maze.getStubCampaign();
/*		List<Campaign> campaigns = new ArrayList<>(Database.getCampaigns().values());
		// haxor to get the arena campaign
		for (Campaign c : campaigns)
		{
			if (c.getName().equals("default"))
			{
				campaign = c;
				break;
			}
		}*/

		Database db = new Database(loader, saver, campaign);

//		dbv1.initImpls();
//		dbv1.initCaches(null);

		db.initImpls();
		db.initCaches(null);

		int count = 0;

		Map<String, FoeTemplate> foes = db.getFoeTemplates();

		for (String s : foes.keySet())
		{
			FoeTemplate foeTemplate = foes.get(s);

			ObjectAnimations spriteAnimations = foeTemplate.getSpriteAnimations();
			if (spriteAnimations != null && spriteAnimations.getAnimationScripts().size() > 0)
			{
				count++;
				System.out.println(foeTemplate.getName() + ": " + spriteAnimations.getAnimationScripts());

				db.getObjectAnimations().put(spriteAnimations.getName(), spriteAnimations);
			}
		}

		for (ObjectAnimations oa : db.getObjectAnimations().values())
		{
			oa.setCampaign(campaign.getName());
		}

		System.out.println(db.getObjectAnimations());
//		db.saveObjectAnimations(db.getObjectAnimations(), campaign);
		db.saveFoeTemplates(db.getFoeTemplates(), campaign);

//		saver.saveFoeTemplates(foes);
		System.out.println("count = [" + count + "]");
	}
}