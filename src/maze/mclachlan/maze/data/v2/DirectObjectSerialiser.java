package mclachlan.maze.data.v2;

import mclachlan.maze.data.Database;

/**
 * Just pass the Object straight to GSON to sort out. Useful for maps of strings
 * and similar.
 */
public class DirectObjectSerialiser<T> implements V2SerialiserObject<T>
{

	@Override
	public Object toObject(T o, Database db)
	{
		return o;
	}

	@Override
	public T fromObject(Object obj, Database db)
	{
		return (T)obj;
	}
}
