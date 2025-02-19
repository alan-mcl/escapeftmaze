package mclachlan.maze.data.v2.serialisers;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.V2SerialiserMap;
import mclachlan.maze.data.v2.V2SerialiserObject;
import mclachlan.maze.stat.PercentageTable;

/**
 *
 */
public class PercentageTableSerialiser<T> implements V2SerialiserMap<PercentageTable<T>>
{
	public static final String ITEMS = "ITEMS";
	public static final String PERCENTAGES = "PERCENTAGES";
	public static final String SHOULD_SUM_TO_100 = "SHOULD_SUM_TO_100";
	private final V2SerialiserObject<T> itemSerialiser;

	public PercentageTableSerialiser(V2SerialiserObject<T> itemSerialiser)
	{
		this.itemSerialiser = itemSerialiser;
	}

	@Override
	public Map toObject(PercentageTable pt, Database db)
	{
		if (pt == null)
		{
			return null;
		}

		Map result = new HashMap();

		List items = new ArrayList();
		pt.getItems().forEach(p -> items.add(itemSerialiser.toObject((T)p, db)));

		result.put(ITEMS, items);
		result.put(PERCENTAGES, pt.getPercentages());
		result.put(SHOULD_SUM_TO_100, Boolean.toString(pt.shouldSumTo100()));

		return result;
	}

	@Override
	public PercentageTable fromObject(Object obj, Database db)
	{
		if (obj == null)
		{
			return null;
		}

		Map map = (Map)obj;

		List percSrc = (List)map.get(PERCENTAGES);
		List itemSrc = (List)map.get(ITEMS);
		Boolean shouldSumTo100 = Boolean.valueOf((String)map.get(SHOULD_SUM_TO_100));

		List<Integer> perc = new ArrayList<>();
		percSrc.forEach(p -> perc.add((int)Math.round(Double.valueOf(p.toString()))));
		List items = new ArrayList();
		itemSrc.forEach(p -> items.add(itemSerialiser.fromObject(p, db)));

		PercentageTable<T> result = new PercentageTable<>(items, perc, shouldSumTo100);
		return result;
	}
}
