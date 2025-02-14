package mclachlan.maze.map;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.SkillTestEvent;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.magic.ValueList;

/**
 *
 */
public class SkillTest extends TileScript
{
	private final Stats.Modifier keyModifier;
	private final ValueList skill;
	private final ValueList successValue;
	private final String successScript, failureScript;


	public SkillTest(Stats.Modifier keyModifier, ValueList skill,
		ValueList successValue, String successScript, String failureScript)
	{
		this.keyModifier = keyModifier;
		this.skill = skill;
		this.successValue = successValue;
		this.successScript = successScript;
		this.failureScript = failureScript;
	}

	@Override
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile,
		int facing)
	{
		return List.of(new SkillTestEvent(keyModifier, skill, successValue, successScript, failureScript));
	}

	public Stats.Modifier getKeyModifier()
	{
		return keyModifier;
	}

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
}
