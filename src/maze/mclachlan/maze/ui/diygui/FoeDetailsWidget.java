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
import java.awt.Font;
import java.awt.Rectangle;
import java.util.*;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.data.StringUtil.getUiLabel;

/**
 * To be used as the popup dialog to display foe details.
 */
public class FoeDetailsWidget extends DIYPane
{
	private static String UNKNOWN;
	private int wrapWidth;

	/*-------------------------------------------------------------------------*/
	public FoeDetailsWidget(Rectangle bounds, Foe foe, int information)
	{
		super(bounds);
		UNKNOWN = StringUtil.getUiLabel("fdw.unknown");
		buildGui(bounds, foe, information);
	}

	/*-------------------------------------------------------------------------*/
	private void buildGui(Rectangle bounds, Foe foe, int information)
	{
		int inset = 2;
		int border = 15;
		int titleWidth = 30;

		int rowHeight = 12;
		int xx = bounds.x + inset + border;
		int yy = bounds.y + inset + border + titleWidth;
		int width1 = bounds.width - inset * 2 - border * 2;
		int height1 = bounds.height - inset * 2 - border * 2;

		wrapWidth = width1;

		// foe name
		String displayName;
		displayName = getDisplayName(foe, information);
		DIYLabel nameLabel = new DIYLabel(displayName);
		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.BOLD, defaultFont.getSize()+2);
		nameLabel.setFont(f);
		nameLabel.setForegroundColour(Constants.Colour.GOLD);
		addRelative(nameLabel, 46, 8, 540, 36);

		// rows of item details
		List<Widget> rows = new ArrayList<Widget>();
		newRow(rows);

		boolean b = false;

		// differs per item type
		b |= addFoeFields(rows, foe, information);

		if (b)
		{
			newRow(rows);
		}
		b = false;

		DIYPane pane = new DIYPane(xx, yy, width1, rows.size()*rowHeight);
		pane.setLayoutManager(new DIYGridLayout(1, rows.size(), 0, 0));

		for (int i=0; i<rows.size(); i++)
		{
			pane.add(rows.get(i));
		}
		pane.doLayout();

		this.add(pane);
	}

	/*-------------------------------------------------------------------------*/
	private String getDisplayName(Foe foe, int information)
	{
		String displayName;
		if (information >= 0)
		{
			displayName = foe.getName();
		}
		else
		{
			displayName = foe.getUnidentifiedName();
		}
		return displayName;
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
	private void addRelative(Widget w, int x, int y, int width, int height)
	{
		w.setBounds(this.x + x, this.y + y, width, height);
		this.add(w);
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
	private boolean addFoeFields(List<Widget> rows, Foe foe, int information)
	{
		// foe level
		addToRow(rows, new DIYLabel(getFoeLevel(foe, information)));
		newRow(rows);
		addToRow(rows, new DIYLabel(getFoeType(foe, information)));
		newRow(rows);
		addToRow(rows, new DIYLabel(getHpApMp(foe, information)));
		newRow(rows);
		addEquipment(rows, foe, information);
		newRow(rows);
		addAttributeModifiers(rows, foe, information);
		newRow(rows);
		addResistances(rows, foe, information);
		newRow(rows);
		addOtherModifiers(rows, foe, information);
		newRow(rows);
		addSpecialAbilities(rows, foe, information);
		newRow(rows);
		addSpellBook(rows, foe, information);
		newRow(rows);

		return true;
	}

	/*-------------------------------------------------------------------------*/
	private void addEquipment(List<Widget> rows, Foe foe, int information)
	{
		if (information >= 3)
		{
			List<Item> equippedItems = foe.getEquippedItems();
			if (equippedItems != null && !equippedItems.isEmpty())
			{
				addToRow(rows, new DIYLabel(StringUtil.getUiLabel("fdw.equipped.items")));

				for (Item item : equippedItems)
				{
					addToRow(rows, new DIYLabel(item.getDisplayName()));
				}
			}
		}
		else
		{
			addToRow(rows, new DIYLabel(UNKNOWN));
		}
	}

	/*-------------------------------------------------------------------------*/
	private void addSpellBook(List<Widget> rows, Foe foe, int information)
	{
		if (information >= 10)
		{
			List<Spell> spells = foe.getSpellBook().getSpells();
			if (spells != null && !spells.isEmpty())
			{
				addToRow(rows, new DIYLabel(StringUtil.getUiLabel("fdw.spells")));

				for (Spell spell : spells)
				{
					addToRow(rows, new DIYLabel(spell.getDisplayName()));
				}
			}
		}
		else
		{
			addToRow(rows, new DIYLabel(UNKNOWN));
		}
	}

	/*-------------------------------------------------------------------------*/
	private void addSpecialAbilities(List<Widget> rows, Foe foe, int information)
	{
		if (information >= 9)
		{
			List<NaturalWeapon> naturalWeapons = foe.getNaturalWeapons();
			if (naturalWeapons != null && !naturalWeapons.isEmpty())
			{
				addToRow(rows, new DIYLabel(StringUtil.getUiLabel("fdw.natural.weapons")));

				for (NaturalWeapon nw : naturalWeapons)
				{
					addToRow(rows, new DIYLabel(nw.getDisplayName()));
				}
				newRow(rows);
			}

			List<SpellLikeAbility> spellLikeAbilities = foe.getSpellLikeAbilities();
			if (spellLikeAbilities != null && !spellLikeAbilities.isEmpty())
			{
				addToRow(rows, new DIYLabel(StringUtil.getUiLabel("fdw.special.abilities")));

				for (SpellLikeAbility sla : spellLikeAbilities)
				{
					addToRow(rows, new DIYLabel(sla.getDisplayName()));
				}
			}
		}
		else
		{
			addToRow(rows, new DIYLabel(UNKNOWN));
		}
	}

	/*-------------------------------------------------------------------------*/
	private void addOtherModifiers(List<Widget> rows, Foe foe, int information)
	{
		Map<String, Integer> modifiers = new HashMap<String, Integer>();

		if (information >= 8)
		{
			for (String s : Stats.middleModifiers)
			{
				if (foe.getModifier(s) != 0)
				{
					modifiers.put(s, foe.getModifier(s));
				}
			}

			addModifiers(modifiers, rows, StringUtil.getUiLabel("fdw.other.modifiers"));
		}
		else
		{
			addToRow(rows, new DIYLabel(UNKNOWN));
		}
	}

	/*-------------------------------------------------------------------------*/
	private void addAttributeModifiers(List<Widget> rows, Foe foe, int information)
	{
		Map<String, Integer> modifiers = new HashMap<String, Integer>();

		if (information >= 5)
		{
			if (information >= 6)
			{
				modifiers.put(Stats.Modifiers.SKILL, foe.getModifier(Stats.Modifiers.SKILL));
				modifiers.put(Stats.Modifiers.THIEVING, foe.getModifier(Stats.Modifiers.THIEVING));
				modifiers.put(Stats.Modifiers.POWER, foe.getModifier(Stats.Modifiers.POWER));
			}

			modifiers.put(Stats.Modifiers.BRAWN, foe.getModifier(Stats.Modifiers.BRAWN));
			modifiers.put(Stats.Modifiers.SNEAKING, foe.getModifier(Stats.Modifiers.SNEAKING));
			modifiers.put(Stats.Modifiers.BRAINS, foe.getModifier(Stats.Modifiers.BRAINS));

			addModifiers(modifiers, rows, StringUtil.getUiLabel("fdw.attribute.modifiers"));
		}
		else
		{
			addToRow(rows, new DIYLabel(UNKNOWN));
		}
	}

	/*-------------------------------------------------------------------------*/
	private void addResistances(List<Widget> rows, Foe foe, int information)
	{
		Map<String, Integer> modifiers = new HashMap<String, Integer>();

		if (information >= 7)
		{
			modifiers.put(Stats.Modifiers.RESIST_AIR, foe.getModifier(Stats.Modifiers.RESIST_AIR));
			modifiers.put(Stats.Modifiers.RESIST_EARTH, foe.getModifier(Stats.Modifiers.RESIST_EARTH));
			modifiers.put(Stats.Modifiers.RESIST_ENERGY, foe.getModifier(Stats.Modifiers.RESIST_EARTH));
			modifiers.put(Stats.Modifiers.RESIST_FIRE, foe.getModifier(Stats.Modifiers.RESIST_FIRE));
			modifiers.put(Stats.Modifiers.RESIST_WATER, foe.getModifier(Stats.Modifiers.RESIST_WATER));
			modifiers.put(Stats.Modifiers.RESIST_MENTAL, foe.getModifier(Stats.Modifiers.RESIST_MENTAL));
			modifiers.put(Stats.Modifiers.RESIST_BLUDGEONING, foe.getModifier(Stats.Modifiers.RESIST_BLUDGEONING));
			modifiers.put(Stats.Modifiers.RESIST_PIERCING, foe.getModifier(Stats.Modifiers.RESIST_PIERCING));
			modifiers.put(Stats.Modifiers.RESIST_SLASHING, foe.getModifier(Stats.Modifiers.RESIST_SLASHING));

			addModifiers(modifiers, rows, StringUtil.getUiLabel("fdw.resistances"));
		}
		else
		{
			addToRow(rows, new DIYLabel(UNKNOWN));
		}
	}

	/*-------------------------------------------------------------------------*/
	private String getHpApMp(Foe foe, int information)
	{
		String result;
		if (information >= 4)
		{
			CurMax hp = foe.getHitPoints();
			CurMax ap = foe.getActionPoints();
			CurMax mp = foe.getMagicPoints();
			result = getUiLabel("fdw.hpapmp",
				hp.getCurrent(),
				hp.getMaximum(),
				ap.getCurrent(),
				ap.getMaximum(),
				mp.getCurrent(),
				mp.getMaximum());
		}
		else if (information >= 2)
		{
			CurMax hp = foe.getHitPoints();
			CurMax ap = foe.getActionPoints();
			CurMax mp = foe.getMagicPoints();
			result = getUiLabel("fdw.hpapmp",
				UNKNOWN,
				hp.getMaximum(),
				UNKNOWN,
				ap.getMaximum(),
				UNKNOWN,
				mp.getMaximum());
		}
		else
		{
			result = getUiLabel("fdw.hpapmp",
				UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN);
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private String getFoeType(Foe foe, int information)
	{
		String uiLabel;
		if (information >= 1)
		{
			uiLabel = getUiLabel("fdw.type", foe.getType());
		}
		else
		{
			uiLabel = getUiLabel("fdw.type", UNKNOWN);
		}
		return uiLabel;
	}

	/*-------------------------------------------------------------------------*/
	private String getFoeLevel(Foe foe, int information)
	{
		String uiLabel;
		if (information >= 1)
		{
			uiLabel = getUiLabel("fdw.level", foe.getLevel());
		}
		else
		{
			uiLabel = getUiLabel("fdw.level", UNKNOWN);
		}
		return uiLabel;
	}

	/*-------------------------------------------------------------------------*/
	private boolean addModifiers(
		Map<String, Integer> modifiers,
		List<Widget> rows,
		String title)
	{
		if (modifiers.size() == 0)
		{
			// no modifiers section
			return false;
		}

		List<String> sortedModifiers = new ArrayList<String>(modifiers.keySet());
		Collections.sort(sortedModifiers);

		StringBuilder sb = new StringBuilder(title+" ");
		List<String> modDesc = new ArrayList<String>();

		for (String modifier : sortedModifiers)
		{
			int value = modifiers.get(modifier);

			modDesc.add(descModifier(modifier, value));
		}

		getCommaString(sb, modDesc);
		wrapString(rows, sb);
		return true;
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
	private DIYLabel getDescriptiveLabel(String s)
	{
		return getLabel(s, Constants.Colour.SILVER);
	}

	/*-------------------------------------------------------------------------*/
	private DIYLabel getTypeLabel(String s)
	{
		return getLabel(s, Color.WHITE);
	}

	/*-------------------------------------------------------------------------*/
	private String descModifier(String modifier, int value)
	{
		String modifierName = StringUtil.getModifierName(modifier);

		Stats.ModifierMetric metric = Stats.ModifierMetric.getMetric(modifier);
		switch (metric)
		{
			case PLAIN:
				return modifierName + " " + descPlainModifier(value);
			case BOOLEAN:
				if (value >= 0)
				{
					return modifierName;
				}
				else
				{
					return getUiLabel("iw.cancel")+" "+modifierName;
				}
			case PERCENTAGE:
				return modifierName + " " + descPlainModifier(value)+"%";
			default:
				throw new MazeException(metric.name());
		}
	}

	/*-------------------------------------------------------------------------*/
	private String descPlainModifier(int value)
	{
		if (value >= 0)
		{
			return "+"+value;
		}
		else
		{
			return ""+value;
		}
	}
}
