package mclachlan.maze.data.v2.serialisers;

import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1CurMaxSub;
import mclachlan.maze.data.v2.V2SerialiserObject;
import mclachlan.maze.stat.CurMaxSub;

/**
 *
 */
public class CurMaxSubSerialiser implements V2SerialiserObject<CurMaxSub>
{
	@Override
	public Object toObject(CurMaxSub curMax, Database db)
	{
		if (curMax == null)
		{
			return null;
		}

		return V1CurMaxSub.toString(curMax);
	}

	@Override
	public CurMaxSub fromObject(Object obj, Database db)
	{
		if (obj == null)
		{
			return null;
		}

		return V1CurMaxSub.fromString((String)obj);
	}
}
