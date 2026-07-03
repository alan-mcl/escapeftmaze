/*
 * Copyright (c) 2011 Alan McLachlan
 *
 * This file is part of Escape From The Maze.
 *
 * Escape From The Maze is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mclachlan.maze.data;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import mclachlan.maze.data.v2.SimpleMapSilo;
import mclachlan.maze.data.v2.V2Utils;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.data.v2.serialisers.V2SerialiserFactory.getColdStringSerialiser;

/**
 * Unified HotString and ColdStrings lookup for one campaign.
 */
public class TextRepository
{
	public static final String STRINGS_DIR = "strings/";
	public static final String COLD_DIR = STRINGS_DIR + "cold/";
	public static final String HOT_FILE_PREFIX = "strings-";
	public static final String MANIFEST_FILE = COLD_DIR + "manifest.json";

	private final String dbPath;
	private final Database db;

	private final Map<String, Map<String, String>> hotStrings = new HashMap<>();
	private final Set<String> missingHotNamespaces = new HashSet<>();
	private List<ColdStringManifestEntry> coldManifest;
	private final Map<String, Map<String, ColdString>> coldShards = new HashMap<>();
	private final Set<String> missingColdShards = new HashSet<>();

	public TextRepository(String dbPath, Database db)
	{
		this.dbPath = dbPath;
		this.db = db;
	}

	public void resetCaches()
	{
		hotStrings.clear();
		missingHotNamespaces.clear();
		coldManifest = null;
		coldShards.clear();
		missingColdShards.clear();
	}

	public String getHotString(String namespace, String key, boolean allowNull)
	{
		if (key != null && key.contains("reserved"))
		{
			return key;
		}

		if (namespace == null)
		{
			namespace = "strings";
		}

		Map<String, String> bundle = loadHotNamespace(namespace);
		if (bundle == null)
		{
			if (allowNull)
			{
				return null;
			}
			throw new MazeException("HotString namespace not found [" + namespace + "]");
		}

		String result = bundle.get(key);
		if (result == null && !allowNull)
		{
			throw new MazeException("Invalid HotString key [" + namespace + "][" + key + "]");
		}
		return result;
	}

	public boolean hasHotNamespace(String namespace)
	{
		if (missingHotNamespaces.contains(namespace))
		{
			return false;
		}
		if (hotStrings.containsKey(namespace))
		{
			return true;
		}
		File file = getHotStringFile(namespace);
		return file.exists();
	}

	public String getColdString(String key, boolean allowNull)
	{
		if (key != null && key.contains("reserved"))
		{
			return key;
		}

		String shard = resolveColdShard(key);
		if (shard == null)
		{
			if (allowNull)
			{
				return null;
			}
			throw new MazeException("No ColdStrings shard for key [" + key + "]");
		}

		Map<String, ColdString> shardMap = loadColdShard(shard);
		if (shardMap == null)
		{
			if (allowNull)
			{
				return null;
			}
			throw new MazeException("ColdStrings shard not found [" + shard + "]");
		}

		ColdString coldString = shardMap.get(key);
		if (coldString == null)
		{
			if (allowNull)
			{
				return null;
			}
			throw new MazeException("Invalid ColdString key [" + key + "]");
		}
		return coldString.getBody();
	}

	public Map<String, String> getHotNamespace(String namespace)
	{
		return loadHotNamespace(namespace);
	}

	public Map<String, ColdString> getColdShard(String shard)
	{
		return loadColdShard(shard);
	}

	public List<ColdStringManifestEntry> getColdManifest()
	{
		return loadColdManifest();
	}

	/**
	 * All ColdString keys from every shard listed in the manifest (loads shards as needed).
	 */
	public List<String> getAllColdStringKeys()
	{
		List<String> result = new ArrayList<>();
		for (ColdStringManifestEntry entry : loadColdManifest())
		{
			Map<String, ColdString> shard = loadColdShard(entry.getShard());
			if (shard != null)
			{
				result.addAll(shard.keySet());
			}
		}
		Collections.sort(result);
		return result;
	}

	public void deleteColdShardFile(String shard) throws IOException
	{
		File file = getColdShardFile(shard);
		if (file.exists() && !file.delete())
		{
			throw new MazeException("Could not delete ColdStrings shard [" + file + "]");
		}
		coldShards.remove(shard);
		missingColdShards.remove(shard);
	}

	public void saveHotNamespace(String namespace, Map<String, String> strings) throws IOException
	{
		File file = getHotStringFile(namespace);
		file.getParentFile().mkdirs();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8)))
		{
			V2Utils.writeJson(strings, writer);
		}
		hotStrings.put(namespace, new LinkedHashMap<>(strings));
		missingHotNamespaces.remove(namespace);
	}

	public void saveColdShard(String shard, Map<String, ColdString> strings) throws Exception
	{
		File file = getColdShardFile(shard);
		file.getParentFile().mkdirs();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8)))
		{
			new SimpleMapSilo<>(getColdStringSerialiser()).save(writer, strings, db);
		}
		coldShards.put(shard, new LinkedHashMap<>(strings));
		missingColdShards.remove(shard);
	}

	public void saveColdManifest(List<ColdStringManifestEntry> manifest) throws IOException
	{
		File file = new File(dbPath, MANIFEST_FILE);
		file.getParentFile().mkdirs();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8)))
		{
			V2Utils.writeJsonList(manifest, writer);
		}
		coldManifest = new ArrayList<>(manifest);
	}

	public static List<String> getHotNamespaces()
	{
		return List.of("ui", "event", "gamesys", "tips", "campaign");
	}

	private Map<String, String> loadHotNamespace(String namespace)
	{
		if (missingHotNamespaces.contains(namespace))
		{
			return null;
		}

		Map<String, String> cached = hotStrings.get(namespace);
		if (cached != null)
		{
			return cached;
		}

		File file = getHotStringFile(namespace);
		if (!file.exists())
		{
			missingHotNamespaces.add(namespace);
			return null;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8)))
		{
			Map raw = V2Utils.getMap(reader);
			Map<String, String> result = new LinkedHashMap<>();
			for (Object entry : raw.entrySet())
			{
				Map.Entry mapEntry = (Map.Entry)entry;
				result.put(String.valueOf(mapEntry.getKey()), String.valueOf(mapEntry.getValue()));
			}
			hotStrings.put(namespace, result);
			return result;
		}
		catch (IOException e)
		{
			throw new MazeException(e);
		}
	}

	private List<ColdStringManifestEntry> loadColdManifest()
	{
		if (coldManifest != null)
		{
			return coldManifest;
		}

		File file = new File(dbPath, MANIFEST_FILE);
		if (!file.exists())
		{
			coldManifest = List.of();
			return coldManifest;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8)))
		{
			coldManifest = V2Utils.getObjectList(reader, ColdStringManifestEntry.class);
			coldManifest.sort((a, b) -> Integer.compare(
				b.getPrefix().length(), a.getPrefix().length()));
			return coldManifest;
		}
		catch (IOException e)
		{
			throw new MazeException(e);
		}
	}

	private String resolveColdShard(String key)
	{
		for (ColdStringManifestEntry entry : loadColdManifest())
		{
			if (key.startsWith(entry.getPrefix()))
			{
				return entry.getShard();
			}
		}
		return null;
	}

	private Map<String, ColdString> loadColdShard(String shard)
	{
		if (missingColdShards.contains(shard))
		{
			return null;
		}

		Map<String, ColdString> cached = coldShards.get(shard);
		if (cached != null)
		{
			return cached;
		}

		File file = getColdShardFile(shard);
		if (!file.exists())
		{
			missingColdShards.add(shard);
			return null;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8)))
		{
			Map<String, ColdString> result = new SimpleMapSilo<>(getColdStringSerialiser())
				.load(reader, db);
			coldShards.put(shard, result);
			return result;
		}
		catch (IOException e)
		{
			throw new MazeException(e);
		}
	}

	private File getHotStringFile(String namespace)
	{
		return new File(dbPath, STRINGS_DIR + HOT_FILE_PREFIX + namespace + ".json");
	}

	private File getColdShardFile(String shard)
	{
		return new File(dbPath, COLD_DIR + shard + ".json");
	}
}
