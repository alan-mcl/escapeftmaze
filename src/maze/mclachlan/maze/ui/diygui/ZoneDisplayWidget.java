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

package mclachlan.maze.ui.diygui;

import java.awt.*;
import java.util.List;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.Zone;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.ItemCacheManager;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.ui.diygui.render.MazeRendererFactory;

/**
 *
 */
public class ZoneDisplayWidget extends DIYPane implements ActionListener
{
	DIYLabel zoneName, terrainType;
	ManaDisplayWidget mana;
	DIYLabel stealth;
	DroppedItemWidget cacheItem;

	private static final Color DEFAULT_COLOUR = Constants.Colour.COMBAT_RED;
	private static final String DEFAULT_MESSAGE = "...";

	/*-------------------------------------------------------------------------*/
	public ZoneDisplayWidget(Rectangle bounds)
	{
		super(bounds);

		this.setLayoutManager(new DIYGridLayout(1,2,0,0));

		DIYPane titlePane = new DIYPane(new DIYGridLayout(3,1,0,0));
		DIYPane midPane = new DIYPane(new DIYGridLayout(3,1,0,0));
		mana = new ManaDisplayWidget();

		zoneName = new DIYLabel("", DIYToolkit.Align.LEFT);
		terrainType = new DIYLabel("", DIYToolkit.Align.RIGHT);

		stealth = new DIYLabel("", DIYToolkit.Align.LEFT);

		cacheItem = new DroppedItemWidget();
		cacheItem.addActionListener(this);
		
		Compass compass = new Compass();

		titlePane.add(zoneName);
		titlePane.add(compass);
		titlePane.add(terrainType);

		midPane.add(stealth);
		midPane.add(mana);
		midPane.add(cacheItem);

		this.add(titlePane);
		this.add(midPane);
	}

	/*-------------------------------------------------------------------------*/
	public void setZone(Zone z)
	{
		this.zoneName.setText(z.getName());
	}

	/*-------------------------------------------------------------------------*/
	public void setTile(Zone zone, Tile t, Point tile)
	{
		this.terrainType.setText(t.getTerrainType()+"("+t.getTerrainSubType()+")");

		int partyStealth = GameSys.getInstance().getStealthValue(t, Maze.getInstance().getParty());
		stealth.setText("Stealth: "+partyStealth);

		mana.refresh(
			t.getAmountRedMagic(),
			t.getAmountBlackMagic(),
			t.getAmountPurpleMagic(),
			t.getAmountGoldMagic(),
			t.getAmountWhiteMagic(),
			t.getAmountGreenMagic(),
			t.getAmountBlueMagic());

		List<Item> itemsOnTile = ItemCacheManager.getInstance().getItemsOnTile(zone, tile);

		if (itemsOnTile != null && itemsOnTile.size() > 0)
		{
			cacheItem.setItem(itemsOnTile.get(0));
		}
		else
		{
			cacheItem.setItem(null);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == cacheItem)
		{
			if (cacheItem.item != null)
			{
				List<Item> items = ItemCacheManager.getInstance().getItemsOnTile(
					Maze.getInstance().getZone(), Maze.getInstance().getTile());

				ItemCacheManager.getInstance().clearItemsOnTile(
					Maze.getInstance().getZone(), Maze.getInstance().getTile());

				Maze.getInstance().grantItems(items);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public static class DroppedItemWidget extends DIYPane
	{
		Item item;

		public void setItem(Item item)
		{
			this.item = item;
		}

		public Item getItem()
		{
			return item;
		}

		@Override
		public String getWidgetName()
		{
			return MazeRendererFactory.DROPPED_ITEM_WIDGET;
		}
	}
}
