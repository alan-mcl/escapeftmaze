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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.*;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.DIYTextArea;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.SpellEffect;

public class SpellDetailsDialog extends GeneralDialog
{
	private final int wrapWidth;
	private DIYLabel requiredManaLabel = null;
	private DIYLabel availableManaLabel = null;
	private ManaDisplayWidget manaRequired, manaAvailable;

	/*-------------------------------------------------------------------------*/
	public SpellDetailsDialog(Rectangle bounds, Spell spell, PlayerCharacter pc)
	{
		super(bounds);

		DIYButton close = getCloseButton();

		close.addActionListener(event -> {
			DIYToolkit.getInstance().clearDialog();
			return true;
		});

		manaRequired = new ManaDisplayWidget("required");
		manaRequired.refresh(spell.getRequirementsToCast());

		manaAvailable = new ManaDisplayWidget("present");
		if (pc != null)
		{
			manaAvailable.refresh(
				pc.getModifier(Stats.Modifier.RED_MAGIC_GEN),
				pc.getModifier(Stats.Modifier.BLACK_MAGIC_GEN),
				pc.getModifier(Stats.Modifier.PURPLE_MAGIC_GEN),
				pc.getModifier(Stats.Modifier.GOLD_MAGIC_GEN),
				pc.getModifier(Stats.Modifier.WHITE_MAGIC_GEN),
				pc.getModifier(Stats.Modifier.GREEN_MAGIC_GEN),
				pc.getModifier(Stats.Modifier.BLUE_MAGIC_GEN));
		}

		int xx = bounds.x + getInset() + getBorder();
		int yy = bounds.y + getInset() + getBorder() + getTitlePaneHeight();
		int width1 = bounds.width - getInset() * 2 - getBorder() * 2;
		int height1 = bounds.height - getInset() * 3 - getBorder() * 2;

		wrapWidth = width1;

		DIYPane title = getTitlePane(spell.getDisplayName());

		int rowHeight = 15;

		// rows of item details
		List<Widget> rows = new ArrayList<>();

		newRow(rows);

		addToRow(rows, getLabel(StringUtil.getUiLabel("sdd.spell.level", spell.getLevel())));
		newRow(rows);

		if (spell.getHitPointCost() != null)
		{
			addToRow(rows, getLabel(StringUtil.getUiLabel("sdd.hit.point.cost",
				StringUtil.descValue(spell.getHitPointCost()))));
			newRow(rows);
		}

		if (spell.getActionPointCost() != null)
		{
			addToRow(rows, getLabel(StringUtil.getUiLabel("sdd.action.point.cost",
				StringUtil.descValue(spell.getActionPointCost()))));
			newRow(rows);
		}

		if (spell.getMagicPointCost() != null)
		{
			addToRow(rows, getLabel(StringUtil.getUiLabel("sdd.magic.point.cost",
				StringUtil.descValue(spell.getMagicPointCost()))));
			newRow(rows);
		}

		addToRow(rows, getLabel(StringUtil.getUiLabel("sdd.target.type",
			StringUtil.descSpellTargetType(spell.getTargetType()))));
		newRow(rows);

		if (spell.isProjectile())
		{
			addToRow(rows, getLabel(StringUtil.getUiLabel("sdd.projectile")));
			newRow(rows);
		}

		addToRow(rows, getLabel(StringUtil.getUiLabel("sdd.usability.type",
			StringUtil.descSpellUsabilityType(spell.getUsabilityType()))));
		newRow(rows);

		newRow(rows);

		addToRow(rows, getLabel(StringUtil.getUiLabel("sdd.book", spell.getBook().getName())));
		newRow(rows);

		addToRow(rows, getLabel(StringUtil.getUiLabel("sdd.school", spell.getSchool())));
		newRow(rows);

		addToRow(rows, getLabel(StringUtil.getUiLabel("sdd.primary.modifier",
			StringUtil.getModifierName(spell.getPrimaryModifier()))));
		newRow(rows);

		addToRow(rows, getLabel(StringUtil.getUiLabel("sdd.secondary.modifier",
			StringUtil.getModifierName(spell.getSecondaryModifier()))));
		newRow(rows);

		List<String> effects = getSpellEffectsDisplay(spell);
		if (!effects.isEmpty())
		{
			StringBuilder sb = new StringBuilder(StringUtil.getUiLabel("sdd.effects"));
			getCommaString(sb, effects);
			wrapString(rows, sb);
		}

		newRow(rows);

		requiredManaLabel = getLabel(StringUtil.getUiLabel("sdd.mana.required"));
		addToRow(rows, requiredManaLabel);
//		addToRow(rows, manaRequired);
		newRow(rows);

		if (pc != null)
		{
			newRow(rows);

			availableManaLabel = getLabel(StringUtil.getUiLabel("sdd.mana.available"));
			addToRow(rows, availableManaLabel);
//			addToRow(rows, manaAvailable);
			newRow(rows);
		}

		//
		// Layout the rows on the pane
		//

		DIYPane pane = new DIYPane(xx, yy, width1, rows.size() * rowHeight);
		pane.setLayoutManager(new DIYGridLayout(1, rows.size(), 0, 0));

		for (int i = 0; i < rows.size(); i++)
		{
			pane.add(rows.get(i));
		}
		pane.doLayout();

		// post layout math

		DIYTextArea desc = new DIYTextArea(spell.getDescription());
		desc.setBounds(xx, yy + (rows.size() * rowHeight), width1, height1 / 5);
		desc.setTransparent(true);

		Dimension mrps = manaRequired.getPreferredSize();
		manaRequired.setBounds(
			requiredManaLabel.x + requiredManaLabel.width + getInset(),
			requiredManaLabel.y +requiredManaLabel.height/2 - mrps.height/2,
			mrps.width,
			mrps.height);
		manaRequired.doLayout();

		if (pc != null)
		{
			Dimension maps = manaAvailable.getPreferredSize();
			manaAvailable.setBounds(
				availableManaLabel.x + availableManaLabel.width + getInset(),
				availableManaLabel.y + availableManaLabel.height / 2 - maps.height / 2,
				maps.width,
				maps.height);
			manaAvailable.doLayout();
		}

		this.add(title);
		this.add(pane);
		this.add(manaRequired);
		if (pc != null)
		{
			this.add(manaAvailable);
		}
		this.add(desc);
		this.add(close);
	}

	/*-------------------------------------------------------------------------*/
	private void wrapString(List<Widget> rows, StringBuilder sb)
	{
		List<String> strings = DIYToolkit.wrapText(
			sb.toString(), Maze.getInstance().getComponent().getGraphics(), wrapWidth);

		for (String s : strings)
		{
			addToRow(rows, getLabel(s));
			newRow(rows);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void getCommaString(StringBuilder sb, List<String> users)
	{
		boolean commaEd = false;

		for (String user : users)
		{
			if (commaEd)
			{
				sb.append(", ");
			}
			commaEd = true;
			sb.append(user);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void addToRow(List<Widget> rows, Widget w)
	{
		Widget widget = rows.get(rows.size() - 1);
		((ContainerWidget)widget).add(w);
	}

	/*-------------------------------------------------------------------------*/
	private void newRow(List<Widget> rows)
	{
		rows.add(new DIYPane(new DIYFlowLayout(4, 0, DIYToolkit.Align.LEFT)));
	}

	/*-------------------------------------------------------------------------*/
	private DIYLabel getLabel(String s)
	{
		return new DIYLabel(s, DIYToolkit.Align.LEFT);
	}

	/*-------------------------------------------------------------------------*/
	private DIYLabel getLabel(String s, Color c)
	{
		DIYLabel result = new DIYLabel(s, DIYToolkit.Align.LEFT);
		result.setForegroundColour(c);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.PANEL;
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE ||
			e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			Maze.getInstance().getUi().clearDialog();
		}
	}

	/*-------------------------------------------------------------------------*/
	private java.util.List<String> getSpellEffectsDisplay(Spell item)
	{
		java.util.List<String> result = new ArrayList<>();

		if (item.getEffects() == null || item.getEffects().getPercentages().size() == 0)
		{
			return result;
		}

		// we're only going to display spell effects that have display names
		for (SpellEffect se : item.getEffects().getPossibilities())
		{
			if (se.getDisplayName() != null && se.getDisplayName().length() > 0)
			{
				result.add(item.getEffects().getPercentage(se) + "% " + se.getDisplayName());
			}
		}

		return result;
	}
}
