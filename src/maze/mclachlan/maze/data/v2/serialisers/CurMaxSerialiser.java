package mclachlan.maze.data.v2.serialisers;

import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1CurMax;
import mclachlan.maze.data.v2.V2SerialiserObject;
import mclachlan.maze.stat.CurMax;

/**
 *
 */
public class CurMaxSerialiser implements V2SerialiserObject<CurMax>
{
	@Override
	public Object toObject(CurMax curMax, Database db)
	{
		if (curMax == null)
		{
			return null;
		}

		return V1CurMax.toString(curMax);
	}

	@Override
	public CurMax fromObject(Object obj, Database db)
	{
		if (obj == null)
		{
			return null;
		}

		return V1CurMax.fromString((String)obj);
	}
}
