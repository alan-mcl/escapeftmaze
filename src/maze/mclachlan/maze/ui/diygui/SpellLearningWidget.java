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
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import mclachlan.diygui.DIYCheckbox;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.DIYScrollPane;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.magic.Spell;

/**
 *
 */
public class SpellLearningWidget extends DIYPane implements ActionListener
{
	private static int inset = 3, titlePaneHeight = 20;
	private static int MIN_ROWS = 20;
	private static SpellComparator comparator = new SpellComparator();

	private PlayerCharacter pc;
	private DIYCheckbox[] boxes;
	private DIYScrollPane scrollPane;
	private DIYLabel spellPicks;
	private int usedPicks = 0;
	private List<Spell> availableSpells;

	/*-------------------------------------------------------------------------*/
	public SpellLearningWidget(PlayerCharacter pc, Rectangle bounds)
	{
		super(bounds);
		buildGui();
		if (pc != null)
		{
			refresh(pc);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void buildGui()
	{
		spellPicks = new DIYLabel();
		spellPicks.setBounds(x+inset, y+inset, width-inset*2, titlePaneHeight);

		this.add(spellPicks);
	}

	/*-------------------------------------------------------------------------*/
	void refresh(PlayerCharacter pc)
	{
		this.pc = pc;
		usedPicks = 0;
		updateSpellPicks();

		if (scrollPane != null)
		{
			this.remove(scrollPane);
		}

		availableSpells = pc.getSpellsThatCanBeLearned();

		Collections.sort(availableSpells, comparator);

		Rectangle bounds = new Rectangle(
			x+inset,
			y+inset*2+titlePaneHeight,
			width-inset*2,
			height-titlePaneHeight-inset*3);

		int cols = 3;
		int rows = Math.max(MIN_ROWS, availableSpells.size());

		DIYPane boxPane = new DIYPane(
			bounds.x,
			bounds.y,
			bounds.width,
			titlePaneHeight*rows);
		boxPane.setLayoutManager(new DIYGridLayout(cols, rows, 0, 0));

		boxes = new DIYCheckbox[availableSpells.size()];

		int count = 0;
		for (;count<boxes.length; count++)
		{
			boxes[count] = new DIYCheckbox(availableSpells.get(count).toString());
			boxes[count].addActionListener(this);
			boxes[count].setActionPayload(availableSpells.get(count));
			boxPane.add(boxes[count]);
		}
		for (;count<rows*cols; count++)
		{
			boxPane.add(new DIYLabel());
		}

		scrollPane = new DIYScrollPane(
			bounds.x,
			bounds.y,
			bounds.width,
			bounds.height,
			boxPane);

		this.add(scrollPane);
		doLayout();
	}

	/*-------------------------------------------------------------------------*/
	java.util.List<Spell> getSelectedSpells()
	{
		ArrayList<Spell> result = new ArrayList<Spell>();

		if (boxes != null)
		{
			for (DIYCheckbox box : boxes)
			{
				if (box.isSelected())
				{
					result.add((Spell)box.getActionPayload());
				}
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<Spell> getAvailableSpells()
	{
		return availableSpells;
	}

	/*-------------------------------------------------------------------------*/
	private void updateSpellPicks()
	{
		spellPicks.setText("Spell Picks Remaining: "+(pc.getSpellPicks() - usedPicks));
		refreshBoxState();
	}

	/*-------------------------------------------------------------------------*/
	private void popupSpellDetailsDialog(Spell spell)
	{
		DiyGuiUserInterface.instance.popupSpellDetailsDialog(spell, null);
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() instanceof DIYCheckbox)
		{
			DIYCheckbox cb = (DIYCheckbox)event.getSource();
			MouseEvent me = (MouseEvent)event.getEvent();
			Spell spell = (Spell)cb.getActionPayload();

			if (me.getButton() == MouseEvent.BUTTON1)
			{
				// left click: select spell
				if (cb.isSelected())
				{
					// box has just been selected
					usedPicks++;
					updateSpellPicks();
					return true;
				}
				else
				{
					// box has just been deselected
					usedPicks--;
					updateSpellPicks();
					return true;
				}
			}
			else if (me.getButton() == MouseEvent.BUTTON3)
			{
				// right click: display details
				popupSpellDetailsDialog(spell);
				// hack: undo the right click selection
				cb.setSelected(!cb.isSelected());
				return true;
			}
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private void refreshBoxState()
	{
		if (boxes == null || pc == null)
		{
			return;
		}

		if (pc.getSpellPicks() - usedPicks < 1)
		{
			for (DIYCheckbox box : boxes)
			{
				box.setEnabled(box.isSelected());
			}
		}
		else if (pc.getSpellPicks() - usedPicks > 0)
		{
			for (DIYCheckbox box : boxes)
			{
				box.setEnabled(true);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void clear()
	{
		this.usedPicks = 0;
		updateSpellPicks();

		for (DIYCheckbox box : boxes)
		{
			box.setSelected(false);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setSelectedSpells(List<Spell> spells)
	{
		for (DIYCheckbox box : boxes)
		{
			if (spells.contains((Spell)box.getActionPayload()))
			{
				box.setSelected(true);
				usedPicks++;
			}
		}

		updateSpellPicks();
	}

	public PlayerCharacter getPlayerCharacter()
	{
		return pc;
	}

	/*-------------------------------------------------------------------------*/
	static class SpellComparator implements Comparator
	{
		public int compare(Object o1, Object o2)
		{
			Spell s1 = (Spell)o1;
			Spell s2 = (Spell)o2;

			if (s1.getLevel() != s2.getLevel())
			{
				// first sort key: level
				return s1.getLevel() - s2.getLevel();
			}
			else
			{
				// second sort key: name
				return s1.getName().compareTo(s2.getName());
			}
		}
	}
}
