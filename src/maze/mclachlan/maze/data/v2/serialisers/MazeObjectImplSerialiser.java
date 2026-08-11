package mclachlan.maze.data.v2.serialisers;

import java.lang.reflect.Method;
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
			// Copy out of the field-ordered TreeMap before adding TYPE_KEY; that
			// comparator treats unknown keys as equal (see ReflectiveSerialiser).
			Map result = new HashMap<>(map);
			result.put(TYPE_KEY, typeKey);
			return result;
		}
		else
		{
			ReflectiveSerialiser serialiser = new ReflectiveSerialiser(
				t.getClass(),
				fieldsSupportedBy(t.getClass()));
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
				Class<?> clazz = Class.forName(className);
				ReflectiveSerialiser serialiser = new ReflectiveSerialiser(
					clazz,
					fieldsSupportedBy(clazz));
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

	/*-------------------------------------------------------------------------*/
	/**
	 * IMPL classes only persist {@code defaultFields} that they actually expose
	 * (getter+setter). Pure custom scripts without those properties stay fieldless.
	 */
	private String[] fieldsSupportedBy(Class<?> clazz)
	{
		if (defaultFields == null || defaultFields.length == 0)
		{
			return new String[0];
		}

		List<String> supported = new ArrayList<>();
		for (String field : defaultFields)
		{
			if (hasAccessor(clazz, "get", field) || hasAccessor(clazz, "is", field))
			{
				if (hasSetter(clazz, field))
				{
					supported.add(field);
				}
			}
		}
		return supported.toArray(new String[0]);
	}

	/*-------------------------------------------------------------------------*/
	private static boolean hasAccessor(Class<?> clazz, String prefix, String field)
	{
		try
		{
			clazz.getMethod(prefix + capitalize(field));
			return true;
		}
		catch (NoSuchMethodException e)
		{
			return false;
		}
	}

	/*-------------------------------------------------------------------------*/
	private static boolean hasSetter(Class<?> clazz, String field)
	{
		String name = "set" + capitalize(field);
		for (Method m : clazz.getMethods())
		{
			if (m.getName().equals(name) && m.getParameterTypes().length == 1)
			{
				return true;
			}
		}
		return false;
	}

	/*-------------------------------------------------------------------------*/
	private static String capitalize(String field)
	{
		return Character.toUpperCase(field.charAt(0)) + field.substring(1);
	}
}
