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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.*;
import mclachlan.maze.data.Database;

/**
 *
 */
public class V2Utils
{
	/*-------------------------------------------------------------------------*/
	public static List<Map> getObjects(BufferedReader reader)
	{
		Gson gson = new Gson();
		Type type = new TypeToken<List<Map>>(){}.getType();
		return gson.fromJson(new JsonReader(reader), type);
	}

	/*-------------------------------------------------------------------------*/
	public static Map getMap(BufferedReader reader)
	{
		Gson gson = new Gson();
		Type type = new TypeToken<Map>(){}.getType();
		return gson.fromJson(new JsonReader(reader), type);
	}

	/*-------------------------------------------------------------------------*/
	public static String getJson(List<Map> list)
	{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Type type = new TypeToken<List<Map>>(){}.getType();
		return gson.toJson(list, type);
	}

	/*-------------------------------------------------------------------------*/
	public static void writeJson(List<Map> list, Writer writer) throws IOException
	{
		writer.write(getJson(list));
		writer.flush();
	}

	/*-------------------------------------------------------------------------*/
	public static String getJson(Map obj)
	{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Type type = new TypeToken<Map>(){}.getType();
		return gson.toJson(obj, type);
	}

	/*-------------------------------------------------------------------------*/
	public static void writeJson(Map obj, Writer writer) throws IOException
	{
		writer.write(getJson(obj));
		writer.flush();
	}

	/*-------------------------------------------------------------------------*/
	public static Map serialiseMap(Map<?,?> map, V2SerialiserMap serialiser, Database db)
	{
		Map result = new HashMap();

		for (Map.Entry<?,?> e : map.entrySet())
		{
			if (e.getValue() != null)
			{
				Map value = serialiser.toObject(e.getValue(), db);
				if (value != null)
				{
					result.put(e.getKey(), value);
				}
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static Map deserialiseMap(Map<?, ?> map, V2SerialiserMap serialiser, Database db)
	{
		Map result = new HashMap();

		for (Map.Entry<?, ?> e : map.entrySet())
		{
			result.put(e.getKey(), serialiser.fromObject((Map<String, ?>)e.getValue(), db));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List serialiseList(List list, V2SerialiserMap serialiser, Database db)
	{
		List result = new ArrayList();

		for (Object item : list)
		{
			if (item != null)
			{
				result.add(serialiser.toObject(item, db));
			}
		}

		return result;
	}

	public static List serialiseList(List list, V2SerialiserObject serialiser)
	{
		List result = new ArrayList();

		for (Object item : list)
		{
			if (item != null)
			{
				result.add(serialiser.toObject(item, null));
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List deserialiseList(List list, V2SerialiserMap serialiser, Database db)
	{
		List result = new ArrayList();

		for (Object item : list)
		{
			result.add(serialiser.fromObject((Map)item, db));
		}

		return result;
	}

	public static List deserialiseList(List list, V2SerialiserObject serialiser)
	{
		List result = new ArrayList();

		for (Object item : list)
		{
			result.add(serialiser.fromObject(item, null));
		}

		return result;
	}
}
