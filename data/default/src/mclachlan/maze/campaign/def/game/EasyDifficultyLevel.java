package mclachlan.maze.campaign.def.game;

import mclachlan.maze.game.DifficultyLevel;
import mclachlan.maze.stat.CurMax;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.stat.Stats;

/**
 *
 */
public class EasyDifficultyLevel extends DifficultyLevel
{
	@Override
	public String getDisplayName()
	{
		return "Storyteller Mode";
	}

	@Override
	public String getDescription()
	{
		return "\"Gather around the campfire, friends, and listen while I tell you a tale of adventure in the far off places of the Maze.\"\n~Lorelei, druidess of Janpur Sanctuary";
	}

	@Override
	public String getImage()
	{
		return "screen/easy_difficulty";
	}

	/*-------------------------------------------------------------------------*/
	public void foeIsSpawned(Foe foe)
	{
		int value = 1;

		// +1 hits, stealth and magic
		decMaximum(foe.getHitPoints(), value);
		decMaximum(foe.getActionPoints(), value);
		decMaximum(foe.getMagicPoints(), value);

		// +1 attack and defence
		foe.incModifier(Stats.Modifier.ATTACK, value);
		foe.incModifier(Stats.Modifier.DEFENCE, value);

		// +5 on all resistances
		foe.incModifier(Stats.Modifier.RESIST_ENERGY, value*5);
		foe.incModifier(Stats.Modifier.RESIST_BLUDGEONING, value*5);
		foe.incModifier(Stats.Modifier.RESIST_PIERCING, value*5);
		foe.incModifier(Stats.Modifier.RESIST_SLASHING, value*5);
		foe.incModifier(Stats.Modifier.RESIST_FIRE, value*5);
		foe.incModifier(Stats.Modifier.RESIST_WATER, value*5);
		foe.incModifier(Stats.Modifier.RESIST_AIR, value*5);
		foe.incModifier(Stats.Modifier.RESIST_EARTH, value*5);
		foe.incModifier(Stats.Modifier.RESIST_MENTAL, value*5);
	}

	/*-------------------------------------------------------------------------*/
	private void decMaximum(CurMax cm, int value)
	{
		cm.incMaximum(-value);
		if (cm.getMaximum() < 1)
		{
			cm.setMaximum(1);
		}
		cm.setCurrentToMax();
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public int getBaseLootMultiplier()
	{
		return 15;
	}
}
