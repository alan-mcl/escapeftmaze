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
	private DIYButton levelPlus, levelMinus;
	private Map<Widget, String> schoolButtons = new HashMap<Widget, String>();
	private Map<String, Widget> schoolButtonsBySchool = new HashMap<String, Widget>();
	private PlayerCharacter playerCharacter;
	private DIYListBox spellList;
	private DIYTextField quickName;
	private DIYLabel magicPointCostLabel, spellLevel;

	private int castingLevel = -1;
	private int magicPointCost = -1;

	private static Comparator<Spell> spellComparator = new SpellLearningWidget.SpellComparator();

	/*-------------------------------------------------------------------------*/
	public SpellDisplayWidget(PlayerCharacter pc, Rectangle bounds)
	{
		super(bounds);

		List<String> spellSchools = MagicSys.getInstance().getSpellSchools();
		DIYPane schoolPane = new DIYPane(
			new DIYGridLayout(1, height/20, 0, 0));
		schoolPane.setBounds(x,y,width/3, height);
		int inset = 10;
		schoolPane.setInsets(new Insets(inset, inset, inset, inset));

		schoolPane.add(new DIYLabel("Select School:"));

		SpellSelectionActionListener internalListener = new SpellSelectionActionListener();
		for (String school : spellSchools)
		{
			DIYCheckbox b = new DIYCheckbox(school);
			schoolPane.add(b);
			schoolButtons.put(b, school);
			schoolButtonsBySchool.put(school, b);
			b.addActionListener(internalListener);

			b.setSelected(true);
		}

		quickName = new DIYTextField("", 15);
		schoolPane.add(new DIYLabel());
		schoolPane.add(quickName);

		DIYLabel levelLabel = new DIYLabel("Level:");
		levelLabel.setBounds(x+width/3, y+ inset, 40, 20);

		levelMinus = new DIYButton("-");
		levelMinus.addActionListener(internalListener);
		levelPlus = new DIYButton("+");
		levelPlus.addActionListener(internalListener);
		spellLevel = new DIYLabel("");

		levelMinus.setBounds(x+width/3+40+3, y+ inset +3, 14, 14);
		spellLevel.setBounds(x+width/3+60, y+ inset, 20, 20);
		levelPlus.setBounds(x+width/3+80+3, y+ inset +3, 14, 14);

		magicPointCostLabel = new DIYLabel("Cost: ");
		magicPointCostLabel.setBounds(x+width/3+100, y+ inset, 75, 20);

		List<String> spells = new ArrayList<String>();
		spellList = new DIYListBox(spells);
		spellList.setBounds(x+width/3, y+ inset +25, width/3*2, height- inset *2-25);
		spellList.addActionListener(internalListener);

		this.add(levelLabel);
		this.add(levelMinus);
		this.add(spellLevel);
		this.add(levelPlus);
		this.add(quickName);
		this.add(magicPointCostLabel);
		this.add(spellList);
		this.add(schoolPane);
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
			this.spellList.setItems(new ArrayList());
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
			this.magicPointCostLabel.setText("Cost: "+magicPointCost);
			
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
		
		Collections.sort(spells, spellComparator);
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

		switch(e.getKeyCode())
		{
			case KeyEvent.VK_1:
				setSpellLevel(1);
				break;
			case KeyEvent.VK_2:
				setSpellLevel(2);
				break;
			case KeyEvent.VK_3:
				setSpellLevel(3);
				break;
			case KeyEvent.VK_4:
				setSpellLevel(4);
				break;
			case KeyEvent.VK_5:
				setSpellLevel(5);
				break;
			case KeyEvent.VK_6:
				setSpellLevel(6);
				break;
			case KeyEvent.VK_7:
				setSpellLevel(7);
				break;
			case KeyEvent.VK_EQUALS:
			case KeyEvent.VK_ADD:
			case KeyEvent.VK_PLUS:
				// The '+' key
				incrementPowerLevel();
				break;
			case KeyEvent.VK_MINUS:
			case KeyEvent.VK_SUBTRACT:
			case KeyEvent.VK_UNDERSCORE:
				// The '-' key
				decrementPowerLevel();
				break;
			default:
				this.quickName.processKeyPressed(e);
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
		public void actionPerformed(ActionEvent e)
		{
			String school = schoolButtons.get(e.getSource());
			if (school != null)
			{
				filterSpells();
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
					}
				}

				// regardless, reset the casting cost (could have been a KeyEvent)
				setSpellLevel(castingLevel);
			}
			else if (e.getSource() == levelMinus)
			{
				setSpellLevel(castingLevel-1);
			}
			else if (e.getSource() == levelPlus)
			{
				setSpellLevel(castingLevel+1);
			}
		}
	}
}
