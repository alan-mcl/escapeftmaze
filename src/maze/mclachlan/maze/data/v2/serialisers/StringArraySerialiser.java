package mclachlan.maze.data.v2.serialisers;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.V2SerialiserObject;

/**
 *
 */
public class StringArraySerialiser implements V2SerialiserObject<String[]>
{
	@Override
	public Object toObject(String[] strs, Database db)
	{
		if (strs == null)
		{
			return null;
		}

		return Arrays.asList(strs);
	}

	@Override
	public String[] fromObject(Object obj, Database db)
	{
		if (obj == null)
		{
			return null;
		}

		ArrayList<String> list = (ArrayList<String>)obj;
		return list.toArray(new String[0]);
	}
}
