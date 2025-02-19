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
	private Stats.Modifier keyModifier;
	private ValueList skill;
	private ValueList successValue;
	private String successScript, failureScript;

	public SkillTestEvent()
	{
	}

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

		SkillTestEvent that = (SkillTestEvent)o;

		if (getKeyModifier() != that.getKeyModifier())
		{
			return false;
		}
		if (getSkill() != null ? !getSkill().equals(that.getSkill()) : that.getSkill() != null)
		{
			return false;
		}
		if (getSuccessValue() != null ? !getSuccessValue().equals(that.getSuccessValue()) : that.getSuccessValue() != null)
		{
			return false;
		}
		if (getSuccessScript() != null ? !getSuccessScript().equals(that.getSuccessScript()) : that.getSuccessScript() != null)
		{
			return false;
		}
		return getFailureScript() != null ? getFailureScript().equals(that.getFailureScript()) : that.getFailureScript() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getKeyModifier() != null ? getKeyModifier().hashCode() : 0;
		result = 31 * result + (getSkill() != null ? getSkill().hashCode() : 0);
		result = 31 * result + (getSuccessValue() != null ? getSuccessValue().hashCode() : 0);
		result = 31 * result + (getSuccessScript() != null ? getSuccessScript().hashCode() : 0);
		result = 31 * result + (getFailureScript() != null ? getFailureScript().hashCode() : 0);
		return result;
	}
}
