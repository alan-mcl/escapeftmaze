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
	private Stats.Modifier keyModifier;
	private ValueList skill;
	private ValueList successValue;
	private String successScript, failureScript;

	public SkillTest()
	{
	}

	/*-------------------------------------------------------------------------*/
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

	public void setKeyModifier(Stats.Modifier keyModifier)
	{
		this.keyModifier = keyModifier;
	}

	public void setSkill(ValueList skill)
	{
		this.skill = skill;
	}

	public void setSuccessValue(ValueList successValue)
	{
		this.successValue = successValue;
	}

	public void setSuccessScript(String successScript)
	{
		this.successScript = successScript;
	}

	public void setFailureScript(String failureScript)
	{
		this.failureScript = failureScript;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		if (!super.equals(o))
		{
			return false;
		}

		SkillTest skillTest = (SkillTest)o;

		if (getKeyModifier() != skillTest.getKeyModifier())
		{
			return false;
		}
		if (getSkill() != null ? !getSkill().equals(skillTest.getSkill()) : skillTest.getSkill() != null)
		{
			return false;
		}
		if (getSuccessValue() != null ? !getSuccessValue().equals(skillTest.getSuccessValue()) : skillTest.getSuccessValue() != null)
		{
			return false;
		}
		if (getSuccessScript() != null ? !getSuccessScript().equals(skillTest.getSuccessScript()) : skillTest.getSuccessScript() != null)
		{
			return false;
		}
		return getFailureScript() != null ? getFailureScript().equals(skillTest.getFailureScript()) : skillTest.getFailureScript() == null;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + (getKeyModifier() != null ? getKeyModifier().hashCode() : 0);
		result = 31 * result + (getSkill() != null ? getSkill().hashCode() : 0);
		result = 31 * result + (getSuccessValue() != null ? getSuccessValue().hashCode() : 0);
		result = 31 * result + (getSuccessScript() != null ? getSuccessScript().hashCode() : 0);
		result = 31 * result + (getFailureScript() != null ? getFailureScript().hashCode() : 0);
		return result;
	}
}
