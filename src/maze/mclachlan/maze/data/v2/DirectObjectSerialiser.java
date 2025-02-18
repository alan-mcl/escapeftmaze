package mclachlan.maze.data.v2;

import mclachlan.maze.data.Database;

/**
 * Just pass the Object straight to GSON to sort out. Useful for maps of strings
 * and similar.
 */
public class DirectObjectSerialiser implements V2SerialiserObject<Object>
{

	@Override
	public Object toObject(Object o, Database db)
	{
		return o;
	}

	@Override
	public Object fromObject(Object obj, Database db)
	{
		return obj;
	}
}
