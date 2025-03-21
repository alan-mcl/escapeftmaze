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

import static mclachlan.maze.data.StringUtil.getUiLabel;
import static mclachlan.maze.stat.ItemTemplate.*;

/**
 * To be used as the popup dialog to display item details.
 */
public class ItemDetailsWidget extends GeneralDialog implements ActionListener
{
	private Item item;
	private DIYButton convert;
	private int wrapWidth;

	/*-------------------------------------------------------------------------*/
	public ItemDetailsWidget(Rectangle bounds, Item item)
	{
		super(bounds);
		this.item = item;
		buildGui(bounds, item);
	}

	/*-------------------------------------------------------------------------*/
	private void buildGui(Rectangle bounds, Item item)
	{
		setStyle(Style.DIALOG);

		int rowHeight = 15;
		int xx = bounds.x + getInset() + getBorder();
		int yy = bounds.y + getInset() + getBorder() + getTitlePaneHeight();
		int width1 = bounds.width - getInset() * 2 - getBorder() * 2;
		int height1 = bounds.height - getInset() * 3 - getBorder() * 2;

		wrapWidth = width1;

		// use case for the Convert button is an item that was initially not
		// identified, lands in the inventory, and now is
		boolean showConvertButton =
			Maze.getInstance().getState() == Maze.State.INVENTORY &&
			item.getIdentificationState() == Item.IdentificationState.IDENTIFIED &&
				(item.getType() == Type.SUPPLIES || item.getType() == Type.MONEY);

		DIYButton close = getCloseButton();

		close.addActionListener(event -> {
			DIYToolkit.getInstance().clearDialog();
			return true;
		});

		// Item name
		Color titleCol = null;
		if (item.isCursed() && item.getCursedState() == Item.CursedState.DISCOVERED)
		{
			titleCol = Color.RED;
		}
		DIYPane title = getTitlePane(item.getDisplayName(), titleCol);

		// Item image
		ItemWidget itemWidget = new ItemWidget();
		itemWidget.setStyle(ItemWidget.Style.ICON_ONLY);
		addRelative(itemWidget, getBorder(), getBorder(), 35, 35);
		itemWidget.setItem(item);

		// Item weight
		DIYLabel weightLabel = new DIYLabel(
			getUiLabel("iw.weight",
				Constants.Format.formatWeight(item.getWeight())));
		Dimension dw = weightLabel.getPreferredSize();
		weightLabel.setBounds(xx +width1 -dw.width, yy, dw.width, dw.height);
		this.add(weightLabel);

		// Current stack
		if (item.getStack().getMaximum() > 1)
		{
			CurMax stack = item.getStack();
			DIYLabel stackLabel = new DIYLabel(
				getUiLabel("iw.stack", stack.getCurrent(), stack.getMaximum()));
			Dimension ds = weightLabel.getPreferredSize();
			stackLabel.setBounds(xx +width1 -ds.width - getInset() - getBorder(), yy + rowHeight, ds.width, ds.height);
			this.add(stackLabel);
		}

		// rows of item details
		List<Widget> rows = new ArrayList<>();
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

		this.add(title);
		this.add(close);

		if (showConvertButton)
		{
			String buttonTitle;
			switch (item.getType())
			{
				case Type.SUPPLIES -> buttonTitle = StringUtil.getUiLabel("iw.convert.to.supplies");
				case Type.MONEY -> buttonTitle = StringUtil.getUiLabel("iw.convert.to.money");
				default -> throw new MazeException("Invalid type "+item.getType());
			}
			convert = new DIYButton(buttonTitle);
			convert.addActionListener(this);

			DIYPane buttonPane = getButtonPane();
			buttonPane.add(convert);

			this.add(buttonPane);
			this.doLayout();
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

			if (item.getType() == ItemTemplate.Type.SPELLBOOK)
			{
				String s = getUiLabel("iw.teaches.spell",
					item.getInvokedSpell().getDisplayName(), item.getInvokedSpell().getBook().getName());

				addToRow(rows, getLabel(s));
				newRow(rows);
				return true;
			}
			else
			{
				String s = getUiLabel("iw.invoked.spell",
					item.getInvokedSpell().getDisplayName());
				int level = item.getInvokedSpellLevel();
				s += " (";
				if (level == 0)
				{
					s += getUiLabel("iw.variable.level");
				}
				else
				{
					s += getUiLabel("iw.level",level);
				}
				CurMax charges = item.getCharges();
				if (charges != null && charges.getMaximum() > 0)
				{
					s += getUiLabel("iw.charges", charges.getCurrent(), charges.getMaximum());
				}
				else
				{
					s += getUiLabel("iw.charges.infinite");
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

			StringBuilder sb = new StringBuilder(getUiLabel("iw.spell.effects"));
			sb.append(" ");
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
		List<String> result = new ArrayList<>();

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
			sb.append(getUiLabel("iw.all"));
		}
		else
		{
			float max = all.size();
			float actual = itemList.size();

			List<String> users = new ArrayList<>(itemList);

			if (actual/max >= 0.66f)
			{
				List<String> exceptions = new ArrayList<>(all);
				exceptions.removeAll(users);
				Collections.sort(exceptions);
				sb.append(getUiLabel("iw.all.except"));
				sb.append(" ");
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
			sb.toString(), wrapWidth, null);

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
		StringBuilder sb = new StringBuilder(getUiLabel("iw.usable.by.races"));
		sb.append(" ");
		return buildUsability(rows, sb,
			new ArrayList<>(Database.getInstance().getRaces().keySet()), item.getUsableByRace());
	}

	/*-------------------------------------------------------------------------*/
	private boolean addUsableByGenders(List<Widget> rows, Item item)
	{
		StringBuilder sb = new StringBuilder(getUiLabel("iw.usable.by.genders"));
		sb.append(" ");
		return buildUsability(rows, sb,
			new ArrayList<>(Database.getInstance().getGenders().keySet()), item.getUsableByGender());
	}

	/*-------------------------------------------------------------------------*/
	private boolean addUsableByClasses(List<Widget> rows, Item item)
	{
		StringBuilder sb = new StringBuilder(getUiLabel("iw.usable.by.classes"));
		sb.append(" ");
		return buildUsability(rows, sb,
			new ArrayList<>(Database.getInstance().getCharacterClasses().keySet()), item.getUsableByCharacterClass());
	}

	/*-------------------------------------------------------------------------*/
	private boolean addEquipableSlots(List<Widget> rows, Item item)
	{
		BitSet slots = item.getEquipableSlots();

		if (slots==null || slots.isEmpty())
		{
			return false;
		}

		addToRow(rows, getDescriptiveLabel(getUiLabel("iw.slots")));
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

		List<ItemTemplate.AmmoType> temp = new ArrayList<>(ammoRequired);
		Collections.sort(temp);

		addToRow(rows, getDescriptiveLabel(getUiLabel("iw.ammo.required")));

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
		java.util.List<String> temp = new ArrayList<>();

		for (int i=0; i<PlayerCharacter.EquipableSlots.NUMBER_OF_SLOTS; i++)
		{
			if (slots.get(i))
			{
				temp.add(PlayerCharacter.EquipableSlots.describe(i));
			}
		}

		StringBuilder result = new StringBuilder();
		for (int i = 0; i < temp.size(); i++)
		{
			result.append(temp.get(i));
			if (i != temp.size()-1)
			{
				result.append(", ");
			}
		}

		return result.toString();
	}

	/*-------------------------------------------------------------------------*/
	private boolean addItemSpecificFields(List<Widget> rows, Item item)
	{
		if (item != null && item.isWeapon())
		{
			addToRow(rows, getTypeLabel(getUiLabel("iw.weapon")));

			if (item.getIdentificationState() == Item.IdentificationState.IDENTIFIED)
			{
				boolean curseDiscovered = item.getCursedState() == Item.CursedState.DISCOVERED;

				addToRow(rows, getLabel(getUiLabel("iw.damage",item.getDamage())));

				newRow(rows);

				// weapon attributes

				StringBuilder attribs = new StringBuilder();
				if (item.isTwoHanded())
				{
					attribs.append(getUiLabel("iw.two.handed"));
				}
				if (item.isRanged())
				{
					if (attribs.length() > 0)
					{
						attribs.append(", ");
					}
					attribs.append(getUiLabel("iw.ranged"));
				}
				if (item.isReturning())
				{
					if (attribs.length() > 0)
					{
						attribs.append(", ");
					}
					attribs.append(getUiLabel("iw.returning"));
				}
				if (item.isBackstabCapable())
				{
					if (attribs.length() > 0)
					{
						attribs.append(", ");
					}
					attribs.append(getUiLabel("iw.backstabber"));
				}
				if (item.isSnipeCapable())
				{
					if (attribs.length() > 0)
					{
						attribs.append(", ");
					}
					attribs.append(getUiLabel("iw.sniper"));
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
					bonuses.append(getUiLabel("iw.to.hit", descPlainModifier(th)));
				}

				int tp = item.getToPenetrate();
				if (tp > 0 || (tp < 0 && curseDiscovered))
				{
					if (bonuses.length() > 0)
					{
						bonuses.append(", ");
					}
					bonuses.append(getUiLabel("iw.to.penetrate", descPlainModifier(tp)));
				}

				int tc = item.getToCritical();
				if (tc > 0 || (tc < 0 && curseDiscovered))
				{
					if (bonuses.length() > 0)
					{
						bonuses.append(", ");
					}
					bonuses.append(getUiLabel("iw.to.criticals", descPlainModifier(tc)));
				}

				int ti = item.getToInitiative();
				if (ti > 0 || (ti < 0 && curseDiscovered))
				{
					if (bonuses.length() > 0)
					{
						bonuses.append(", ");
					}
					bonuses.append(getUiLabel("iw.to.initiative", descPlainModifier(ti)));
				}
				if (bonuses.length() > 0)
				{
					addToRow(rows, getLabel(getUiLabel("iw.attacks.at",bonuses.toString())));
					newRow(rows);
				}

				StringBuilder bonusAttacks = new StringBuilder();
				int ba = item.getBonusAttacks();
				if (ba > 0 || (ba < 0 && curseDiscovered))
				{
					bonusAttacks.append(getUiLabel("iw.bonus.attacks", descPlainModifier(ba)));
				}
				int bs = item.getBonusStrikes();
				if (bs > 0 || (bs < 0 && curseDiscovered))
				{
					if (bonusAttacks.length() > 0)
					{
						bonusAttacks.append(", ");
					}
					bonusAttacks.append(getUiLabel("iw.bonus.strikes", descPlainModifier(bs)));
				}
				if (bonusAttacks.length() > 0)
				{
					addToRow(rows, getLabel(getUiLabel("iw.attacks.with", bonusAttacks.toString())));
					newRow(rows);
				}

				// attack modes and range

				String[] attackTypes = item.getAttackTypes();
				addToRow(rows, getDescriptiveLabel(getUiLabel("iw.attack.modes")+" "));
				StringBuilder sb = new StringBuilder();
				for (int i=0; i<attackTypes.length-1; i++)
				{
					sb.append(attackTypes[i]);
					sb.append(", ");
				}
				sb.append(attackTypes[attackTypes.length - 1]);

				int minRange = item.getMinRange();
				int maxRange = item.getMaxRange();

				sb.append(" ").append(getUiLabel("iw.at")).append(" ");
				sb.append(ItemTemplate.WeaponRange.describe(minRange));

				if (minRange != maxRange)
				{
					sb.append(" ").append(getUiLabel("iw.to")).append(" ").
						append(ItemTemplate.WeaponRange.describe(maxRange));
				}
				sb.append(" ").append(getUiLabel("iw.range"));
				addToRow(rows, getLabel(sb.toString()));
			}

			newRow(rows);
		}
		else if (item.isArmour())
		{
			addToRow(rows, getTypeLabel((getUiLabel("iw.armour"))));

			if (item.getIdentificationState() == Item.IdentificationState.IDENTIFIED)
			{
				String summary = getUiLabel("iw.damage.prevention",
					item.getDamagePreventionChance(),
					descPlainModifier(-item.getDamagePrevention())); // remember that damage reduction is positive ;-)

				addToRow(rows, getLabel((summary)));
			}

			newRow(rows);
		}
		else if (item.isShield())
		{
			addToRow(rows, getTypeLabel((getUiLabel("iw.shield"))));

			if (item.getIdentificationState() == Item.IdentificationState.IDENTIFIED)
			{
				String summary = getUiLabel("iw.damage.prevention",
					item.getDamagePreventionChance(),
					descPlainModifier(-item.getDamagePrevention())); // remember that damage reduction is positive ;-);

				addToRow(rows, getLabel((summary)));
			}

			newRow(rows);
		}
		else if (item.isAmmo())
		{
			addToRow(rows, getTypeLabel((getUiLabel("iw.ammo"))));

			if (item.getIdentificationState() == Item.IdentificationState.IDENTIFIED)
			{
				addToRow(rows, getLabel((getUiLabel("iw.damage", item.getDamage()))));
			}

			newRow(rows);
		}
		else if (item.getType() == Type.MONEY)
		{
			addToRow(rows, getTypeLabel((getUiLabel("iw.money"))));

			if (item.getIdentificationState() == Item.IdentificationState.IDENTIFIED)
			{
				addToRow(rows, getLabel((getUiLabel("iw.conversion.rate.money",
					item.applyConversionRate()))));
			}

			newRow(rows);
		}
		else if (item.getType() == Type.SUPPLIES)
		{
			addToRow(rows, getTypeLabel((getUiLabel("iw.supplies"))));

			if (item.getIdentificationState() == Item.IdentificationState.IDENTIFIED)
			{
				addToRow(rows, getLabel((getUiLabel("iw.conversion.rate.supplies",
					item.applyConversionRate()))));
			}

			newRow(rows);
		}
		else
		{
			String typeDesc = switch (item.getType())
				{
					case Type.BANNER_EQUIPMENT -> getUiLabel("iw.banner.item");
					case Type.BOMB -> getUiLabel("iw.bomb");
					case Type.DRINK -> getUiLabel("iw.drink");
					case Type.FOOD -> getUiLabel("iw.food");
					case Type.KEY -> getUiLabel("iw.key");
					case Type.MISC_EQUIPMENT -> getUiLabel("iw.misc.item");
					case Type.MISC_MAGIC -> getUiLabel("iw.misc.magic");
					case Type.OTHER -> getUiLabel("iw.misc");
					case Type.POTION -> getUiLabel("iw.potion");
					case Type.POWDER -> getUiLabel("iw.powder");
					case Type.SCROLL -> getUiLabel("iw.scroll");
					case Type.SPELLBOOK -> getUiLabel("iw.spellbook");
					case Type.WRITING -> getUiLabel("iw.writing");
					case Type.SUPPLIES -> getUiLabel("iw.supplies");
					case Type.GADGET -> getUiLabel("iw.gadget");
					case Type.MUSICAL_INSTRUMENT -> getUiLabel("iw.music");
					default -> null;
				};

			if (typeDesc != null)
			{
				addToRow(rows, getTypeLabel((typeDesc)));
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
				return addModifiers(item.getModifiers().getModifiers(), rows,
					getUiLabel("iw.modifiers"), item, true);
			}
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private boolean addItemRequirements(List<Widget> rows, Item item)
	{
		boolean result = false;

		if (item.getEquipRequirements() != null && addModifiers(
			item.getEquipRequirements().getModifiers(), rows,
			getUiLabel("iw.equip.requirements"), item, false))
		{
			result = true;
		}

		if (item.getUseRequirements() != null && addModifiers(
			item.getUseRequirements().getModifiers(), rows,
			getUiLabel("iw.use.requirements"), item, false))
		{
			result = true;
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private boolean addModifiers(Map<Stats.Modifier, Integer> modifiers, List<Widget> rows,
		String title, Item item, boolean suppressCursedItemStuff)
	{
		if (modifiers.size() == 0)
		{
			// no modifiers section
			return false;
		}

		List<Stats.Modifier> sortedModifiers = new ArrayList<>(modifiers.keySet());
		Collections.sort(sortedModifiers);

		if (suppressCursedItemStuff && item.getCursedState() == Item.CursedState.UNDISCOVERED)
		{
			//
			// For items with an UNDISCOVERED cursed state, include only positive
			// modifiers.
			//

			boolean hasAnyPositiveModifiers = false;

			for (Stats.Modifier modifier : sortedModifiers)
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

		StringBuilder sb = new StringBuilder(title+" ");
		List<String> modDesc = new ArrayList<>();

		for (Stats.Modifier modifier : sortedModifiers)
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
	private String descModifier(Stats.Modifier modifier, int value)
	{
		String modifierName = StringUtil.getModifierName(modifier);

		Stats.ModifierMetric metric = modifier.getMetric();
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
			e.consume();
			Maze.getInstance().getUi().clearDialog();
		}
	}

	@Override
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == convert)
		{
			PlayerParty party = Maze.getInstance().getParty();
			if (item.getType() == ItemTemplate.Type.MONEY)
			{
				int amount = item.applyConversionRate();
				party.incGold(amount);
				party.removeItem(item);
				Maze.getInstance().getUi().showInventoryScreen();
				return true;
			}
			else if (item.getType() == ItemTemplate.Type.SUPPLIES)
			{
				int amount = item.applyConversionRate();
				party.incSupplies(amount);
				party.removeItem(item);
				Maze.getInstance().getUi().showInventoryScreen();
				return true;
			}
			else
			{
				throw new MazeException("Invalid "+item.getType());
			}
		}

		return false;
	}
}
