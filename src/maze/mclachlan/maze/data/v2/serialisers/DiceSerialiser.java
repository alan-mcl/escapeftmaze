package mclachlan.maze.data.v2.serialisers;

import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1Dice;
import mclachlan.maze.data.v2.V2SerialiserObject;
import mclachlan.maze.stat.Dice;

/**
 *
 */
public class DiceSerialiser implements V2SerialiserObject<Dice>
{
	@Override
	public Object toObject(Dice dice, Database db)
	{
		if (dice == null)
		{
			return null;
		}
		return V1Dice.toString(dice);
	}

	@Override
	public Dice fromObject(Object obj, Database db)
	{
		if (obj == null)
		{
			return null;
		}
		return V1Dice.fromString((String)obj);
	}
}
