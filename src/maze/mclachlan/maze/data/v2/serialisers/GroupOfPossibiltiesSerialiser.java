package mclachlan.maze.data.v2.serialisers;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.V2SerialiserMap;
import mclachlan.maze.data.v2.V2SerialiserObject;
import mclachlan.maze.stat.GroupOfPossibilities;

/**
 *
 */
public class GroupOfPossibiltiesSerialiser<T> implements V2SerialiserMap<GroupOfPossibilities<T>>
{
	public static final String POSSIBILITIES = "POSSIBILITIES";
	public static final String PERCENTAGES = "PERCENTAGES";
	private final V2SerialiserObject<T> posSerialiser;

	public GroupOfPossibiltiesSerialiser(V2SerialiserObject<T> posSerialiser)
	{
		this.posSerialiser = posSerialiser;
	}

	@Override
	public Map toObject(GroupOfPossibilities gop,
		Database db)
	{
		if (gop == null)
		{
			return null;
		}

		Map result = new HashMap();

		List pos = new ArrayList();
		gop.getPossibilities().forEach(p -> pos.add(posSerialiser.toObject((T)p, db)));

		result.put(PERCENTAGES, gop.getPercentages());
		result.put(POSSIBILITIES, pos);

		return result;
	}

	@Override
	public GroupOfPossibilities fromObject(Object obj, Database db)
	{
		if (obj == null)
		{
			return null;
		}

		Map map = (Map)obj;

		List percSrc = (List)map.get(PERCENTAGES);
		List posSrc = (List)map.get(POSSIBILITIES);

		List<Integer> perc = new ArrayList<>();
		percSrc.forEach(p -> perc.add((int)Math.round(Double.valueOf(p.toString()))));
		List pos = new ArrayList();
		posSrc.forEach(p -> pos.add(posSerialiser.fromObject(p, db)));

		GroupOfPossibilities<T> result = new GroupOfPossibilities<>(pos, perc);
		return result;
	}
}
