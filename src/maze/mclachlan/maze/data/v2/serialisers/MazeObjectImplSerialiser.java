package mclachlan.maze.data.v2.serialisers;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.ReflectiveSerialiser;
import mclachlan.maze.data.v2.V2Exception;
import mclachlan.maze.data.v2.V2SerialiserMap;
import mclachlan.maze.util.MazeException;

/**
 * Serialiser for the common Maze data object default + impl pattern
 */
public class MazeObjectImplSerialiser<T> implements V2SerialiserMap<T>
{
	private static final String IMPL = "IMPL";
	private static final String TYPE_KEY = "TYPE_KEY";
	private final Map<String, V2SerialiserMap<T>> serialisers = new HashMap<>();
	private String[] defaultFields;

	public MazeObjectImplSerialiser(
		Map<Class, V2SerialiserMap<T>> serialiserMap,
		String... defaultFields)
	{
		this.defaultFields = defaultFields;
		serialiserMap.forEach((k, v) -> serialisers.put(k.getName(), v));
	}

	@Override
	public Map toObject(T t, Database db)
	{
		if (t == null)
		{
			return null;
		}

		String typeKey = t.getClass().getName();
		if (serialisers.containsKey(typeKey))
		{
			Map map = serialisers.get(typeKey).toObject(t, db);
			map.put(TYPE_KEY, typeKey);
			return map;
		}
		else
		{
			ReflectiveSerialiser serialiser = new ReflectiveSerialiser(t.getClass(), defaultFields);
			Map result = serialiser.toObject(t, db);
			result.put(IMPL, typeKey);
			return result;
		}
	}

	@Override
	public T fromObject(Object obj, Database db)
	{
		if (obj == null)
		{
			return null;
		}

		Map<String, ?> map = (Map<String, ?>)obj;

		if (map.containsKey(IMPL))
		{
			String className = (String)map.get(IMPL);
			try
			{
				ReflectiveSerialiser serialiser = new ReflectiveSerialiser(Class.forName(className), defaultFields);
				return (T)serialiser.fromObject(obj, db);
			}
			catch (Exception e)
			{
				throw new MazeException(e);
			}
		}
		else if (map.containsKey(TYPE_KEY))
		{
			Object key = map.get(TYPE_KEY);
			V2SerialiserMap<T> serialiser = serialisers.get(key.toString());
			return serialiser.fromObject(map, db);
		}
		else
		{
			throw new V2Exception("unknown type: ["+map+"]");
		}
	}

	public Map<String, V2SerialiserMap<T>> getSerialisers()
	{
		return serialisers;
	}
}
