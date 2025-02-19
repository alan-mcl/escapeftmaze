package mclachlan.maze.campaign.def.script;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.SetMazeVariableEvent;
import mclachlan.maze.stat.npc.ChangeNpcLocationEvent;
import mclachlan.maze.stat.npc.Npc;
import mclachlan.maze.stat.npc.NpcManager;

/**
 *
 */
public class RhysDeath extends MazeEvent
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		ArrayList<MazeEvent> result = new ArrayList<>();

		Npc kepnasha = NpcManager.getInstance().getNpc("Kepnasha Minion Of Usark");

		result.add(new SetMazeVariableEvent("imogen.quest.4.complete", "true"));
		result.add(new ChangeNpcLocationEvent(kepnasha, new Point(36, 3), "Ichiba City"));

		return result;
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		return o != null && getClass() == o.getClass();
	}

}
