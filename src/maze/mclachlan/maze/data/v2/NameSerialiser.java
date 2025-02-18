package mclachlan.maze.data.v2;

import java.util.function.*;
import mclachlan.maze.data.Database;

/**
 *
 */
public class NameSerialiser<E extends V2DataObject> implements V2SerialiserObject<E>
{
	private final Function<String, E> dbGetter;
	private final String defaultKey;
	private final E defaultValue;

	public NameSerialiser(Function<String, E> dbGetter)
	{
		this(dbGetter, null, null);
	}

	public NameSerialiser(Function<String, E> dbGetter, String defaultKey, E defaultValue)
	{
		this.dbGetter = dbGetter;
		this.defaultKey = defaultKey;
		this.defaultValue = defaultValue;
	}

	@Override
	public Object toObject(E e, Database db)
	{
		return e.getName();
	}

	@Override
	public E fromObject(Object obj, Database db)
	{
		if (obj == null)
		{
			return null;
		}

		String key = (String)obj;

		if (key.equals(defaultKey))
		{
			return defaultValue;
		}
		else
		{
			return dbGetter.apply(key);
		}
	}
}
