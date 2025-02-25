package mclachlan.maze.data.v2.serialisers;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.V2SerialiserMap;
import mclachlan.maze.data.v2.V2SerialiserObject;

/**
 *
 */
class MapSerialiser<K, V> implements V2SerialiserMap<Map<K, V>>
{
	private final V2SerialiserObject<K> keySerialiser;
	private final V2SerialiserObject<V> valueSerialiser;

	public MapSerialiser(V2SerialiserObject<K> keySerialiser, V2SerialiserObject<V> valueSerialiser)
	{
		this.keySerialiser = keySerialiser;
		this.valueSerialiser = valueSerialiser;
	}

	@Override
	public Map<Object, Object> toObject(Map<K, V> caches, Database db)
	{
		Map<Object, Object> result = new TreeMap<>();

		caches.forEach((k, v) -> result.put(keySerialiser.toObject(k, db), valueSerialiser.toObject(v, db)));

		return result;
	}

	@Override
	public Map<K, V> fromObject(Object obj, Database db)
	{
		Map<String, Object> map = (Map<String, Object>)obj;
		Map<K, V> result = new HashMap<>();

		map.forEach((k, v) -> result.put(keySerialiser.fromObject(k, db), valueSerialiser.fromObject(v, db)));

		return result;
	}
}
