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
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.diygui.*;
import mclachlan.diygui.toolkit.*;

/**
 *
 */
public class SpellDisplayWidget extends DIYPane
{
	private final DIYButton levelPlus, levelMinus;
	private final Map<Widget, String> schoolButtons = new HashMap<Widget, String>();
	private final Map<String, Widget> schoolButtonsBySchool = new HashMap<String, Widget>();
	private PlayerCharacter playerCharacter;
	private final DIYListBox spellList;
	private final DIYTextField quickName;
	private final DIYLabel magicPointCostLabel, spellLevel;

	private int castingLevel = -1;
	private int magicPointCost = -1;

	private static final Comparator<Spell> spellComparator = new SpellLearningWidget.SpellComparator();

	/*-------------------------------------------------------------------------*/
	public SpellDisplayWidget(PlayerCharacter pc, Rectangle bounds)
	{
		super(bounds);

		RendererProperties rp = DIYToolkit.getInstance().getRendererProperties();

		int inset = rp.getProperty(RendererProperties.Property.INSET);
		int iconButtonSize = 45;
//		int titleHeight = rp.getProperty(RendererProperties.Property.TITLE_PANE_HEIGHT);
		int titleHeight = 20;

		int column1x = bounds.x + inset;
		int columnWidth = (width -5*inset) / 3;

		int column2x = column1x + columnWidth + inset;
		int column3x = column2x + columnWidth + inset;

		// column 1: filters
		List<String> spellSchools = MagicSys.getInstance().getSpellSchools();

		DIYPane leftPane = new DIYPane(
			new DIYGridLayout(1, height/30, 0, 0));
		leftPane.setBounds(
			x,
			y,
			columnWidth,
			height);

		leftPane.add(new DIYLabel(StringUtil.getUiLabel("ssd.filters")));

		SpellSelectionActionListener internalListener = new SpellSelectionActionListener();
		for (String school : spellSchools)
		{
			DIYCheckbox b = new DIYCheckbox(school);
			leftPane.add(b);
			schoolButtons.put(b, school);
			schoolButtonsBySchool.put(school, b);
			b.addActionListener(internalListener);

			b.setSelected(true);
		}

		quickName = new DIYTextField("", 15);
		leftPane.add(new DIYLabel());
		leftPane.add(quickName);

		// column 2: spell list

		List<String> spells = new ArrayList<>();
		spellList = new DIYListBox(spells);
		spellList.setBounds(
			column2x,
			y,
			columnWidth,
			height);
		spellList.addActionListener(internalListener);

		// column 3: casting level

		DIYLabel levelLabel = new DIYLabel(StringUtil.getUiLabel("ssd.casting.level"));
		levelLabel.setBounds(
			column3x,
			y,
			columnWidth,
			20);

		levelMinus = new DIYButton(null);
		levelMinus.setImage("icon/minus");
		levelMinus.addActionListener(internalListener);

		levelPlus = new DIYButton(null);
		levelPlus.setImage("icon/plus");
		levelPlus.addActionListener(internalListener);

		spellLevel = new DIYLabel("");

		levelMinus.setBounds(
			column3x,
			levelLabel.y +levelLabel.height +inset,
			iconButtonSize,
			iconButtonSize);

		spellLevel.setBounds(
			column3x +columnWidth/2 -30,
			levelLabel.y +levelLabel.height +inset,
			60,
			20);

		levelPlus.setBounds(
			column3x +columnWidth -iconButtonSize,
			levelLabel.y +levelLabel.height +inset,
			iconButtonSize,
			iconButtonSize);

		magicPointCostLabel = new DIYLabel(StringUtil.getUiLabel("ssd.cost", "0"));
		magicPointCostLabel.setBounds(
			column3x +columnWidth/2 -40,
			spellLevel.y +spellLevel.height +inset,
			80, 20);

		this.add(levelLabel);
		this.add(levelMinus);
		this.add(spellLevel);
		this.add(levelPlus);

		this.add(quickName);
		this.add(magicPointCostLabel);
		this.add(spellList);
		this.add(leftPane);
		this.doLayout();

		setPlayerCharacter(pc);
	}

	/*-------------------------------------------------------------------------*/
	public void setPlayerCharacter(PlayerCharacter pc)
	{
		playerCharacter = pc;

		if (pc != null)
		{
			for (Widget w : schoolButtons.keySet())
			{
				String school = schoolButtons.get(w);

				if (playerCharacter.getSpellBook() == null ||
					playerCharacter.getSpellBook().getSpells(school).size() == 0)
				{
					// no spells in this school
					w.setEnabled(false);
				}
				else
				{
					w.setEnabled(true);
				}
			}

			setSchools();
			setSpellLevel(1);
		}
		else
		{
			this.spellLevel.setText("-");
			this.castingLevel = -1;
			this.magicPointCost = -1;
			this.magicPointCostLabel.setText("-");
			this.spellList.setItems(new ArrayList<>());
			setSpell(null);
		}
	}

	/*-------------------------------------------------------------------------*/
	void setSpellLevel(int level)
	{
		this.spellLevel.setText(""+level);
		this.castingLevel = level;
		if (getSpellSelected() != null)
		{
			this.magicPointCost = MagicSys.getInstance().getPointCost(
				getSpellSelected().getMagicPointCost(),
				castingLevel,
				playerCharacter);
			this.magicPointCostLabel.setText(StringUtil.getUiLabel("ssd.cost", magicPointCost));
			
			int nextLevelMagicPointCost = MagicSys.getInstance().getPointCost(
				getSpellSelected().getMagicPointCost(),
				castingLevel + 1,
				playerCharacter);
			
			this.levelMinus.setEnabled(castingLevel != 1);
			this.levelPlus.setEnabled(
				castingLevel != MagicSys.MAX_CASTING_LEVEL &&
				nextLevelMagicPointCost <= playerCharacter.getMagicPoints().getCurrent());
		}
		else
		{
			this.levelMinus.setEnabled(false);
			this.levelPlus.setEnabled(false);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void setSchools()
	{
		if (playerCharacter.getSpellBook() == null)
		{
			setSpell(null);
			this.doLayout();
			return;
		}
		
		filterSpells();
	}
	
	/*-------------------------------------------------------------------------*/
	public void filterSpells()
	{
		List<Spell> allSpells = playerCharacter.getSpellBook().getSpells();
		List<Spell> spells = new ArrayList<Spell>();
		String fragment = quickName.getText().toLowerCase();
		
		for (Spell s : allSpells)
		{
			DIYCheckbox cb = (DIYCheckbox)schoolButtonsBySchool.get(s.getSchool());
			if (cb.isSelected() && s.getDisplayName().toLowerCase().contains(fragment))
			{
				spells.add(s);
			}
		}
		
		spells.sort(spellComparator);
		spellList.setItems(spells);
		Spell firstSpell = null;

		for (Spell s : spells)
		{
			if (playerCharacter.canCast(s))
			{
				if (firstSpell == null)
				{
					firstSpell = s;
				}
			}
			else
			{
				spellList.setEnabled(s, false);
			}
		}

		setSpell(firstSpell);
		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	private void setSpell(Spell spell)
	{
		if (spell != null)
		{
			this.spellList.setSelected(spell);
			this.setSpellSelected(spell);
		}
		else
		{
			this.setSpellSelected(null);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void popupSpellDetailsDialog(Spell spell)
	{
		DiyGuiUserInterface.instance.popupSpellDetailsDialog(spell, playerCharacter);
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		if (e.getID() != KeyEvent.KEY_PRESSED)
		{
			return;
		}

		switch (e.getKeyCode())
		{
			case KeyEvent.VK_1 -> setSpellLevel(1);
			case KeyEvent.VK_2 -> setSpellLevel(2);
			case KeyEvent.VK_3 -> setSpellLevel(3);
			case KeyEvent.VK_4 -> setSpellLevel(4);
			case KeyEvent.VK_5 -> setSpellLevel(5);
			case KeyEvent.VK_6 -> setSpellLevel(6);
			case KeyEvent.VK_7 -> setSpellLevel(7);
			case KeyEvent.VK_EQUALS, KeyEvent.VK_ADD, KeyEvent.VK_PLUS ->
				// The '+' key
				incrementPowerLevel();
			case KeyEvent.VK_MINUS, KeyEvent.VK_SUBTRACT, KeyEvent.VK_UNDERSCORE ->
				// The '-' key
				decrementPowerLevel();
			default -> this.quickName.processKeyPressed(e);
		}
	}

	void decrementPowerLevel()
	{
		if (castingLevel > 1)
		{
			setSpellLevel(castingLevel-1);
		}
	}

	void incrementPowerLevel()
	{
		if (getSpellSelected() != null)
		{
			int nextLevelMagicPointCost = MagicSys.getInstance().getPointCost(
				getSpellSelected().getMagicPointCost(),
				castingLevel + 1,
				playerCharacter);
			if (castingLevel < MagicSys.MAX_CASTING_LEVEL &&
				nextLevelMagicPointCost <= playerCharacter.getMagicPoints().getCurrent())
			{
				setSpellLevel(castingLevel+1);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public Spell getSpellSelected()
	{
		return (Spell)spellList.getSelected();
	}

	/*-------------------------------------------------------------------------*/
	public void setSpellSelected(Spell spellSelected)
	{
		spellList.setSelected(spellSelected);
	}

	public DIYListBox getSpellList()
	{
		return spellList;
	}

	public DIYTextField getQuickName()
	{
		return quickName;
	}

	public int getCastingLevel()
	{
		return castingLevel;
	}

	/*-------------------------------------------------------------------------*/
	private class SpellSelectionActionListener implements ActionListener
	{
		public boolean actionPerformed(ActionEvent e)
		{
			String school = schoolButtons.get(e.getSource());
			if (school != null)
			{
				filterSpells();
				return true;
			}
			else if (e.getSource() == spellList)
			{
				if (e.getEvent() instanceof MouseEvent)
				{
					MouseEvent me = (MouseEvent)e.getEvent();
					Spell spell = (Spell)spellList.getLastClicked();
					if (me.getButton() == MouseEvent.BUTTON3)
					{
						// right click: select + display details
						popupSpellDetailsDialog(spell);
						return true;
					}
				}

				// regardless, reset the casting cost (could have been a KeyEvent)
				setSpellLevel(castingLevel);
			}
			else if (e.getSource() == levelMinus)
			{
				setSpellLevel(castingLevel-1);
				return true;
			}
			else if (e.getSource() == levelPlus)
			{
				setSpellLevel(castingLevel+1);
				return true;
			}

			return false;
		}
	}
}
