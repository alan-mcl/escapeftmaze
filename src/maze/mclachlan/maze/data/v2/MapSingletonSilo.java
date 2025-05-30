/*
 * This file is part of Brewday.
 *
 * Brewday is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Brewday is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Brewday.  If not, see https://www.gnu.org/licenses.
 */

package mclachlan.maze.data.v2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import mclachlan.maze.data.Database;

/**
 *
 */
public class MapSingletonSilo implements V2SiloSingleton<Map>
{
	private V2SerialiserObject valueSerialiser;

	public MapSingletonSilo()
	{
	}

	public MapSingletonSilo(V2SerialiserObject valueSerialiser)
	{
		this.valueSerialiser = valueSerialiser;
	}

	@Override
	public void validate(Map map, Database db)
	{
		if (valueSerialiser != null)
		{
			for (Object key : map.keySet())
			{
				valueSerialiser.toObject(map.get(key), db);
			}
		}
	}

	@Override
	public Map load(BufferedReader reader,
		Database db) throws IOException
	{
		Map map = V2Utils.getMap(reader);

		if (valueSerialiser != null)
		{
			map.forEach((k, v) -> map.put(k, valueSerialiser.fromObject(v, db)));
		}

		return map;
	}

	@Override
	public void save(BufferedWriter writer, Map obj, Database db) throws IOException
	{
		Map map = obj;

		Map<Object, Object> temp = new HashMap<>();

		if (valueSerialiser != null)
		{
			for (Object key : map.keySet())
			{
				Object value = valueSerialiser.toObject(map.get(key), db);
				temp.put(key, value);
			}

//			map.forEach((k, v) -> map.put(k, valueSerialiser.toObject(v, db)));
			V2Utils.writeJson(temp, writer);
		}
		else
		{
			V2Utils.writeJson(map, writer);
		}
	}
}
