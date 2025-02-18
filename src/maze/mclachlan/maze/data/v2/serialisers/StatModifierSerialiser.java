package mclachlan.maze.data.v2.serialisers;

import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1StatModifier;
import mclachlan.maze.data.v2.V2SerialiserObject;
import mclachlan.maze.stat.StatModifier;

/**
 *
 */
public class StatModifierSerialiser implements V2SerialiserObject<StatModifier>
{
	@Override
	public Object toObject(StatModifier statModifier, Database db)
	{
		return V1StatModifier.toString(statModifier);
	}

	@Override
	public StatModifier fromObject(Object obj, Database db)
	{
		return V1StatModifier.fromString((String)obj);
	}
}
