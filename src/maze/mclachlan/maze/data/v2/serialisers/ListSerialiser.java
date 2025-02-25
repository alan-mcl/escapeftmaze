package mclachlan.maze.data.v2.serialisers;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.V2SerialiserObject;

/**
 *
 */
public class ListSerialiser<T> implements V2SerialiserObject<List<T>>
{
	private final V2SerialiserObject<T> elementSerialiser;

	public ListSerialiser(V2SerialiserObject<T> elementSerialiser)
	{
		this.elementSerialiser = elementSerialiser;
	}

	@Override
	public Object toObject(List<T> list, Database db)
	{
		if (list == null)
		{
			return null;
		}

		List<Object> result = new ArrayList<>();

		for (T t : list)
		{
			Object map = elementSerialiser.toObject(t, db);
			result.add(map);
		}

//		list.forEach(obj -> result.add(elementSerialiser.toObject(obj, db)));

		return result;
	}

	@Override
	public List<T> fromObject(Object obj, Database db)
	{
		if (obj == null)
		{
			return null;
		}

		ArrayList<Object> list = (ArrayList<Object>)obj;

		ArrayList<T> result = new ArrayList<>();

		list.forEach(e -> result.add(elementSerialiser.fromObject(e, db)));

		return result;
	}
}
