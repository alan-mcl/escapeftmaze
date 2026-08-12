package mclachlan.maze.data.v2.serialisers;

import mclachlan.maze.data.Database;
import mclachlan.maze.data.codec.DiceCodec;
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
		return DiceCodec.toString(dice);
	}

	@Override
	public Dice fromObject(Object obj, Database db)
	{
		if (obj == null)
		{
			return null;
		}
		return DiceCodec.fromString((String)obj);
	}
}
