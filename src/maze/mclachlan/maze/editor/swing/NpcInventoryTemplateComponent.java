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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1Dice;
import mclachlan.maze.map.ILootEntry;
import mclachlan.maze.map.LootEntryRow;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.ItemTemplate;
import mclachlan.maze.stat.npc.NpcInventoryTemplateRow;
import mclachlan.maze.stat.npc.NpcInventoryTemplateRowItem;
import mclachlan.maze.stat.npc.NpcInventoryTemplateRowLootEntry;
import mclachlan.maze.util.GenInv;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.stat.ItemTemplate.Type;

/**
 *
 */
public class NpcInventoryTemplateComponent extends JPanel
	implements ActionListener, MouseListener
{
	private final int dirtyFlag;
	private JButton add, remove, quickFill, clear;
	private JComboBox itemTemplateCombo;

	private JList list;
	private InventoryTemplateRowListModel dataModel;

	/*-------------------------------------------------------------------------*/
	protected NpcInventoryTemplateComponent(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;

		itemTemplateCombo = new JComboBox();
		dataModel = new InventoryTemplateRowListModel(
			new ArrayList<NpcInventoryTemplateRow>());
		list = new JList(dataModel);
		list.addMouseListener(this);

		add = new JButton("Add");
		add.addActionListener(this);
		remove = new JButton("Remove");
		remove.addActionListener(this);
		quickFill = new JButton("Quick Fill");
		quickFill.addActionListener(this);
		clear = new JButton("Clear");
		clear.addActionListener(this);
		JPanel buttons = new JPanel();
		buttons.add(add);
		buttons.add(remove);
		buttons.add(quickFill);
		buttons.add(clear);

		this.setLayout(new BorderLayout(3,3));
		this.add(new JScrollPane(list), BorderLayout.CENTER);
		this.add(buttons, BorderLayout.NORTH);
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector vec = new Vector(Database.getInstance().getItemTemplates().keySet());
		Collections.sort(vec);
		itemTemplateCombo.setModel(new DefaultComboBoxModel(vec));
	}

	/*-------------------------------------------------------------------------*/
	public List<NpcInventoryTemplateRow> getList()
	{
		List<NpcInventoryTemplateRow> result = new ArrayList<NpcInventoryTemplateRow>();

		result.addAll(dataModel.data);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(List<NpcInventoryTemplateRow> list)
	{
		dataModel.clear();

		if (list != null)
		{
			Collections.sort(list, new NpcInvTemRowCmp());
			for (NpcInventoryTemplateRow r : list)
			{
				dataModel.add(r);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void editListItem()
	{
		int index = list.getSelectedIndex();
		if (index > -1)
		{
			NpcInventoryTemplateRow row = dataModel.data.get(index);

			InventoryRowDialog dialog = new InventoryRowDialog(row);
			if (dialog.getInventoryTemplateRow() != null)
			{
				SwingEditor.instance.setDirty(dirtyFlag);
				dataModel.update(dialog.getInventoryTemplateRow(), index);
			}
		}
	}


	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == add)
		{
			InventoryRowDialog d = new InventoryRowDialog();

			if (d.apply)
			{
				dataModel.add(d.getInventoryTemplateRow());
				SwingEditor.instance.setDirty(dirtyFlag);
			}
		}
		else if (e.getSource() == remove)
		{
			int index = list.getSelectedIndex();
			if (index > -1)
			{
				dataModel.remove(index);
				SwingEditor.instance.setDirty(dirtyFlag);
			}
		}
		else if (e.getSource() == quickFill)
		{
			QuickFillDialog d = new QuickFillDialog();

			if (d.apply)
			{
				quickFillInvTemplate(d);
				SwingEditor.instance.setDirty(dirtyFlag);
			}
		}
		else if (e.getSource() == clear)
		{
			dataModel.clear();
			SwingEditor.instance.setDirty(dirtyFlag);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void mouseClicked(MouseEvent e)
	{
		if (e.getSource() == list)
		{
			if (e.getClickCount() == 2)
			{
				// a double click on a list item, treat as an edit
				SwingEditor.instance.setDirty(dirtyFlag);
				editListItem();
			}
		}
	}

	public void mousePressed(MouseEvent e)
	{

	}

	public void mouseReleased(MouseEvent e)
	{

	}

	public void mouseEntered(MouseEvent e)
	{

	}

	public void mouseExited(MouseEvent e)
	{

	}


	/*-------------------------------------------------------------------------*/
	private void quickFillInvTemplate(QuickFillDialog d)
	{
		if (d.tabbedPane.getSelectedIndex() == 0)
		{
			// fill by item type
			for (ItemTypeComponent comp : d.itemTypeComps)
			{
				if (comp.select.isSelected())
				{
					quickFillItemType(
						comp.type,
						(Integer)comp.maxPrice.getValue(),
						comp.chanceOfSpawning.getText(),
						comp.chanceOfVanishing.getText(),
						comp.stackSize.getText(),
						comp.maxStocked.getText(),
						comp.partyLvlAppearing.getText());
				}
			}
		}
		else
		{
			// fill by loot entry
			for (LootEntryComponent comp : d.lootEntryComps)
			{
				if (comp.lootEntry.getSelectedItem() != EditorPanel.NONE)
				{
					quickFillLootEntry(
						Database.getInstance().getLootEntry((String)comp.lootEntry.getSelectedItem()),
						(Integer)comp.maxPrice.getValue(),
						comp.chanceOfSpawning.getText(),
						comp.chanceOfVanishing.getText(),
						comp.stackSize.getText(),
						comp.maxStocked.getText(),
						comp.partyLvlAppearing.getText());
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void quickFillLootEntry(
		ILootEntry lootEntry,
		int maxPrice,
		String chanceOfSpawning,
		String chanceOfVanishing,
		String stackSize,
		String maxStocked,
		String partyLvlAppearing)
	{
		Dice spawnDice = getDice(chanceOfSpawning);
		Dice vanishDice = getDice(chanceOfVanishing);
		Dice stackDice = getDice(stackSize);
		Dice stockedDice = getDice(maxStocked);
		Dice lvlDice = getDice(partyLvlAppearing);

		for (LootEntryRow ler : lootEntry.getContents())
		{
			ItemTemplate it = Database.getInstance().getItemTemplate(ler.getItemName());

			if (maxPrice == -1 || maxPrice <= it.getBaseCost())
			{
				int spawnChance = spawnDice==null ? 15 : spawnDice.roll();
				int lvlAppearing = lvlDice==null ? GenInv.getDefaultPartyLvlAppearing(it) : lvlDice.roll();
				int stocked = stockedDice==null ? 1 : stockedDice.roll();
				int vanishChance = vanishDice==null ? 15 : vanishDice.roll();
				Dice stack = stackDice==null ? getDefaultStackSize(it.getType()) : stackDice;

				// todo: shouldn't this add a Loot Entry? Or maybe be removed?
				NpcInventoryTemplateRowItem r = new NpcInventoryTemplateRowItem(
					it.getName(),
					spawnChance,
					lvlAppearing,
					stocked,
					vanishChance,
					stack);

				dataModel.add(r);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void quickFillItemType(
		int type,
		int maxPrice,
		String chanceOfSpawning,
		String chanceOfVanishing,
		String stackSize,
		String maxStocked,
		String partyLvlAppearing)
	{
		Dice spawnDice = getDice(chanceOfSpawning);
		Dice vanishDice = getDice(chanceOfVanishing);
		Dice stackDice = getDice(stackSize);
		Dice stockedDice = getDice(maxStocked);
		Dice lvlDice = getDice(partyLvlAppearing);

		for (ItemTemplate it : Database.getInstance().getItemTemplates().values())
		{
			if (it.getType() == type && (maxPrice <= 0 || it.getBaseCost() <= maxPrice))
			{
				int spawnChance = spawnDice==null ? 15 : spawnDice.roll();
				int lvlAppearing = lvlDice==null ? GenInv.getDefaultPartyLvlAppearing(it) : lvlDice.roll();
				int stocked = stockedDice==null ? 1 : stockedDice.roll();
				int vanishChance = vanishDice==null ? 15 : vanishDice.roll();
				Dice stack = stackDice==null ? getDefaultStackSize(type) : stackDice;

				NpcInventoryTemplateRowItem r = new NpcInventoryTemplateRowItem(
					it.getName(),
					spawnChance,
					lvlAppearing,
					stocked,
					vanishChance,
					stack);

				dataModel.add(r);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private Dice getDefaultStackSize(int type)
	{
		switch (type)
		{
			case Type.AMMUNITION: return new Dice(3, 6, 35);
			case Type.BOMB: return new Dice(1,2,1);
			case Type.POTION: return new Dice(1,2,1);
			case Type.POWDER: return new Dice(2,2,0);
			case Type.THROWN_WEAPON: return new Dice(5,6,0);
			default: return Dice.d1;
		}
	}

	/*-------------------------------------------------------------------------*/
	private Dice getDice(String txt)
	{
		try
		{
			int i = Integer.parseInt(txt);
			return new Dice(i,1,0);
		}
		catch (NumberFormatException x)
		{
			try
			{
				return V1Dice.fromString(txt);
			}
			catch (Exception e)
			{
				return null;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private JPanel dirtyGridBagCrap(Component... comps)
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();
		for (int i=0; i<comps.length; i+=2)
		{
			dodgyGridBagShite(result, comps[i], comps[i+1], gbc);
		}

		gbc.weighty = 1.0;
		dodgyGridBagShite(result, new JLabel(), new JLabel(), gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	protected void dodgyGridBagShite(JPanel panel, Component a, Component b, GridBagConstraints gbc)
	{
		gbc.weightx = 0.0;
		gbc.gridx=0;
		panel.add(a, gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		panel.add(b, gbc);
		gbc.gridy++;
	}

	/*-------------------------------------------------------------------------*/
	protected GridBagConstraints createGridBagConstraints()
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.WEST;
		return gbc;
	}

	/*-------------------------------------------------------------------------*/
	private static class InventoryTemplateRowListModel extends AbstractListModel
	{
		List<NpcInventoryTemplateRow> data;

		public InventoryTemplateRowListModel(List<NpcInventoryTemplateRow> data)
		{
			this.data = data;
		}

		public Object getElementAt(int index)
		{
			NpcInventoryTemplateRow row = data.get(index);
			return row.toString();
		}

		public int getSize()
		{
			return data.size();
		}

		public void add(NpcInventoryTemplateRow me)
		{
			data.add(me);
			fireContentsChanged(this, data.size(), data.size());
		}

		public void remove(int index)
		{
			data.remove(index);
			fireIntervalRemoved(this, index, index);
		}

		public void update(NpcInventoryTemplateRow me, int index)
		{
			data.set(index, me);
			fireContentsChanged(this, index, index);
		}

		public void moveUp(int index)
		{
			if (index > 0)
			{
				NpcInventoryTemplateRow row = data.remove(index);
				data.add(index-1, row);
				fireContentsChanged(this, index-1, index);
			}
		}

		public void moveDown(int index)
		{
			if (index < data.size()-1)
			{
				NpcInventoryTemplateRow row = data.remove(index);
				data.add(index+1, row);
				fireContentsChanged(this, index, index+1);
			}
		}

		public void clear()
		{
			int size = data.size();
			data.clear();
			fireContentsChanged(this, 0, size-1);
		}
	}

	/*-------------------------------------------------------------------------*/
	private class NpcInvTemRowCmp implements Comparator
	{
		public int compare(Object o1, Object o2)
		{
			NpcInventoryTemplateRow r1 = (NpcInventoryTemplateRow)o1;
			NpcInventoryTemplateRow r2 = (NpcInventoryTemplateRow)o2;

			int sortOrder = getSortOrder(r1) - getSortOrder(r2);

			if (sortOrder != 0)
			{
				return sortOrder;
			}
			else
			{
				return r1.compareTo(r2);
			}
		}

		private int getSortOrder(NpcInventoryTemplateRow row)
		{
			if (row instanceof NpcInventoryTemplateRowLootEntry)
			{
				return 1;
			}
			else if (row instanceof NpcInventoryTemplateRowItem)
			{
				return 2;
			}
			else
			{
				throw new MazeException(""+row);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private class QuickFillDialog extends JDialog implements ActionListener
	{
		JButton ok, cancel;
		JPanel byItemIype, byLootEntry;
		ItemTypeComponent[] itemTypeComps;
		LootEntryComponent[] lootEntryComps;
		boolean apply;
		private JTabbedPane tabbedPane;
		JComboBox lootEntry;

		private QuickFillDialog()
		{
			super(SwingEditor.instance, "Quick Fill Inventory Template", true);
			setLayout(new BorderLayout());

			JPanel buttons = new JPanel();
			ok = new JButton("OK");
			ok.addActionListener(this);
			cancel = new JButton("Cancel");
			cancel.addActionListener(this);
			buttons.add(ok);
			buttons.add(cancel);

			tabbedPane = new JTabbedPane();

			tabbedPane.add("By Item Type", getByItemTypePanel());
			tabbedPane.add("By Loot Entry", getByLootEntryPanel());

			this.add(tabbedPane, BorderLayout.CENTER);
			this.add(buttons, BorderLayout.SOUTH);

			pack();
			setLocationRelativeTo(NpcInventoryTemplateComponent.this);
			setVisible(true);
		}

		JPanel getByLootEntryPanel()
		{
			JPanel result = new JPanel();
			int max = 24;
			result.setLayout(new GridLayout(max, 1));

			result.add(new JLabel("Choose loot entries..."));

			JPanel header = new JPanel(new GridLayout(1, 7));
			header.add(new JLabel());
			header.add(new JLabel("Max Price"));
			header.add(new JLabel("% Spawn"));
			header.add(new JLabel("% Vanish"));
			header.add(new JLabel("Stack Size"));
			header.add(new JLabel("Max Stocked"));
			header.add(new JLabel("Party Lvl"));

			result.add(header);

			lootEntryComps = new LootEntryComponent[max];
			for (int i = 0; i < max-2; i++)
			{
				lootEntryComps[i] = new LootEntryComponent();
				result.add(lootEntryComps[i]);
			}

			return result;
		}

		JPanel getByItemTypePanel()
		{
			JPanel result = new JPanel();
			result.setLayout(new GridLayout(ItemTemplate.Type.MAX_ITEM_TYPES+2, 1));

			result.add(new JLabel("Enter integers or Dice in each field..."));

			JPanel header = new JPanel(new GridLayout(1, 8));
			header.add(new JLabel());
			header.add(new JLabel());
			header.add(new JLabel("Max Price"));
			header.add(new JLabel("% Spawn"));
			header.add(new JLabel("% Vanish"));
			header.add(new JLabel("Stack Size"));
			header.add(new JLabel("Max Stocked"));
			header.add(new JLabel("Party Lvl"));

			result.add(header);

			itemTypeComps = new ItemTypeComponent[ItemTemplate.Type.MAX_ITEM_TYPES];
			for (int i = 0; i < ItemTemplate.Type.MAX_ITEM_TYPES; i++)
			{
				itemTypeComps[i] = new ItemTypeComponent(i);
				result.add(itemTypeComps[i]);
			}

			return result;
		}

		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() == ok)
			{
				apply = true;
			}

			setVisible(false);
		}
	}

	/*-------------------------------------------------------------------------*/
	private class InventoryRowDialog extends JDialog implements ActionListener
	{
		private boolean apply = false;

		private JTabbedPane tabbedPane;
		private JButton ok, cancel;
		private JComboBox<String> items, lootEntries;
		private JSpinner spawnChance, vanishChance, partyLevelAppearing, maxStocked;
		private DiceField stackSize, itemsToSpawnLE;
		private JSpinner spawnChanceLE, vanishChanceLE, partyLevelAppearingLE, maxStockedLE;

		/*-------------------------------------------------------------------------*/
		private InventoryRowDialog()
		{
			super(SwingEditor.instance, "Add To Inventory Template", true);
			buildUI();
			setVisible(true);
		}

		/*-------------------------------------------------------------------------*/
		private InventoryRowDialog(NpcInventoryTemplateRow row)
		{
			super(SwingEditor.instance, "Add To Inventory Template", true);
			buildUI();

			if (row instanceof NpcInventoryTemplateRowItem)
			{
				NpcInventoryTemplateRowItem r = (NpcInventoryTemplateRowItem)row;

				tabbedPane.setSelectedIndex(0);

				items.setSelectedItem(r.getItemName());
				spawnChance.setValue(r.getChanceOfSpawning());
				vanishChance.setValue(r.getChanceOfVanishing());
				partyLevelAppearing.setValue(r.getPartyLevelAppearing());
				maxStocked.setValue(r.getMaxStocked());
				stackSize.setDice(r.getStackSize());
			}
			else if (row instanceof NpcInventoryTemplateRowLootEntry)
			{
				NpcInventoryTemplateRowLootEntry r = (NpcInventoryTemplateRowLootEntry)row;

				tabbedPane.setSelectedIndex(1);

				items.setSelectedItem(r.getLootEntry());
				spawnChance.setValue(r.getChanceOfSpawning());
				vanishChance.setValue(r.getChanceOfVanishing());
				partyLevelAppearing.setValue(r.getPartyLevelAppearing());
				maxStocked.setValue(r.getMaxStocked());
				itemsToSpawnLE.setValue(r.getItemsToSpawn());
			}

			setVisible(true);
		}

		/*-------------------------------------------------------------------------*/
		private void buildUI()
		{
			setLayout(new BorderLayout());

			JPanel buttons = new JPanel();
			ok = new JButton("OK");
			ok.addActionListener(this);
			cancel = new JButton("Cancel");
			cancel.addActionListener(this);
			buttons.add(ok);
			buttons.add(cancel);

			tabbedPane = new JTabbedPane();

			tabbedPane.add("Item", getItemPanel());
			tabbedPane.add("Loot Entry", getLootEntryPanel());

			this.add(tabbedPane, BorderLayout.CENTER);
			this.add(buttons, BorderLayout.SOUTH);

			pack();
			setLocationRelativeTo(NpcInventoryTemplateComponent.this);
		}

		/*-------------------------------------------------------------------------*/
		private Component getItemPanel()
		{
			Vector<String> itemVec = new Vector<String>(
				Database.getInstance().getItemTemplates().keySet());
			Collections.sort(itemVec);
			items = new JComboBox<String>(itemVec);

			spawnChance = new JSpinner(new SpinnerNumberModel(15, 1, 100, 1));
			vanishChance = new JSpinner(new SpinnerNumberModel(15, 1, 100, 1));
			partyLevelAppearing = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
			maxStocked = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
			stackSize = new DiceField();

			return dirtyGridBagCrap(
				new JLabel("Item:"), items,
				new JLabel("Party Lvl Appearing:"), partyLevelAppearing,
				new JLabel("Spawn Chance (%):"), spawnChance,
				new JLabel("Vanish Chance (%):"), vanishChance,
				new JLabel("Max Stocked:"), maxStocked,
				new JLabel("Stack Size:"), stackSize);
		}

		/*-------------------------------------------------------------------------*/
		private Component getLootEntryPanel()
		{
			Vector<String> leVec = new Vector<String>(
				Database.getInstance().getLootEntries().keySet());
			Collections.sort(leVec);
			lootEntries = new JComboBox<String>(leVec);

			spawnChanceLE = new JSpinner(new SpinnerNumberModel(15, 1, 100, 1));
			vanishChanceLE = new JSpinner(new SpinnerNumberModel(15, 1, 100, 1));
			partyLevelAppearingLE = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
			maxStockedLE = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
			itemsToSpawnLE = new DiceField();

			return dirtyGridBagCrap(
				new JLabel("Loot Entry:"), lootEntries,
				new JLabel("Party Lvl Appearing:"), partyLevelAppearingLE,
				new JLabel("Spawn Chance (%):"), spawnChanceLE,
				new JLabel("Vanish Chance (%):"), vanishChanceLE,
				new JLabel("Max Stocked:"), maxStockedLE,
				new JLabel("Items To Spawn Each Round:"), itemsToSpawnLE);
		}

		/*-------------------------------------------------------------------------*/

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() == ok)
			{
				apply = true;
			}

			setVisible(false);
		}

		/*-------------------------------------------------------------------------*/
		public NpcInventoryTemplateRow getInventoryTemplateRow()
		{
			if (apply)
			{
				if (tabbedPane.getSelectedIndex() == 0)
				{
					return new NpcInventoryTemplateRowItem(
						(String)items.getSelectedItem(),
						(Integer)spawnChance.getValue(),
						(Integer)partyLevelAppearing.getValue(),
						(Integer)maxStocked.getValue(),
						(Integer)vanishChance.getValue(),
						stackSize.getDice());
				}
				else if (tabbedPane.getSelectedIndex() == 1)
				{
					return new NpcInventoryTemplateRowLootEntry(
						(Integer)spawnChance.getValue(),
						(Integer)partyLevelAppearing.getValue(),
						(Integer)maxStocked.getValue(),
						(Integer)vanishChance.getValue(),
						(String)lootEntries.getSelectedItem(),
						itemsToSpawnLE.getDice());
				}
				else
				{
					throw new MazeException(""+tabbedPane.getSelectedIndex());
				}
			}
			else
			{
				return null;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private class ItemTypeComponent extends JPanel
	{
		int type;
		JLabel name;
		JSpinner maxPrice;
		JTextField chanceOfVanishing, chanceOfSpawning, stackSize, maxStocked, partyLvlAppearing;
		JCheckBox select;

		private ItemTypeComponent(int type)
		{
			this.type = type;

			setLayout(new GridLayout(1, 8));

			select = new JCheckBox("sells?");
			name = new JLabel(ItemTemplate.Type.describe(type));
			maxPrice = new JSpinner(new SpinnerNumberModel(-1,-1,9999999, 1));
			chanceOfVanishing = new JTextField();
			chanceOfSpawning = new JTextField();
			stackSize = new JTextField();
			maxStocked = new JTextField();
			partyLvlAppearing = new JTextField();

			this.add(select);
			this.add(name);
			this.add(maxPrice);
			this.add(chanceOfSpawning);
			this.add(chanceOfVanishing);
			this.add(stackSize);
			this.add(maxStocked);
			this.add(partyLvlAppearing);
		}
	}

	/*-------------------------------------------------------------------------*/
	private class LootEntryComponent extends JPanel
	{
		JComboBox lootEntry;
		JSpinner maxPrice;
		JTextField chanceOfVanishing, chanceOfSpawning, stackSize, maxStocked, partyLvlAppearing;
		JCheckBox select;

		private LootEntryComponent()
		{
			setLayout(new GridLayout(1, 7));

			Vector<String> vec = new Vector<String>(Database.getInstance().getLootEntries().keySet());
			Collections.sort(vec);
			vec.add(0, EditorPanel.NONE);

			lootEntry = new JComboBox(vec);
			maxPrice = new JSpinner(new SpinnerNumberModel(-1,-1,9999999, 1));
			chanceOfVanishing = new JTextField();
			chanceOfSpawning = new JTextField();
			stackSize = new JTextField();
			maxStocked = new JTextField();
			partyLvlAppearing = new JTextField();

			this.add(lootEntry);
			this.add(maxPrice);
			this.add(chanceOfSpawning);
			this.add(chanceOfVanishing);
			this.add(stackSize);
			this.add(maxStocked);
			this.add(partyLvlAppearing);
		}
	}

}
