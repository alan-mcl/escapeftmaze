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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1Dice;
import mclachlan.maze.map.ILootEntry;
import mclachlan.maze.map.LootEntryRow;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.ItemTemplate;
import mclachlan.maze.stat.npc.NpcInventoryTemplateRow;
import mclachlan.maze.util.GenInv;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.stat.ItemTemplate.*;

/**
 *
 */
public class NpcInventoryTemplateComponent extends JPanel implements ActionListener
{
	private int dirtyFlag;
	JTable table;
	JButton add, remove, quickFill, clear;
	MyTableModel dataModel;
	JComboBox itemTemplateCombo;

	/*-------------------------------------------------------------------------*/
	protected NpcInventoryTemplateComponent(int dirtyFlag, double scaleX, double scaleY)
	{
		this.dirtyFlag = dirtyFlag;

		itemTemplateCombo = new JComboBox();
		dataModel = new MyTableModel();
		table = new JTable(dataModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDefaultRenderer(Integer.TYPE, new DefaultTableCellRenderer());
		table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(itemTemplateCombo));
		table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JTextField()));
		table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JTextField()));
		table.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(new JTextField()));
		table.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JTextField()));
		table.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JTextField()));

		Dimension d = table.getPreferredScrollableViewportSize();
		table.setPreferredScrollableViewportSize(
			new Dimension((int)(d.width*scaleX), (int)(d.height*scaleY)));

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
		this.add(new JScrollPane(table), BorderLayout.CENTER);
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

		for (int i=0; i<dataModel.itemTemplates.size(); i++)
		{
			String s = dataModel.stackSizes.get(i);
			result.add(
				new NpcInventoryTemplateRow(
					dataModel.itemTemplates.get(i),
					dataModel.chancesOfSpawning.get(i),
					dataModel.partyLevelsAppearing.get(i),
					dataModel.maxesStocked.get(i),
					dataModel.chancesOfVanishing.get(i),
					s==null?null:V1Dice.fromString(s)));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(List<NpcInventoryTemplateRow> list)
	{
		dataModel.clear();

		if (list != null)
		{
			Collections.sort(list, new NpcInvTemRowCmp());
			for (NpcInventoryTemplateRow row : list)
			{
				dataModel.add(
					row.getItemName(),
					row.getChanceOfSpawning(),
					row.getPartyLevelAppearing(),
					row.getMaxStocked(),
					row.getChanceOfVanishing(),
					row.getStackSize());
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == add)
		{
			dataModel.add(
				(String)itemTemplateCombo.getItemAt(0),
				0,
				0,
				0,
				0,
				Dice.d1);
		}
		else if (e.getSource() == remove)
		{
			int index = table.getSelectedRow();
			if (index > -1)
			{
				dataModel.remove(index);
			}
		}
		else if (e.getSource() == quickFill)
		{
			QuickFillDialog d = new QuickFillDialog();

			if (d.apply)
			{
				quickFillInvTemplate(d);
			}
		}
		else if (e.getSource() == clear)
		{
			dataModel.clear();
		}
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

				dataModel.add(
					it.getName(),
					spawnChance,
					lvlAppearing,
					stocked,
					vanishChance,
					stack);
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

				dataModel.add(
					it.getName(),
					spawnChance,
					lvlAppearing,
					stocked,
					vanishChance,
					stack);
			}
		}
	}

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
	class MyTableModel extends AbstractTableModel
	{
		List<String> itemTemplates = new ArrayList<String>();
		List<Integer> chancesOfSpawning = new ArrayList<Integer>();
		List<Integer> partyLevelsAppearing = new ArrayList<Integer>();
		List<Integer> maxesStocked = new ArrayList<Integer>();
		List<Integer> chancesOfVanishing = new ArrayList<Integer>();
		List<String> stackSizes = new ArrayList<String>();

		/*----------------------------------------------------------------------*/
		public String getColumnName(int column)
		{
			switch (column)
			{
				case 0: return "Item Template";
				case 1: return "Chance Of Spawning";
				case 2: return "Party Level Appearing";
				case 3: return "Max Stocked";
				case 4: return "Chance Of Vanishing";
				case 5: return "Stack Size";
				default: throw new MazeException("Invalid column "+column);
			}
		}

		/*----------------------------------------------------------------------*/
		public int getColumnCount()
		{
			return 6;
		}

		/*----------------------------------------------------------------------*/
		public int getRowCount()
		{
			return itemTemplates.size();
		}

		/*----------------------------------------------------------------------*/
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			switch (columnIndex)
			{
				case 0: return itemTemplates.get(rowIndex);
				case 1: return chancesOfSpawning.get(rowIndex);
				case 2: return partyLevelsAppearing.get(rowIndex);
				case 3: return maxesStocked.get(rowIndex);
				case 4: return chancesOfVanishing.get(rowIndex);
				case 5: return stackSizes.get(rowIndex);
				default: throw new MazeException("Invalid columnIndex "+columnIndex);
			}
		}

		/*----------------------------------------------------------------------*/
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			SwingEditor.instance.setDirty(dirtyFlag);
			switch (columnIndex)
			{
				case 0: itemTemplates.set(rowIndex, (String)aValue); break;
				case 1: chancesOfSpawning.set(rowIndex, Integer.valueOf((String)aValue)); break;
				case 2: partyLevelsAppearing.set(rowIndex, Integer.valueOf((String)aValue)); break;
				case 3: maxesStocked.set(rowIndex, Integer.valueOf((String)aValue)); break;
				case 4: chancesOfVanishing.set(rowIndex, Integer.valueOf((String)aValue)); break;
				case 5:
					{
						String cov = (String)aValue;
						if (cov.equals(""))
						{
							stackSizes.set(rowIndex, null);
						}
						else
						{
							stackSizes.set(rowIndex, cov);
						}
						break;
					}
				default: throw new MazeException("Invalid columnIndex "+columnIndex);
			}
		}

		/*----------------------------------------------------------------------*/
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return true;
		}

		/*----------------------------------------------------------------------*/
		public void clear()
		{
			itemTemplates.clear();
			chancesOfVanishing.clear();
			chancesOfSpawning.clear();
			stackSizes.clear();
			maxesStocked.clear();
			partyLevelsAppearing.clear();
			fireTableDataChanged();
		}

		/*----------------------------------------------------------------------*/
		public void add(
			String item, 
			int chanceOfSpawning,
			int partyLevelAppearing,
			int maxStocked,
			int chanceOfVanishing,
			Dice stackSize)
		{
			this.itemTemplates.add(item);
			this.chancesOfVanishing.add(chanceOfVanishing);
			this.chancesOfSpawning.add(chanceOfSpawning);
			this.stackSizes.add(V1Dice.toString(stackSize));
			this.maxesStocked.add(maxStocked);
			this.partyLevelsAppearing.add(partyLevelAppearing);
			fireTableDataChanged();
		}

		/*----------------------------------------------------------------------*/
		public void remove(int index)
		{
			itemTemplates.remove(index);
			chancesOfVanishing.remove(index);
			chancesOfSpawning.remove(index);
			stackSizes.remove(index);
			maxesStocked.remove(index);
			partyLevelsAppearing.remove(index);
			fireTableDataChanged();
		}
	}

	/*-------------------------------------------------------------------------*/
	private class NpcInvTemRowCmp implements Comparator
	{
		public int compare(Object o1, Object o2)
		{
			NpcInventoryTemplateRow r1 = (NpcInventoryTemplateRow)o1;
			NpcInventoryTemplateRow r2 = (NpcInventoryTemplateRow)o2;

			return r1.getItemName().compareTo(r2.getItemName());
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
