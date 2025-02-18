package mclachlan.maze.data.v2.serialisers;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.V2SerialiserObject;

/**
 *
 */
public class ListSerialiser implements V2SerialiserObject<List>
{
	private final V2SerialiserObject elementSerialiser;

	public ListSerialiser(V2SerialiserObject elementSerialiser)
	{
		this.elementSerialiser = elementSerialiser;
	}

	@Override
	public Object toObject(List list, Database db)
	{
		if (list == null)
		{
			return null;
		}

		List result = new ArrayList();

		list.forEach(obj -> result.add(elementSerialiser.toObject(obj, db)));

		return result;
	}

	@Override
	public List fromObject(Object obj, Database db)
	{
		if (obj == null)
		{
			return null;
		}

		ArrayList list = (ArrayList)obj;

		ArrayList result = new ArrayList();

		list.forEach(e -> result.add(elementSerialiser.fromObject(e, db)));

		return result;
	}
}
