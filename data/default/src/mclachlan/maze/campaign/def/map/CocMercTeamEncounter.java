package mclachlan.maze.campaign.def.map;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.campaign.def.npc.SirKay;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.EncounterActorsEvent;
import mclachlan.maze.map.script.FlavourTextEvent;

/**
 *
 */
public class CocMercTeamEncounter extends TileScript
{
	private static final String COC_MERC_TEAM_ENCOUNTER = "coc.merc.team.ambush";

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		if (MazeVariables.getBoolean(SirKay.QUEST_3_STARTED) &&
			!MazeVariables.getBoolean(SirKay.QUEST_3_COMPLETE))
		{
			return getList(
				new FlavourTextEvent("The trees here crowd close together, " +
					"perfect for...", MazeEvent.Delay.WAIT_ON_CLICK, true),
				new FlavourTextEvent("Ambush!", MazeEvent.Delay.WAIT_ON_CLICK, true),
				new FlavourTextEvent("Half a dozen armed figures spring from the " +
					"trees, led by a tall man wielding a large sword. He " +
					"brandishes a scrap of paper and shouts at you...", MazeEvent.Delay.WAIT_ON_CLICK, true),
				new FlavourTextEvent("WITH THE AUTHORITY OF THE ICHIBA CHAMBER OF " +
					"COMMERCE YOU ARE HEREBY ARRESTED AND SENTENCED TO DEATH!",
					MazeEvent.Delay.WAIT_ON_CLICK, true),
				new FlavourTextEvent("PREPARE TO DIE, SCUM!", MazeEvent.Delay.WAIT_ON_CLICK, false),
				new EncounterActorsEvent(COC_MERC_TEAM_ENCOUNTER, COC_MERC_TEAM_ENCOUNTER, null, null, null, null));
		}
		else
		{
			return null;
		}
	}
}