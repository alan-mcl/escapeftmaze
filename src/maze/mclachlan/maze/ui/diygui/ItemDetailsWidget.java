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
import java.util.*;
import java.util.List;
import mclachlan.diygui.*;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.SpellEffect;
import mclachlan.maze.util.MazeException;

/**
 * To be used as the popup dialog to display item details.
 */
public class ItemDetailsWidget extends DIYPanel
{
	private int wrapWidth;

	/*-------------------------------------------------------------------------*/
	public ItemDetailsWidget(Rectangle bounds, Item item)
	{
		super(bounds);
		buildGui(bounds, item);
	}

	/*-------------------------------------------------------------------------*/
	private void buildGui(Rectangle bounds, Item item)
	{
		int okButtonHeight = 17;
		int okButtonWidth = bounds.width / 4;
		int inset = 2;
		int border = 15;
		int titleWidth = 30;

		int rowHeight = 12;
		int xx = bounds.x + inset + border;
		int yy = bounds.y + inset + border + titleWidth;
		int width1 = bounds.width - inset * 2 - border * 2;
		int height1 = bounds.height - okButtonHeight - inset * 3 - border * 2;

		wrapWidth = width1;

		DIYButton ok = new DIYButton(StringUtil.getUiLabel("common.ok"));
		ok.setBounds(new Rectangle(
			bounds.x+ bounds.width/2 - okButtonWidth /2,
			bounds.y+ bounds.height - okButtonHeight - inset - border,
			okButtonWidth, okButtonHeight));

		ok.setActionMessage(Constants.Messages.DISPOSE_DIALOG);

		// Item name
		DIYLabel nameLabel = new DIYLabel(item.getDisplayName());
		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.BOLD, defaultFont.getSize()+2);
		nameLabel.setFont(f);
		if (item.isCursed() &&
			item.getCursedState() == Item.CursedState.DISCOVERED)
		{
			nameLabel.setForegroundColour(Color.RED);
		}
		else
		{
			nameLabel.setForegroundColour(Constants.Colour.GOLD);
		}
		addRelative(nameLabel, 46, 8, 540, 36);

		// Item image
		DIYLabel itemIcon = new DIYLabel(Database.getInstance().getImage(item.getImage()));
		addRelative(itemIcon, 11, 11, 35, 35);

		// Item weight
		DIYLabel weightLabel = new DIYLabel(
			StringUtil.getUiLabel("idw.weight",
				Constants.Format.formatWeight(item.getWeight())));
		Dimension dw = weightLabel.getPreferredSize();
		weightLabel.setBounds(xx +width1 -dw.width, yy, dw.width, dw.height);
		this.add(weightLabel);

		// Current stack
		if (item.getStack().getMaximum() > 1)
		{
			CurMax stack = item.getStack();
			DIYLabel stackLabel = new DIYLabel(
				StringUtil.getUiLabel("idw.stack", stack.getCurrent(), stack.getMaximum()));
			Dimension ds = weightLabel.getPreferredSize();
			stackLabel.setBounds(xx + width1 - ds.width, yy + rowHeight, ds.width, ds.height);
			this.add(stackLabel);
		}

		// rows of item details
		List<Widget> rows = new ArrayList<Widget>();
		newRow(rows);

		boolean b = false;

		// differs per item type
		b |= addItemSpecificFields(rows, item);

		// which slots this item can be equipped in
		b |= addEquipableSlots(rows, item);

		// ammo required
		b |= addRequiredAmmo(rows, item);

		if (b)
		{
			newRow(rows);
		}
		b = false;

		// add spell effects of the item
		b |= addSpellEffects(rows, item);

		// adds the invoked spell of the item
		b |= addInvokedSpell(rows, item);

		// list of modifiers
		b |= addItemModifiers(rows, item);

		if (b)
		{
			newRow(rows);
		}
		b = false;

		// modifier requirements
		b |= addItemRequirements(rows, item);
		
		// users
		b |= addUsableByGenders(rows, item);
		b |= addUsableByRaces(rows, item);
		b |= addUsableByClasses(rows, item);

		DIYPane pane = new DIYPane(xx, yy, width1, rows.size()*rowHeight);
		pane.setLayoutManager(new DIYGridLayout(1, rows.size(), 0, 0));

		for (int i=0; i<rows.size(); i++)
		{
			pane.add(rows.get(i));
		}
		pane.doLayout();

		this.add(pane);

		if (item.getIdentificationState() == Item.IdentificationState.IDENTIFIED)
		{
			String text = item.getDescription();

			if (text != null)
			{
				DIYTextArea desc = new DIYTextArea(item.getDescription());
				desc.setBounds(xx, yy+(rows.size()*rowHeight), width1, height1/5);
				desc.setTransparent(true);
				this.add(desc);
			}
		}

		this.add(ok);

		Image back = Database.getInstance().getImage("screen/item_dialog_back");
		this.setBackgroundImage(back);
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
	private boolean addInvokedSpell(List<Widget> rows, Item item)
	{
		if (item.getIdentificationState() == Item.IdentificationState.IDENTIFIED)
		{
			if (item.getInvokedSpell() == null)
			{
				return false;
			}

			if (item.getType() == Item.Type.SPELLBOOK)
			{
				String s = StringUtil.getUiLabel("idw.teaches.spell",
					item.getInvokedSpell().getDisplayName(), item.getInvokedSpell().getBook().getName());

				addToRow(rows, getLabel(s));
				newRow(rows);
				return true;
			}
			else
			{
				String s = "Invoked Spell: "+item.getInvokedSpell().getDisplayName();
				int level = item.getInvokedSpellLevel();
				if (level == 0)
				{
					s += " (variable level";
				}
				else
				{
					s += " (level "+ level;
				}
				CurMax charges = item.getCharges();
				if (charges != null && charges.getMaximum() > 0)
				{
					s += ", charges " + charges.getCurrent() + " of " + charges.getMaximum();
				}
				else
				{
					s += ", charges infinite";
				}
				s += ")";
				addToRow(rows, getLabel(s));
				newRow(rows);
				return true;
			}
		}
		else
		{
			return false;
		}
	}

	/*-------------------------------------------------------------------------*/
	private boolean addSpellEffects(List<Widget> rows, Item item)
	{
		if (item.getIdentificationState() == Item.IdentificationState.IDENTIFIED)
		{
			List<String> displayList = getSpellEffectsDisplay(item);
			if (displayList.isEmpty())
			{
				return false;
			}

			StringBuilder sb = new StringBuilder("Effects: ");
			getCommaString(sb, displayList);
			wrapString(rows, sb);
			return true;
		}
		else
		{
			return false;
		}
	}

	/*-------------------------------------------------------------------------*/
	private List<String> getSpellEffectsDisplay(Item item)
	{
		List<String> result = new ArrayList<String>();

		if (item.getSpellEffects() == null || item.getSpellEffects().getPercentages().size() == 0)
		{
			return result;
		}

		// we're only going to display spell effects that have display names
		for (SpellEffect se : item.getSpellEffects().getPossibilities())
		{
			if (se.getDisplayName() != null && se.getDisplayName().length() > 0)
			{
				result.add(item.getSpellEffects().getPercentage(se)+"% "+se.getDisplayName());
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private boolean buildUsability(List<Widget> rows, StringBuilder sb,
		List<String> all, Set<String> itemList)
	{
		if (itemList == null || itemList.size() == all.size())
		{
			sb.append("all");
		}
		else
		{
			float max = all.size();
			float actual = itemList.size();

			List<String> users = new ArrayList<String>(itemList);

			if (actual/max >= 0.66f)
			{
				List<String> exceptions = new ArrayList<String>(all);
				exceptions.removeAll(users);
				Collections.sort(exceptions);
				sb.append("all except ");
				getCommaString(sb, exceptions);
			}
			else
			{
				Collections.sort(users);
				getCommaString(sb, users);
			}
		}

		wrapString(rows, sb);
		return true;
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
	private boolean addUsableByRaces(List<Widget> rows, Item item)
	{
		StringBuilder sb = new StringBuilder("Usable by races: ");
		return buildUsability(rows, sb,
			Database.getInstance().getRaceList(), item.getUsableByRace());
	}

	/*-------------------------------------------------------------------------*/
	private boolean addUsableByGenders(List<Widget> rows, Item item)
	{
		StringBuilder sb = new StringBuilder("Usable by genders: ");
		return buildUsability(rows, sb,
			Database.getInstance().getGenderList(), item.getUsableByGender());
	}

	/*-------------------------------------------------------------------------*/
	private boolean addUsableByClasses(List<Widget> rows, Item item)
	{
		StringBuilder sb = new StringBuilder("Usable by classes: ");
		return buildUsability(rows, sb,
			Database.getInstance().getCharacterClassList(), item.getUsableByCharacterClass());
	}

	/*-------------------------------------------------------------------------*/
	private boolean addEquipableSlots(List<Widget> rows, Item item)
	{
		BitSet slots = item.getEquipableSlots();

		if (slots==null || slots.isEmpty())
		{
			return false;
		}

		addToRow(rows, getDescriptiveLabel("Slots:"));
		addToRow(rows, getLabel(describeSlots(slots)));
		newRow(rows);
		return true;
	}

	/*-------------------------------------------------------------------------*/
	private boolean addRequiredAmmo(List<Widget> rows, Item item)
	{
		List<ItemTemplate.AmmoType> ammoRequired = item.getAmmoRequired();

		if (ammoRequired==null || ammoRequired.isEmpty() ||
			(ammoRequired.size()==1 && ammoRequired.contains(ItemTemplate.AmmoType.SELF)))
		{
			return false;
		}

		List<ItemTemplate.AmmoType> temp = new ArrayList<ItemTemplate.AmmoType>(ammoRequired);
		Collections.sort(temp);

		addToRow(rows, getDescriptiveLabel("Ammo Required:"));

		boolean first = true;
		for (ItemTemplate.AmmoType at : temp)
		{
			String s = at.name().toLowerCase();
			if (!first)
			{
				s = ", "+s;
				first = false;
			}
			addToRow(rows, getLabel(s));
		}
		newRow(rows);
		return true;
	}

	/*-------------------------------------------------------------------------*/
	private String describeSlots(BitSet slots)
	{
		java.util.List<String> temp = new ArrayList<String>();

		for (int i=0; i<PlayerCharacter.EquipableSlots.NUMBER_OF_SLOTS; i++)
		{
			if (slots.get(i))
			{
				temp.add(PlayerCharacter.EquipableSlots.describe(i));
			}
		}

		String result = "";
		for (int i = 0; i < temp.size(); i++)
		{
			result += temp.get(i);
			if (i != temp.size()-1)
			{
				result += ", ";
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private boolean addItemSpecificFields(List<Widget> rows, Item item)
	{
		if (item != null && item.isWeapon())
		{
			addToRow(rows, getTypeLabel("Weapon: "));

			if (item.getIdentificationState() == Item.IdentificationState.IDENTIFIED)
			{
				boolean curseDiscovered = item.getCursedState() == Item.CursedState.DISCOVERED;

				addToRow(rows, getLabel(item.getDamage()+ " damage"));

				newRow(rows);

				// weapon attributes

				StringBuilder attribs = new StringBuilder();
				if (item.isTwoHanded())
				{
					attribs.append("Two Handed");
				}
				if (item.isRanged())
				{
					if (attribs.length() > 0)
					{
						attribs.append(", ");
					}
					attribs.append("Ranged");
				}
				if (item.isReturning())
				{
					if (attribs.length() > 0)
					{
						attribs.append(", ");
					}
					attribs.append("Returning");
				}
				if (item.isBackstabCapable())
				{
					if (attribs.length() > 0)
					{
						attribs.append(", ");
					}
					attribs.append("Backstabber");
				}
				if (item.isSnipeCapable())
				{
					if (attribs.length() > 0)
					{
						attribs.append(", ");
					}
					attribs.append("Sniper");
				}

				if (attribs.length() > 0)
				{
					addToRow(rows, getLabel(attribs.toString()));
					newRow(rows);
				}

				// weapon attack bonuses

				StringBuilder bonuses = new StringBuilder();
				int th = item.getToHit();
				if (th > 0 || (th < 0 && curseDiscovered))
				{
					bonuses.append(descPlainModifier(th)).append(" to hit");
				}

				int tp = item.getToPenetrate();
				if (tp > 0 || (tp < 0 && curseDiscovered))
				{
					if (bonuses.length() > 0)
					{
						bonuses.append(", ");
					}
					bonuses.append(descPlainModifier(tp)).append(" to penetrate");
				}

				int tc = item.getToCritical();
				if (tc > 0 || (tc < 0 && curseDiscovered))
				{
					if (bonuses.length() > 0)
					{
						bonuses.append(", ");
					}
					bonuses.append(descPlainModifier(tc)).append(" to criticals");
				}

				int ti = item.getToInitiative();
				if (ti > 0 || (ti < 0 && curseDiscovered))
				{
					if (bonuses.length() > 0)
					{
						bonuses.append(", ");
					}
					bonuses.append(descPlainModifier(ti)).append(" to initiative");
				}
				if (bonuses.length() > 0)
				{
					addToRow(rows, getLabel("Attacks at "+bonuses.toString()));
					newRow(rows);
				}

				StringBuilder bonusAttacks = new StringBuilder();
				int ba = item.getBonusAttacks();
				if (ba > 0 || (ba < 0 && curseDiscovered))
				{
					bonusAttacks.append(descPlainModifier(ba)).append(" bonus attacks");
				}
				int bs = item.getBonusStrikes();
				if (bs > 0 || (bs < 0 && curseDiscovered))
				{
					if (bonusAttacks.length() > 0)
					{
						bonusAttacks.append(", ");
					}
					bonusAttacks.append(descPlainModifier(bs)).append(" bonus strikes");
				}
				if (bonusAttacks.length() > 0)
				{
					addToRow(rows, getLabel("Attacks with "+bonusAttacks.toString()));
					newRow(rows);
				}

				// attack modes and range

				String[] attackTypes = item.getAttackTypes();
				addToRow(rows, getDescriptiveLabel("Attack modes: "));
				StringBuilder sb = new StringBuilder();
				for (int i=0; i<attackTypes.length-1; i++)
				{
					sb.append(attackTypes[i]);
					sb.append(", ");
				}
				sb.append(attackTypes[attackTypes.length - 1]);

				int minRange = item.getMinRange();
				int maxRange = item.getMaxRange();

				sb.append(" at ");
				sb.append(ItemTemplate.WeaponRange.describe(minRange));

				if (minRange != maxRange)
				{
					sb.append(" to ").append(ItemTemplate.WeaponRange.describe(maxRange));
				}
				sb.append(" range");
				addToRow(rows, getLabel(sb.toString()));
			}

			newRow(rows);
		}
		else if (item.isArmour())
		{
			addToRow(rows, getTypeLabel(("Armour: ")));

			if (item.getIdentificationState() == Item.IdentificationState.IDENTIFIED)
			{
				String summary;

				summary = item.getDamagePreventionChance()+"% chance of damage ";

				// remember that damage reduction is positive ;-)
				summary += descPlainModifier(-item.getDamagePrevention());

				addToRow(rows, getLabel((summary)));
			}

			newRow(rows);
		}
		else if (item.isShield())
		{
			addToRow(rows, getTypeLabel(("Shield: ")));

			if (item.getIdentificationState() == Item.IdentificationState.IDENTIFIED)
			{
				String summary;

				summary = item.getDamagePreventionChance()+"% chance of damage ";

				// remember that damage reduction is positive ;-)
				summary += descPlainModifier(-item.getDamagePrevention());

				addToRow(rows, getLabel((summary)));
			}

			newRow(rows);
		}
		else if (item.isAmmo())
		{
			addToRow(rows, getTypeLabel(("Ammunition: ")));

			if (item.getIdentificationState() == Item.IdentificationState.IDENTIFIED)
			{
				addToRow(rows, getLabel((item.getDamage()+" damage ")));
			}

			newRow(rows);
		}
		else
		{
			String typeDesc = null;
			switch (item.getType())
			{
				case Item.Type.BANNER_EQUIPMENT: typeDesc = "Banner Item"; break;
				case Item.Type.BOMB: typeDesc = "Bomb"; break;
				case Item.Type.DRINK: typeDesc = "Beverage"; break;
				case Item.Type.FOOD: typeDesc = "Food"; break;
				case Item.Type.KEY: typeDesc = "Key"; break;
				case Item.Type.MISC_EQUIPMENT: typeDesc = "Miscellaneous Item"; break;
				case Item.Type.MISC_MAGIC: typeDesc = "Magical Item"; break;
				case Item.Type.OTHER: typeDesc = "Miscellaneous Item"; break;
				case Item.Type.POTION: typeDesc = "Potion"; break;
				case Item.Type.POWDER: typeDesc = "Powder"; break;
				case Item.Type.SCROLL: typeDesc = "Scroll"; break;
				case Item.Type.SPELLBOOK: typeDesc = "Spell Book"; break;
				case Item.Type.WRITING: typeDesc = "Book"; break;
			}

			if (typeDesc != null)
			{
				addToRow(rows, getTypeLabel((typeDesc + ":")));
				newRow(rows);
			}
		}
		
		return true;
	}

	/*-------------------------------------------------------------------------*/
	private boolean addItemModifiers(List<Widget> rows, Item item)
	{
		if (item.getIdentificationState() == Item.IdentificationState.IDENTIFIED)
		{
			if (item.getModifiers() != null)
			{
				return addModifiers(item.getModifiers().getModifiers(), rows, "Modifiers: ", item, true);
			}
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private boolean addItemRequirements(List<Widget> rows, Item item)
	{
		boolean result = false;

		if (item.getEquipRequirements() != null && addModifiers(
				item.getEquipRequirements().getModifiers(), rows, "Required to equip: ", item, false))
		{
			result = true;
		}

		if (item.getUseRequirements() != null && addModifiers(
				item.getUseRequirements().getModifiers(), rows, "Required to use: ", item, false))
		{
			result = true;
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private boolean addModifiers(Map<String, Integer> modifiers, List<Widget> rows,
		String title, Item item, boolean suppressCursedItemStuff)
	{
		if (modifiers.size() == 0)
		{
			// no modifiers section
			return false;
		}

		List<String> sortedModifiers = new ArrayList<String>(modifiers.keySet());
		Collections.sort(sortedModifiers);

		if (suppressCursedItemStuff && item.getCursedState() == Item.CursedState.UNDISCOVERED)
		{
			//
			// For items with an UNDISCOVERED cursed state, include only positive
			// modifiers.
			//

			boolean hasAnyPositiveModifiers = false;

			for (String modifier : sortedModifiers)
			{
				if (modifiers.get(modifier) > 0)
				{
					hasAnyPositiveModifiers = true;
					break;
				}
			}

			if (!hasAnyPositiveModifiers)
			{
				// no positive modifiers to display, so display nothing
				return false;
			}
		}

		StringBuilder sb = new StringBuilder(title);
		List<String> modDesc = new ArrayList<String>();

		for (String modifier : sortedModifiers)
		{
			int value = modifiers.get(modifier);

			if (suppressCursedItemStuff &&
				item.getCursedState() == Item.CursedState.UNDISCOVERED &&
				value < 0)
			{
				// undiscovered cursed item, do not display this negative modifier
				continue;
			}

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
					return "Cancel "+modifierName;
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
}
