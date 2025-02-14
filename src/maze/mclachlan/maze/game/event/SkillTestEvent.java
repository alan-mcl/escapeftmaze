package mclachlan.maze.game.event;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.magic.ValueList;

/**
 *
 */
public class SkillTestEvent extends MazeEvent
{
	private final Stats.Modifier keyModifier;
	private final ValueList skill;
	private final ValueList successValue;
	private final String successScript, failureScript;

	/*-------------------------------------------------------------------------*/
	public SkillTestEvent(Stats.Modifier keyModifier, ValueList skill, ValueList successValue, String successScript,
		String failureScript)
	{
		this.keyModifier = keyModifier;
		this.skill = skill;
		this.successValue = successValue;
		this.successScript = successScript;
		this.failureScript = failureScript;
	}

	/*-------------------------------------------------------------------------*/

	public ValueList getSkill()
	{
		return skill;
	}

	public ValueList getSuccessValue()
	{
		return successValue;
	}

	public String getSuccessScript()
	{
		return successScript;
	}

	public String getFailureScript()
	{
		return failureScript;
	}

	public Stats.Modifier getKeyModifier()
	{
		return keyModifier;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public List<MazeEvent> resolve()
	{
		UnifiedActor source;
		if (keyModifier != null)
		{
			source = Maze.getInstance().getParty().getActorWithBestModifier(keyModifier);
		}
		else
		{
			source = Maze.getInstance().getParty().getRandomPlayerCharacter();
		}

		boolean success = GameSys.getInstance().skillTest(source, null, skill, successValue);

		if (success && successScript != null)
		{
			return Database.getInstance().getMazeScript(successScript).getEvents();
		}
		else if (failureScript != null)
		{
			return Database.getInstance().getMazeScript(failureScript).getEvents();
		}
		else
		{
			return null;
		}
	}
}
