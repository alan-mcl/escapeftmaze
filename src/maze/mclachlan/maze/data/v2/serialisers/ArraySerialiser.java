package mclachlan.maze.data.v2.serialisers;

import java.lang.reflect.Array;
import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.V2SerialiserObject;

/**
 *
 */
public class ArraySerialiser<T> implements V2SerialiserObject<T[]>
{
	private final Class clazz;
	private final V2SerialiserObject serialiser;

	public ArraySerialiser(Class clazz, V2SerialiserObject serialiser)
	{
		this.clazz = clazz;
		this.serialiser = serialiser;
	}

	@Override
	public Object toObject(T[] objects, Database db)
	{
		if (objects == null)
		{
			return null;
		}

		ArrayList<Object> result = new ArrayList<>();

		for (Object obj : objects)
		{
			result.add(serialiser.toObject(obj, db));
		}

		return result;
	}

	@Override
	public T[] fromObject(Object obj, Database db)
	{
		if (obj == null)
		{
			return null;
		}

		ArrayList<Object> list = (ArrayList<Object>)obj;
		ArrayList<T> result = new ArrayList<>();

		for (Object object : list)
		{
			result.add((T)serialiser.fromObject(object, db));
		}

		T[] array = (T[])Array.newInstance(clazz, result.size());
		return result.toArray(array);
	}
}
