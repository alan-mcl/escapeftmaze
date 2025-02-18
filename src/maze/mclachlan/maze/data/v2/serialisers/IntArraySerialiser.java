package mclachlan.maze.data.v2.serialisers;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.V2SerialiserObject;

/**
 *
 */
public class IntArraySerialiser implements V2SerialiserObject<int[]>
{
	@Override
	public Object toObject(int[] ints, Database db)
	{
		ArrayList<String> result = new ArrayList<>();

		for (int i : ints)
		{
			result.add(String.valueOf(i));
		}

		return result;
	}

	@Override
	public int[] fromObject(Object obj, Database db)
	{
		ArrayList<String> list = (ArrayList<String>)obj;

		int[] result = new int[list.size()];

		for (int i = 0; i < list.size(); i++)
		{
			result[i] = Integer.valueOf(list.get(i));
		}

		return result;
	}
}
