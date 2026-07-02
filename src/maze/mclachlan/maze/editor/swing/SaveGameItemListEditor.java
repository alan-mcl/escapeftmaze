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

package mclachlan.maze.editor.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import mclachlan.maze.stat.Item;

/**
 * Scrollable list of {@link ItemComponent} rows for editing save-game item lists.
 */
public class SaveGameItemListEditor extends JPanel implements ActionListener
{
	private final int dirtyFlag;
	private final JPanel itemsPanel;
	private final List<ItemComponent> components = new ArrayList<>();
	private List<Item> items;

	/*-------------------------------------------------------------------------*/
	public SaveGameItemListEditor(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		itemsPanel = new JPanel();
		itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));

		JButton add = new JButton("Add Item");
		add.setActionCommand("add");
		add.addActionListener(this);
		JButton remove = new JButton("Remove Item");
		remove.setActionCommand("remove");
		remove.addActionListener(this);
		JButton clear = new JButton("Clear All");
		clear.setActionCommand("clear");
		clear.addActionListener(this);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttons.add(add);
		buttons.add(remove);
		buttons.add(clear);

		setLayout(new BorderLayout(3, 3));
		add(buttons, BorderLayout.NORTH);
		add(new JScrollPane(itemsPanel), BorderLayout.CENTER);
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		for (ItemComponent component : components)
		{
			component.initForeignKeys();
		}
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(List<Item> itemList)
	{
		items = itemList == null ? new ArrayList<>() : itemList;
		rebuild();
	}

	/*-------------------------------------------------------------------------*/
	public List<Item> getItems()
	{
		List<Item> result = new ArrayList<>();
		for (ItemComponent component : components)
		{
			Item item = component.getItem();
			if (item != null)
			{
				result.add(item);
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void commitToList()
	{
		if (items == null)
		{
			items = new ArrayList<>();
		}
		items.clear();
		items.addAll(getItems());
	}

	/*-------------------------------------------------------------------------*/
	private void rebuild()
	{
		itemsPanel.removeAll();
		components.clear();

		int index = 0;
		for (Item item : items)
		{
			addRow(index++, item);
		}
		if (components.isEmpty())
		{
			addRow(0, null);
		}

		itemsPanel.revalidate();
		itemsPanel.repaint();
	}

	/*-------------------------------------------------------------------------*/
	private void addRow(int index, Item item)
	{
		JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
		row.add(new JLabel("#" + index + ":"));
		ItemComponent component = new ItemComponent(dirtyFlag);
		component.initForeignKeys();
		component.refresh(item);
		components.add(component);
		row.add(component);
		itemsPanel.add(row);
	}

	/*-------------------------------------------------------------------------*/
	private void markDirty()
	{
		SwingEditor.instance.setDirty(dirtyFlag);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		markDirty();
		commitToList();

		if ("add".equals(e.getActionCommand()))
		{
			items.add(null);
			rebuild();
		}
		else if ("remove".equals(e.getActionCommand()))
		{
			if (!items.isEmpty())
			{
				items.remove(items.size() - 1);
			}
			rebuild();
		}
		else if ("clear".equals(e.getActionCommand()))
		{
			items.clear();
			rebuild();
		}
	}
}
