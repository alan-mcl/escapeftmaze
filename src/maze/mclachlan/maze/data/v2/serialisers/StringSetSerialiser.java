package mclachlan.maze.data.v2.serialisers;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.V2SerialiserObject;

/**
 *
 */
public class StringSetSerialiser implements V2SerialiserObject<Set>
{
	@Override
	public Object toObject(Set set, Database db)
	{
		return new ArrayList(set);
	}

	@Override
	public Set fromObject(Object obj, Database db)
	{
		ArrayList list = (ArrayList)obj;

		Set result = new HashSet();
		result.addAll(list);
		return result;
	}
}
