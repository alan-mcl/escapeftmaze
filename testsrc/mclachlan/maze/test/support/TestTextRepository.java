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

package mclachlan.maze.test.support;

import java.io.IOException;
import java.util.*;
import mclachlan.maze.data.ColdString;
import mclachlan.maze.data.ColdStringManifestEntry;
import mclachlan.maze.data.TextRepository;
import mclachlan.maze.util.MazeException;

/**
 * In-memory HotString / ColdStrings stub for hermetic tests.
 */
public class TestTextRepository extends TextRepository
{
	private final Map<String, Map<String, String>> hotStrings = new HashMap<>();
	private final Map<String, Map<String, ColdString>> coldShards = new HashMap<>();
	private List<ColdStringManifestEntry> coldManifest = new ArrayList<>();

	public TestTextRepository()
	{
		super("", null);
	}

	public void putHotString(String namespace, String key, String value)
	{
		hotStrings.computeIfAbsent(namespace, n -> new LinkedHashMap<>()).put(key, value);
	}

	public void putColdString(String key, String body)
	{
		String shard = "test";
		coldShards.computeIfAbsent(shard, s -> new LinkedHashMap<>())
			.put(key, new ColdString(key, body));
		if (coldManifest.stream().noneMatch(e -> "test.".equals(e.getPrefix())))
		{
			coldManifest.add(new ColdStringManifestEntry("test.", shard));
		}
	}

	@Override
	public void resetCaches()
	{
	}

	@Override
	public String getHotString(String namespace, String key, boolean allowNull)
	{
		if (key != null && key.contains("reserved"))
		{
			return key;
		}

		Map<String, String> bundle = hotStrings.get(namespace);
		if (bundle != null && bundle.containsKey(key))
		{
			return bundle.get(key);
		}

		return key;
	}

	@Override
	public boolean hasHotNamespace(String namespace)
	{
		return true;
	}

	@Override
	public String getColdString(String key, boolean allowNull)
	{
		if (key != null && key.contains("reserved"))
		{
			return key;
		}

		for (Map<String, ColdString> shard : coldShards.values())
		{
			ColdString coldString = shard.get(key);
			if (coldString != null)
			{
				return coldString.getBody();
			}
		}

		return key;
	}

	@Override
	public Map<String, String> getHotNamespace(String namespace)
	{
		return hotStrings.get(namespace);
	}

	@Override
	public Map<String, ColdString> getColdShard(String shard)
	{
		return coldShards.get(shard);
	}

	@Override
	public List<ColdStringManifestEntry> getColdManifest()
	{
		return coldManifest;
	}

	@Override
	public void saveHotNamespace(String namespace, Map<String, String> strings) throws IOException
	{
		hotStrings.put(namespace, new LinkedHashMap<>(strings));
	}

	@Override
	public void saveColdShard(String shard, Map<String, ColdString> strings)
	{
		coldShards.put(shard, new LinkedHashMap<>(strings));
	}

	@Override
	public void saveColdManifest(List<ColdStringManifestEntry> manifest) throws IOException
	{
		coldManifest = new ArrayList<>(manifest);
	}
}
