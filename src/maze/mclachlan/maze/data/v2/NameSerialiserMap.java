package mclachlan.maze.data.v2;

import java.util.*;
import java.util.function.*;
import mclachlan.maze.data.Database;

/**
 *
 */
public class NameSerialiserMap<E extends V2DataObject> implements V2SerialiserMap<E>
{
	public static final String NAME = "NAME";

	private final Function<String, E> dbGetter;
	private final String defaultKey;
	private final E defaultValue;

	public NameSerialiserMap(Function<String, E> dbGetter)
	{
		this(dbGetter, null, null);
	}

	public NameSerialiserMap(Function<String, E> dbGetter, String defaultKey, E defaultValue)
	{
		this.dbGetter = dbGetter;
		this.defaultKey = defaultKey;
		this.defaultValue = defaultValue;
	}

	@Override
	public Map<String, String> toObject(E e, Database db)
	{
		if (e == null)
		{
			return null;
		}

		Map<String, String> result = new HashMap<>();
		result.put(NAME, e.getName());
		return result;
	}

	@Override
	public E fromObject(Object obj, Database db)
	{
		if (obj == null)
		{
			return null;
		}

		Map<String, String> map = (Map<String, String>)obj;

		String key = map.get(NAME);

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
