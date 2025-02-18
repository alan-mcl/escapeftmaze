package mclachlan.maze.data.v2;

import java.util.*;
import java.util.function.*;
import mclachlan.maze.data.Database;

/**
 * This is effectively a foreign key lookup on a list of names.
 */
public class NameListSerialiser<E extends V2DataObject> implements V2SerialiserObject<java.util.List>
{
	private final Function<String, E> dbGetter;
	private final NameSerialiser<E> serialiser;


	public NameListSerialiser(Function<String, E> dbGetter)
	{
		this.dbGetter = dbGetter;
		this.serialiser = new NameSerialiser<E>(dbGetter);
	}

	@Override
	public Object toObject(List list, Database db)
	{
		return V2Utils.serialiseList((java.util.List)list, serialiser);
	}

	@Override
	public java.util.List fromObject(Object obj, Database db)
	{
		if (obj == null)
		{
			return null;
		}
		return (java.util.List)V2Utils.deserialiseList((java.util.List)obj, serialiser);
	}
}
