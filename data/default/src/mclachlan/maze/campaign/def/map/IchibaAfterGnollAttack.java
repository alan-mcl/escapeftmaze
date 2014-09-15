package mclachlan.maze.campaign.def.map;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.campaign.def.npc.Imogen;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.npc.Npc;
import mclachlan.maze.stat.npc.NpcManager;
import mclachlan.maze.stat.npc.NpcFaction;

/**
 *
 */
public class IchibaAfterGnollAttack extends TileScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		if (MazeVariables.getBoolean(Imogen.QUEST_3_COMPLETE))
		{
			// the adventurers inn
			MazeVariables.set("ichiba.city.portal.12", Portal.State.LOCKED);
			// the temple of danaos
			MazeVariables.set("ichiba.city.portal.16", Portal.State.LOCKED);
			// glaucus' shop
			MazeVariables.set("ichiba.city.portal.17", Portal.State.LOCKED);
			// COC
			MazeVariables.set("ichiba.city.portal.6", Portal.State.LOCKED);
			// red ear leaves town 
			Npc redEar = NpcManager.getInstance().getNpc("Red Ear");
			redEar.setTile(new Point(1,1));

			// turn the COC hostile
			NpcFaction coc = NpcManager.getInstance().getNpcFaction("Ichiba Chamber Of Commerce");
			coc.setAttitude(-1);

			// turn the GSC hostile
			NpcFaction gsc = NpcManager.getInstance().getNpcFaction("Ichiba GSC");
			gsc.setAttitude(-1);

			return getList(
				new FlavourTextEvent("Stepping out into the city, the cries " +
					"and clash of battle fill your ears!"),
				new FlavourTextEvent("Smoke from burning buildings drifts on " +
					"the air, and all is chaos. You see gnolls - lots of gnolls. " +
					"Even as you watch, more scale the walls and leap keening " +
					"onto the rooftops below.",
					MazeEvent.Delay.WAIT_ON_CLICK,
					true),
				new FlavourTextEvent("In the streets a running battle rages. " +
					"Ichiba is no soft town, and the inhabitants are " +
					"fighting back, street by street and building by building.",
					MazeEvent.Delay.WAIT_ON_CLICK,
					true),
				new FlavourTextEvent("\nThere is little cohesion in the defenders - " +
					"gnomes, leonals and men each look their own. You see " +
					"few of the steel armoured soldiers of the Chamber of " +
					"Commerce in the battle..."));
		}
		else
		{
			return null;
		}
	}
}