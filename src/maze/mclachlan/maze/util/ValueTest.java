package mclachlan.maze.util;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.magic.*;
import mclachlan.maze.stat.magic.Value.SCALE;

import static mclachlan.maze.stat.magic.Value.SCALE.*;

/**
 *
 */
public class ValueTest
{
	public static void main(String[] args) throws Exception
	{
		V1Loader loader = new V1Loader();
		V1Saver saver = new V1Saver();
		Database db = new Database(loader, saver, Maze.getStubCampaign());

		List<Value> values = new ArrayList<>();

		values.add((getBaseValue(1, NONE, null, false)));
		values.add((getBaseValue(1, SCALE_WITH_CLASS_LEVEL, "Shaman", false)));
		values.add((getBaseValue(1, SCALE_WITH_CHARACTER_LEVEL, null, false)));
		values.add((getBaseValue(1, SCALE_WITH_MODIFIER, StringUtil.getModifierName(Stats.Modifier.BRAWN), false)));
		values.add((getBaseValue(1, SCALE_WITH_PARTY_SIZE, null, false)));
		values.add((getDiceValue(Dice.d6,1, NONE, null, false)));
		values.add((getDiceValue(Dice.d6,1, SCALE_WITH_CLASS_LEVEL, "Shaman", false)));
		values.add((getDiceValue(Dice.d6,1, SCALE_WITH_CHARACTER_LEVEL, null, false)));
		values.add((getDiceValue(Dice.d6,1, SCALE_WITH_MODIFIER, StringUtil.getModifierName(Stats.Modifier.BRAWN), false)));
		values.add((getDiceValue(Dice.d6,1, SCALE_WITH_PARTY_SIZE, null, false)));
		values.add((getModifierValue(Stats.Modifier.BRAWN,1, NONE, null, false)));
		values.add((getModifierValue(Stats.Modifier.BRAWN,1, SCALE_WITH_CLASS_LEVEL, "Shaman", false)));
		values.add((getModifierValue(Stats.Modifier.BRAWN,1, SCALE_WITH_CHARACTER_LEVEL, null, false)));
		values.add((getModifierValue(Stats.Modifier.BRAWN,1, SCALE_WITH_MODIFIER, StringUtil.getModifierName(Stats.Modifier.BRAWN), false)));
		values.add((getModifierValue(Stats.Modifier.BRAWN,1, SCALE_WITH_PARTY_SIZE, null, false)));
		values.add((getMagicPresentValue(MagicSys.MagicColour.BLUE,1, NONE, null, false)));
		values.add((getMagicPresentValue(MagicSys.MagicColour.BLUE,1, SCALE_WITH_CLASS_LEVEL, "Shaman", false)));
		values.add((getMagicPresentValue(MagicSys.MagicColour.BLUE,1, SCALE_WITH_CHARACTER_LEVEL, null, false)));
		values.add((getMagicPresentValue(MagicSys.MagicColour.BLUE,1, SCALE_WITH_MODIFIER, StringUtil.getModifierName(Stats.Modifier.BRAWN), false)));
		values.add((getMagicPresentValue(MagicSys.MagicColour.BLUE,1, SCALE_WITH_PARTY_SIZE, null, false)));

		for (Value v : values)
		{
			desc(new ValueList(v));
		}

		System.out.println();

		for (int i=0; i<20; i++)
		{
			ValueList vl = new ValueList();
			int nr = new Dice(1, 4, 1).roll("test");

			for (int j=0; j<nr; j++)
			{
				vl.add(values.get(new Dice(1, values.size(), -1).roll("test")));
			}

			desc(vl);
		}
	}

	/*-------------------------------------------------------------------------*/
	private static Value getBaseValue(int i, SCALE scale, String ref, boolean negate)
	{
		Value result = new Value(i, scale);
		result.setNegate(negate);
		result.setReference(ref);
		return result;
	}

	private static Value getDiceValue(Dice d, int i, SCALE scale, String ref, boolean negate)
	{
		Value result = new DiceValue(d);
		result.setValue(i);
		result.setScaling(scale);
		result.setNegate(negate);
		result.setReference(ref);
		return result;
	}

	private static Value getModifierValue(Stats.Modifier mod, int i, SCALE scale, String ref, boolean negate)
	{
		Value result = new ModifierValue(mod);
		result.setValue(i);
		result.setScaling(scale);
		result.setNegate(negate);
		result.setReference(ref);
		return result;
	}

	private static Value getMagicPresentValue(int col, int i, SCALE scale, String ref, boolean negate)
	{
		Value result = new MagicPresentValue(col);
		result.setValue(i);
		result.setScaling(scale);
		result.setNegate(negate);
		result.setReference(ref);
		return result;
	}

	private static void desc(ValueList vl)
	{
		System.out.println(StringUtil.descValue(vl));
	}
}
