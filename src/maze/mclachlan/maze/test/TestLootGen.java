package mclachlan.maze.test;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.LootTable;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.combat.Combat;

/**
 *
 */
public class TestLootGen
{
	public static void main(String[] args) throws Exception
	{
		V2Saver v2Saver = new V2Saver();
		V2Loader v2Loader = new V2Loader();
		Database db = new Database(v2Loader, v2Saver, Maze.getStubCampaign());

		db.initImpls();
		db.initCaches(null);

		LootTable testingFoeLoot = db.getLootTable("TestingFoeLoot");
		List<Item> items = new ArrayList<>();

		for (int i=0; i<10; i++)
		{
			items.addAll(testingFoeLoot.generate());
		}

		List<Item> lootedItems = Combat.getLootedItems(items);

		for (Item item : lootedItems)
		{
			System.out.println(item.getName());
		}
	}
}
