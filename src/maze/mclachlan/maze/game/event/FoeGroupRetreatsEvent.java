package mclachlan.maze.game.event;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.FoeGroup;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class FoeGroupRetreatsEvent extends MazeEvent
{
	private final Combat combat;
	private final FoeGroup foeGroup;

	public FoeGroupRetreatsEvent(Combat combat, FoeGroup foeGroup)
	{
		this.combat = combat;
		this.foeGroup = foeGroup;
	}

	@Override
	public List<MazeEvent> resolve()
	{
		int index = combat.getFoes().indexOf(foeGroup);

		if (index < 0)
		{
			throw new MazeException("invalid foe group "+foeGroup.getDescription());
		}

		if (index == 0)
		{
			// nothing to do
			return null;
		}

		// otherwise, reorder the foe groups
		combat.retreatFoeGroup(foeGroup);

		return Collections.singletonList(new FlavourTextEvent(foeGroup.getDescription() + " retreats!"));
	}
}
