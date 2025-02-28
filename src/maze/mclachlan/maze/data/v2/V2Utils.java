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
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
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
		Gson gson = getGsonReader();
		Type type = new TypeToken<List<Map>>()
		{
		}.getType();
		return gson.fromJson(new JsonReader(reader), type);
	}

	/*-------------------------------------------------------------------------*/
	public static Map getMap(BufferedReader reader)
	{
		Gson gson = getGsonReader();
		Type type = new TypeToken<Map>()
		{
		}.getType();
		return gson.fromJson(new JsonReader(reader), type);
	}

	/*-------------------------------------------------------------------------*/
	private static Gson getGsonReader()
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(String.class, new Utf8StringTypeAdapter());
		Gson gson = builder.create();
		return gson;
	}

	/*-------------------------------------------------------------------------*/
	public static String getJson(List<Map> list)
	{
		GsonBuilder gsonBuilder = new GsonBuilder();
//		gsonBuilder.registerTypeAdapter(String.class, new UnicodeStringAdapter());
		Gson gson = gsonBuilder.setPrettyPrinting().create();
		Type type = new TypeToken<List<Map>>()
		{
		}.getType();
		return gson.toJson(list, type);
	}

	/*-------------------------------------------------------------------------*/
	public static void writeJson(List<Map> list,
		Writer writer) throws IOException
	{
		writer.write(getJson(list));
		writer.flush();
	}

	/*-------------------------------------------------------------------------*/
	public static String getJson(Map obj)
	{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Type type = new TypeToken<Map>()
		{
		}.getType();
		return gson.toJson(obj, type);
	}

	/*-------------------------------------------------------------------------*/
	public static void writeJson(Map obj, Writer writer) throws IOException
	{
		writer.write(getJson(obj));
		writer.flush();
	}

	/*-------------------------------------------------------------------------*/
	public static Map serialiseMap(Map<?, ?> map, V2SerialiserMap serialiser,
		Database db)
	{
		Map result = new HashMap();

		for (Map.Entry<?, ?> e : map.entrySet())
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
	public static Map deserialiseMap(Map<?, ?> map, V2SerialiserMap serialiser,
		Database db)
	{
		Map result = new HashMap();

		for (Map.Entry<?, ?> e : map.entrySet())
		{
			result.put(e.getKey(), serialiser.fromObject((Map<String, ?>)e.getValue(), db));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List serialiseList(List list, V2SerialiserMap serialiser,
		Database db)
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
	public static List deserialiseList(List list, V2SerialiserMap serialiser,
		Database db)
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

	static class UnicodeStringAdapter extends TypeAdapter<String>
	{
		@Override
		public void write(JsonWriter out, String value) throws IOException
		{
			if (value == null)
			{
				out.nullValue();
				return;
			}
			StringBuilder unicodeString = new StringBuilder();
			for (char c : value.toCharArray())
			{
//				if (c > 127)
//				{
//					unicodeString.append(String.format("\\u%04x", (int)c));
//				}
//				else
				{
					unicodeString.append(c);
				}
			}
			String value1 = unicodeString.toString();
			System.out.println("value1 = " + value1);
			out.value(value1);
		}

		@Override
		public String read(JsonReader in) throws IOException
		{
			return in.nextString();//.replaceAll("\\\\u", "\\u");
		}

		public static void main(String[] args) throws IOException
		{
			GsonBuilder builder = new GsonBuilder();
			builder.disableHtmlEscaping();
			builder.registerTypeAdapter(String.class, new UnicodeStringAdapter());
			Gson gson = builder.setPrettyPrinting().create();

			// Example usage
			Map<String, String> exampleMap = new HashMap<>();
			String value = "value with unicode: \u00A9. Der Mond flüstert mir Geheimnisse, doch sie sind nicht für sterbliche Ohren bestimmt!";
			System.out.println("value = " + value);
			exampleMap.put("key", value);

			Writer writer = new PrintWriter(System.out);
			gson.toJson(exampleMap, writer);
			writer.close();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static class Utf8StringTypeAdapter extends TypeAdapter<String>
	{
		@Override
		public void write(JsonWriter jsonWriter, String s) throws IOException
		{
			jsonWriter.value(s);
		}

		@Override
		public String read(JsonReader jsonReader) throws IOException
		{
			String s = jsonReader.nextString();
			return new String(s.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws IOException
	{
		Map<String, String> map = new HashMap<>();

		map.put("val", "\u00A9. Der Mond flüstert mir Geheimnisse, doch sie sind nicht für sterbliche Ohren bestimmt!");
		System.out.println("map = " + map);

		String json = V2Utils.getJson(map);
		System.out.println("json = " + json);

		Map map1 = V2Utils.getMap(new BufferedReader(new StringReader(json)));
		System.out.println("map1 = " + map1);
	}
}
