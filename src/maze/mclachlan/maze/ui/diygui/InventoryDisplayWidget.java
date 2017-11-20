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
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.ILootEntry;
import mclachlan.maze.map.LootEntry;
import mclachlan.maze.map.LootTable;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.SpellIntention;
import mclachlan.maze.stat.combat.UseItemIntention;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class InventoryDisplayWidget extends ContainerWidget
	implements CastSpellCallback, UseItemCallback, GetAmountCallback
{
	private PlayerCharacter character;

	private ItemWidget primaryWeapon = new ItemWidget();
	private ItemWidget secondaryWeapon = new ItemWidget();
	private ItemWidget altPrimaryWeapon = new ItemWidget();
	private ItemWidget altSecondaryWeapon = new ItemWidget();
	private ItemWidget helm = new ItemWidget();
	private ItemWidget torsoArmour = new ItemWidget();
	private ItemWidget legArmour = new ItemWidget();
	private ItemWidget gloves = new ItemWidget();
	private ItemWidget boots = new ItemWidget();
	private ItemWidget bannerItem = new ItemWidget();
	private ItemWidget miscItem1 = new ItemWidget();
	private ItemWidget miscItem2 = new ItemWidget();

	private Map<ItemWidget, Integer> widgetToSlot = new HashMap<ItemWidget, Integer>();
	private Map<Widget, Integer> packMap = new HashMap<Widget, Integer>();
	private static final int PACK_SIZE = 20;
	private ItemWidget[] packItems = new ItemWidget[PACK_SIZE];
	
	Map<String, DIYLabel> labelMap = new HashMap<String, DIYLabel>();
	private DIYLabel nameLabel = new DIYLabel("", DIYToolkit.Align.LEFT);
	private FilledBarWidget carrying = new FilledBarWidget(0,0);
	private DIYLabel goldLabel = new DIYLabel("", DIYToolkit.Align.LEFT);
	private DIYLabel suppliesLabel = new DIYLabel("", DIYToolkit.Align.LEFT);

	private DIYButton castSpell = new DIYButton(StringUtil.getUiLabel("idw.cast.spell"));
	private DIYButton useItem = new DIYButton(StringUtil.getUiLabel("idw.use.item"));
	private DIYButton craftItem = new DIYButton(StringUtil.getUiLabel("idw.craft"));
	private DIYButton disassemble = new DIYButton(StringUtil.getUiLabel("idw.disassemble"));
	private DIYButton dropItem = new DIYButton(StringUtil.getUiLabel("idw.drop.item"));
	private DIYButton splitStack = new DIYButton(StringUtil.getUiLabel("idw.split.stack"));

	// horrible hackery
	private Spell lastSpell;
	private PlayerCharacter lastCaster;
	private int lastCastingLevel;
	private Item lastItem;
	private Object lastObj;

	/*-------------------------------------------------------------------------*/
	public InventoryDisplayWidget(Rectangle bounds)
	{
		super(bounds);
		this.buildGUI(bounds);
	}
	
	/*-------------------------------------------------------------------------*/
	private void buildGUI(Rectangle bounds)
	{
		// set up widget to equipable slot map
		widgetToSlot.put(primaryWeapon, PlayerCharacter.EquipableSlots.PRIMARY_WEAPON);
		widgetToSlot.put(altPrimaryWeapon, PlayerCharacter.EquipableSlots.PRIMARY_WEAPON);
		widgetToSlot.put(secondaryWeapon, PlayerCharacter.EquipableSlots.SECONDARY_WEAPON);
		widgetToSlot.put(altSecondaryWeapon, PlayerCharacter.EquipableSlots.SECONDARY_WEAPON);
		widgetToSlot.put(helm, PlayerCharacter.EquipableSlots.HELM);
		widgetToSlot.put(torsoArmour, PlayerCharacter.EquipableSlots.TORSO_ARMOUR);
		widgetToSlot.put(legArmour, PlayerCharacter.EquipableSlots.LEG_ARMOUR);
		widgetToSlot.put(gloves, PlayerCharacter.EquipableSlots.GLOVES);
		widgetToSlot.put(boots, PlayerCharacter.EquipableSlots.BOOTS);
		widgetToSlot.put(bannerItem, PlayerCharacter.EquipableSlots.BANNER_ITEM);
		widgetToSlot.put(miscItem1, PlayerCharacter.EquipableSlots.MISC_ITEM_1);
		widgetToSlot.put(miscItem2, PlayerCharacter.EquipableSlots.MISC_ITEM_2);

		// add action listeners
		ActionListener listener = new InventoryActionListener();
		primaryWeapon.addActionListener(listener);
		altPrimaryWeapon.addActionListener(listener);
		secondaryWeapon.addActionListener(listener);
		altSecondaryWeapon.addActionListener(listener);
		helm.addActionListener(listener);
		torsoArmour.addActionListener(listener);
		legArmour.addActionListener(listener);
		gloves.addActionListener(listener);
		boots.addActionListener(listener);
		bannerItem.addActionListener(listener);
		miscItem1.addActionListener(listener);
		miscItem2.addActionListener(listener);

		// init pack item slots
		for (int i=0; i<PACK_SIZE; i++)
		{
			packItems[i] = new ItemWidget();
			packMap.put(packItems[i], i);
			packItems[i].addActionListener(listener);
		}

		castSpell.addActionListener(listener);
		useItem.addActionListener(listener);
		craftItem.addActionListener(listener);
		dropItem.addActionListener(listener);
		splitStack.addActionListener(listener);
		disassemble.addActionListener(listener);

		carrying.setText(FilledBarWidget.InnerText.CUSTOM);

		// build gui
		int rows = 26; //bounds.height/(textHeight+inset) -2;
		int inset = 4;
		int labelWidth = bounds.width/5 - inset;
		int itemCellWidth = 2*labelWidth;

		DIYLabel top = new DIYLabel("Inventory", DIYToolkit.Align.CENTER);
		top.setBounds(162, 0, DiyGuiUserInterface.SCREEN_WIDTH-162, 30);
		top.setForegroundColour(Constants.Colour.GOLD);
		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.BOLD, defaultFont.getSize()+5);
		top.setFont(f);
		this.add(top);
		
		DIYPane left = new DIYPane(bounds.x, bounds.y, labelWidth, bounds.height);
		DIYPane right = new DIYPane(bounds.x+labelWidth, bounds.y, itemCellWidth*2, bounds.height);
		
		left.setLayoutManager(new DIYGridLayout(1, rows, inset, inset));
		right.setLayoutManager(new DIYGridLayout(2, rows, inset, inset));
		
		right.setInsets(new Insets(0, inset,0,0));
		
		left.add(getBlank());
		left.add(nameLabel);

		left.add(getBlank());
		left.add(getLabel(StringUtil.getUiLabel("idw.carrying"), Color.LIGHT_GRAY));
		left.add(getBlank());
		left.add(getLabel(StringUtil.getUiLabel("idw.primary"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		left.add(getLabel(StringUtil.getUiLabel("idw.secondary"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		left.add(getLabel(StringUtil.getUiLabel("idw.alt.primary"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		left.add(getLabel(StringUtil.getUiLabel("idw.alt.secondary"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		left.add(getLabel(StringUtil.getUiLabel("idw.helm"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		left.add(getLabel(StringUtil.getUiLabel("idw.torso"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		left.add(getLabel(StringUtil.getUiLabel("idw.legs"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		left.add(getLabel(StringUtil.getUiLabel("idw.gloves"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		left.add(getLabel(StringUtil.getUiLabel("idw.boots"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		left.add(getLabel(StringUtil.getUiLabel("idw.banner"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		left.add(getLabel(StringUtil.getUiLabel("idw.misc1"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		left.add(getLabel(StringUtil.getUiLabel("idw.misc2"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		left.add(castSpell);
		left.add(useItem);
		left.add(craftItem);
		left.add(dropItem);
		left.add(splitStack);
		left.add(disassemble);

		right.add(getBlank());
		right.add(getBlank());
		right.add(getBlank());
		right.add(suppliesLabel);
		right.add(getBlank());
		right.add(goldLabel);
		right.add(carrying);
		right.add(getLabel(StringUtil.getUiLabel("idw.pack.items"), Color.LIGHT_GRAY));
		right.add(getLabel(StringUtil.getUiLabel("idw.equipped.items"), Color.LIGHT_GRAY));
		right.add(packItems[0]);
		right.add(primaryWeapon);
		right.add(packItems[1]);
		right.add(secondaryWeapon);
		right.add(packItems[2]);
		right.add(altPrimaryWeapon);
		right.add(packItems[3]);
		right.add(altSecondaryWeapon);
		right.add(packItems[4]);
		right.add(helm);

		right.add(packItems[5]);
		right.add(torsoArmour);
		right.add(packItems[6]);
		right.add(legArmour);
		right.add(packItems[7]);
		right.add(gloves);
		right.add(packItems[8]);
		right.add(boots);
		right.add(packItems[9]);
		right.add(bannerItem);
		right.add(packItems[10]);
		right.add(miscItem1);
		right.add(packItems[11]);
		right.add(miscItem2);
		right.add(packItems[12]);
		right.add(getBlank());
		right.add(packItems[13]);
		right.add(getBlank());
		right.add(packItems[14]);
		right.add(getBlank());
		right.add(packItems[15]);
		right.add(getBlank());
		right.add(packItems[16]);
		right.add(getBlank());
		right.add(packItems[17]);
		right.add(getBlank());
		right.add(packItems[18]);
		right.add(getBlank());
		right.add(packItems[19]);
		right.add(getBlank());

		this.add(left);
		this.add(right);
	}
	
	/*-------------------------------------------------------------------------*/
	public void setCharacter(PlayerCharacter character)
	{
		this.character = character;

		if (this.character == null)
		{
			return;
		}

		refreshData();
	}

	/*-------------------------------------------------------------------------*/
	private void refreshData()
	{
		nameLabel.setForegroundColour(Color.WHITE);
		nameLabel.setText(StringUtil.getUiLabel(
			"idw.character.details",
			this.character.getName(),
			String.valueOf(this.character.getLevel()),
			character.getGender().getName(),
			character.getRace().getName(),
			character.getCharacterClass().getName()));

		refreshCarryingCapacity();

		PlayerParty party = Maze.getInstance().getParty();
		goldLabel.setForegroundColour(Color.WHITE);
		goldLabel.setText(StringUtil.getUiLabel("idw.party.gold", String.valueOf(party.getGold())));

		suppliesLabel.setForegroundColour(Color.WHITE);
		suppliesLabel.setText(StringUtil.getUiLabel("idw.party.supplies", String.valueOf(party.getSupplies())));

		refreshItemWidgets();

		// do not allow cast spell or use item during combat
		castSpell.setEnabled(!Maze.getInstance().isInCombat());
		useItem.setEnabled(!Maze.getInstance().isInCombat());
	}

	/*-------------------------------------------------------------------------*/
	public void refreshItemWidgets()
	{
		primaryWeapon.setItem(character.getPrimaryWeapon());
		secondaryWeapon.setItem(character.getSecondaryWeapon());
		altPrimaryWeapon.setItem(character.getAltPrimaryWeapon());
		altSecondaryWeapon.setItem(character.getAltSecondaryWeapon());
		helm.setItem(character.getHelm());
		torsoArmour.setItem(character.getTorsoArmour());
		legArmour.setItem(character.getLegArmour());
		gloves.setItem(character.getGloves());
		boots.setItem(character.getBoots());
		bannerItem.setItem(character.getBannerItem());
		miscItem1.setItem(character.getMiscItem1());
		miscItem2.setItem(character.getMiscItem2());

		for (int i=0; i<PlayerCharacter.MAX_PACK_ITEMS; i++)
		{
			Item item = this.character.getInventory().get(i);
			packItems[i].setItem(item);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void refreshCarryingCapacity()
	{
		int cur = this.character.getCarrying();
		int max = GameSys.getInstance().getCarryingCapacity(this.character);

		carrying.setCustomText(
			StringUtil.getUiLabel(
				"idw.carrying.label", Constants.Format.formatWeight(cur), Constants.Format.formatWeight(max)));

		carrying.set(cur, max);

		if (cur <= max * .5)
		{
			carrying.setBackgroundColour(Color.BLUE);
			carrying.setForegroundColour(Color.LIGHT_GRAY);
		}
		else if (cur <= max * .75)
		{
			carrying.setBackgroundColour(Color.YELLOW);
			carrying.setForegroundColour(Color.WHITE);
		}
		else
		{
			carrying.setBackgroundColour(Color.RED);
			carrying.setForegroundColour(Color.DARK_GRAY);
		}
	}

	/*-------------------------------------------------------------------------*/
	private DIYLabel getLabel(String text, Color colour)
	{
		DIYLabel result = new DIYLabel(text, DIYToolkit.Align.LEFT);
		result.setForegroundColour(colour);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private DIYLabel getLabel(String text, Color colour, DIYToolkit.Align align)
	{
		DIYLabel result = new DIYLabel(text, align);
		result.setForegroundColour(colour);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.PANE;
	}

	/*-------------------------------------------------------------------------*/
	private void grabItem(ItemWidget itemWidget)
	{
		Item item = itemWidget.getItem();

		// cannot pick up an equipped cursed item
		if (item != null
			&& itemWidget.getItem().isCursed()
			&& itemWidget.getItem().getCursedState() == Item.CursedState.DISCOVERED
			&& !isPack(itemWidget))
		{
			return;
		}

		if (DIYToolkit.getInstance().getCursorContents() != null)
		{
			if (!dropItem(itemWidget))
			{
				return;
			}

			Maze.getInstance().refreshCharacterData();
			SpeechUtil.getInstance().dropItemInInventorySpeech(character, item);
		}
		else
		{
			itemWidget.setItem(null);
			this.setCharacterItem(itemWidget, null);
			Maze.getInstance().refreshCharacterData();
		}

		setCursorToItem(item);
	}

	/*-------------------------------------------------------------------------*/
	public static void setCursorToItem(Item item)
	{
		BufferedImage itemImage = Database.getInstance().getImage(item.getImage());
		BufferedImage overlayImage = Database.getInstance().getImage("cursor/cursor_overlay");

		BufferedImage cursorImage = DIYToolkit.cloneImage(itemImage);
		cursorImage = DIYToolkit.overlayImages(cursorImage, overlayImage);

		Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(
			cursorImage,
			new Point(0,0),
			item.getName());

		DIYToolkit.getInstance().setCursor(cursor, item);
	}

	/*-------------------------------------------------------------------------*/
	private boolean dropItem(ItemWidget itemWidget)
	{
		Object cursorContents = DIYToolkit.getInstance().getCursorContents();

		if (cursorContents != null
			&& cursorContents instanceof Item
			&& itemIsEquipableInSlot(itemWidget, (Item)cursorContents))
		{
			Item item = (Item)cursorContents;

			// equipping a cursed item suddenly discovers it's cursed state
			if (itemWidget.getItem() != null
				&& itemWidget.getItem().isCursed()
				&& itemWidget.getItem().getCursedState() != Item.CursedState.TEMPORARILY_REMOVED
				&& !isPack(itemWidget))
			{
				itemWidget.getItem().setCursedState(Item.CursedState.DISCOVERED);
				return false;
			}

			DIYToolkit.getInstance().clearCursor();

			if (item.isStackable()
				&& itemWidget.getItem() != null
				&& itemWidget.getItem().getName().equalsIgnoreCase(item.getName()))
			{
				// we're dropping one stackable item onto another of the same
				// type.  Merge them instead.

				int current = itemWidget.getItem().getStack().getCurrent();
				int other = item.getStack().getCurrent();

				if (other+current <= item.getStack().getMaximum())
				{
					// simply merge the two,
					itemWidget.getItem().getStack().incCurrent(other);
					itemWidget.setItem(itemWidget.getItem()); //refresh the text
					item = null;
					return false;
				}
				else
				{
					// a remainder.
					itemWidget.getItem().getStack().setCurrentToMax();
					itemWidget.setItem(itemWidget.getItem()); //refresh the text
					item.getStack().setCurrent(other+current - item.getStack().getMaximum());
					setCursorToItem(item);
					return false;
				}
			}
			else
			{
				itemWidget.setItem(item);
				this.setCharacterItem(itemWidget, item);
				return true;
			}
		}
		
		return false;
	}
	
	/*-------------------------------------------------------------------------*/
	private void setCharacterItem(ItemWidget widget, Item item)
	{
		// warning, ugly code ahead
		
		if (widget == primaryWeapon)
		{
			character.setPrimaryWeapon(item);
		}
		else if (widget == secondaryWeapon)
		{
			character.setSecondaryWeapon(item);
		}
		else if (widget == altPrimaryWeapon)
		{
			character.setAltPrimaryWeapon(item);
		}
		else if (widget == altSecondaryWeapon)
		{
			character.setAltSecondaryWeapon(item);
		}
		else if (widget == helm)
		{
			character.setHelm(item);
		}
		else if (widget == torsoArmour)
		{
			character.setTorsoArmour(item);
		}
		else if (widget == legArmour)
		{
			character.setLegArmour(item);
		}
		else if (widget == gloves)
		{
			character.setGloves(item);
		}
		else if (widget == boots)
		{
			character.setBoots(item);
		}
		else if (widget == bannerItem)
		{
			character.setBannerItem(item);
		}
		else if (widget == miscItem1)
		{
			character.setMiscItem1(item);
		}
		else if (widget == miscItem2)
		{
			character.setMiscItem2(item);
		}
		else if (isPack(widget))
		{
			int packIndex = packMap.get(widget);
			character.getInventory().add(item, packIndex);
		}
		else
		{
			throw new MazeException("invalid widget: "+widget);
		}
	}

	/*-------------------------------------------------------------------------*/
	private boolean itemIsEquipableInSlot(ItemWidget itemWidget, Item item)
	{
		// can always throw stuff in the pack
		if (isPack(itemWidget))
		{
			return true;
		}

		// illegal item widget?
		if (!widgetToSlot.containsKey(itemWidget))
		{
			return false;
		}

		// even if it doesn't, is it going to be used as ammo?
		if (itemWidget == secondaryWeapon
			&& primaryWeapon.getItem() != null
			&& primaryWeapon.getItem().getAmmoRequired() != null
			&& primaryWeapon.getItem().getAmmoRequired().contains(item.isAmmoType()))
		{
			// big old special case; we short circuit all other checks.
			// todo:
			// this currently leaves a bug where a player can equip an item
			// in this way, then remove the primary weapon and continue using
			// this item. Honestly, for now I'm happy to let them exploit that.
			return true;
		}

		// check is equipable
		if (!character.isEquippableItem(item))
		{
			return false;
		}

		BitSet equipableSlots = item.getEquipableSlots();

		// check that it can be equipped at all
		if (equipableSlots == null)
		{
			return false;
		}

		// check that it is equipable in this slot
		Integer index = this.widgetToSlot.get(itemWidget);
		// fudge the misc slots...
		if (index == PlayerCharacter.EquipableSlots.MISC_ITEM_2)
		{
			index = PlayerCharacter.EquipableSlots.MISC_ITEM_1;
		}
		boolean matchesSlot = equipableSlots.get(index);
		if (!matchesSlot)
		{
			return false;
		}

		// check whether any of the 1H Wield modifiers apply
		boolean oneHWieldPrimary = GameSys.getInstance().oneHandWieldApplies(character, primaryWeapon.getItem());
		boolean oneHWieldAltPrimary = GameSys.getInstance().oneHandWieldApplies(character, altPrimaryWeapon.getItem());
		boolean oneHWieldSelected = GameSys.getInstance().oneHandWieldApplies(character, item);

		// check that an attempt to drop a two handed weapon in the P weapon
		// slot only succeeds if the other hand is empty
		if (item.isTwoHanded() && !oneHWieldSelected &&
			((itemWidget == primaryWeapon && secondaryWeapon.getItem() != null)
			||
			(itemWidget == altPrimaryWeapon && altSecondaryWeapon.getItem() != null)))
		{
			return false;
		}
		
		// check that an attempt to drop an item in the S weapon slot only
		// succeeds if the weapon in the P weapon slot is not two-handed
		if (itemWidget == secondaryWeapon
				&& primaryWeapon.getItem() != null
				&& primaryWeapon.getItem().isTwoHanded()
				&& !oneHWieldPrimary
			||
			itemWidget == altSecondaryWeapon
				&& altPrimaryWeapon.getItem() != null
				&& altPrimaryWeapon.getItem().isTwoHanded()
				&& !oneHWieldAltPrimary)
		{
			return false;
		}

		return true;
	}

	/*-------------------------------------------------------------------------*/
	private boolean isPack(ItemWidget itemWidget)
	{
		return packMap.containsKey(itemWidget);
	}

	/*-------------------------------------------------------------------------*/
	private void popupItemDetailsDialog(ItemWidget itemWidget)
	{
		if (itemWidget.getItem() == null)
		{
			return;
		}
		
		popupItemDetailsDialog(itemWidget.getItem());
	}

	/*-------------------------------------------------------------------------*/
	private void popupItemDetailsDialog(Item item)
	{
		DiyGuiUserInterface.instance.popupItemDetailsWidget(item);
	}

	/*-------------------------------------------------------------------------*/
	public static DIYLabel getBlank()
	{
		return new DIYLabel();
	}

	/*-------------------------------------------------------------------------*/
	public boolean castSpell(Spell spell, PlayerCharacter caster, int casterIndex, int castingLevel, int target)
	{
		if (spell.getTargetType() == MagicSys.SpellTargetType.ITEM)
		{
			setSpellStateHack(spell, caster, castingLevel);
			// proceed with item selection
			new UseItem(StringUtil.getUiLabel("idw.cast.spell.on.item"), this, character);
			return true;
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private void setSpellStateHack(Spell spell, PlayerCharacter caster, int castingLevel)
	{
		this.lastSpell = spell;
		this.lastCaster = caster;
		this.lastCastingLevel = castingLevel;
	}

	/*-------------------------------------------------------------------------*/
	private void setItemStateHack(Item item)
	{
		this.lastItem = item;
	}

	/*-------------------------------------------------------------------------*/
	public boolean useItem(final Item item, PlayerCharacter user, int userIndex, SpellTarget target)
	{
		// this whole fucking thing is such a fucking hack.  fucking ui programming horseshit

		if (lastObj == dropItem)
		{
			// drop the item on the ground
			user.removeItem(item, true);
			List<Item> list = new ArrayList<Item>();
			list.add(item);
			Maze.getInstance().dropItemsOnCurrentTile(list);
			refreshItemWidgets();
			setSpellStateHack(null, null, -1);
			DIYToolkit.getInstance().clearCursor();
			return true;
		}
		else if (lastSpell != null)
		{
			// we've gotta cast some sort spell on this item
			SpellIntention spellIntention = new SpellIntention(item, lastSpell, lastCastingLevel);
			GameSys.getInstance().resolveActorActionIntention(Maze.getInstance(), user, spellIntention);

			Maze.getInstance().appendEvents(new MazeEvent()
			{
				@Override
				public List<MazeEvent> resolve()
				{
					refreshItemWidgets();
					popupItemDetailsDialog(item);
					return null;
				}
			});

			setSpellStateHack(null, null, -1);
			return true;
		}
		else if (lastItem != null)
		{
			// we've gotta invoke the given item on the chosen item

			UseItemIntention useItemIntention = new UseItemIntention(lastItem, item);
			GameSys.getInstance().resolveActorActionIntention(Maze.getInstance(), user, useItemIntention);

//			GameSys.getInstance().useItemOnItem(
//				lastItem,
//				item,
//				user);

			Maze.getInstance().appendEvents(new MazeEvent()
			{
				@Override
				public List<MazeEvent> resolve()
				{
					refreshItemWidgets();
					popupItemDetailsDialog(item);
					return null;
				}
			});

			setItemStateHack(null);
			return true;
		}
		else if (item.getInvokedSpell() != null &&
			item.getInvokedSpell().getTargetType() == MagicSys.SpellTargetType.ITEM &&
			item.getType() != ItemTemplate.Type.SPELLBOOK)
		{
			// this is an item with an ITEM targeting effect
			// (but not a spellbook! In that case the PC must learn the spell)
			setItemStateHack(item);
			new UseItem(StringUtil.getUiLabel("idw.invoke.spell.on.item"), this, user);
			return true;
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private void popupDialog(String text)
	{
		int x = DiyGuiUserInterface.SCREEN_WIDTH/4;
		int y = DiyGuiUserInterface.SCREEN_HEIGHT/3;

		Rectangle rectangle = new Rectangle(x, y,
			DiyGuiUserInterface.SCREEN_WIDTH/2, DiyGuiUserInterface.SCREEN_HEIGHT/3);

		Maze.getInstance().getUi().showDialog(new OkDialogWidget(rectangle, null, text));
	}

	/*-------------------------------------------------------------------------*/
	public void disassemble()
	{
		Item item = (Item)DIYToolkit.getInstance().getCursorContents();
		if (item == null)
		{
			popupDialog(StringUtil.getUiLabel("idw.pick.up.to.disassemble"));
			return;
		}

		if (item.getDisassemblyLootTable() == null)
		{
			popupDialog(StringUtil.getUiLabel("idw.cannot.disassemble",
				item.getDisplayName()));
			return;
		}

		StatModifier reqs = item.getUseRequirements();
		if (!character.meetsRequirements(reqs))
		{
			StringBuilder sb = new StringBuilder(
				StringUtil.getUiLabel(
					"idw.cannot.disassemble.req",character.getDisplayName()));

			boolean first = true;
			for (Stats.Modifier s : reqs.getModifiers().keySet())
			{
				if (!first)
				{
					sb.append(",");
				}
				sb.append(StringUtil.getModifierName(s));
				sb.append(" ");
				sb.append(reqs.getModifier(s));
				first = false;
			}

			popupDialog(sb.toString());
			return;
		}

		LootTable lt = Database.getInstance().getLootTable(item.getDisassemblyLootTable());
		List<ILootEntry> loot = lt.getLootEntries().getRandom();

		// consume the item in the cursor
		DIYToolkit.getInstance().clearCursor();

		// grant any disassembly loot
		Maze.getInstance().setState(Maze.State.MOVEMENT);
		List<Item> items = LootEntry.generate(loot);
		Maze.getInstance().appendEvents(TileScript.getLootingEvents(items));
	}

	/*-------------------------------------------------------------------------*/
	public void split()
	{
		Item item = (Item)DIYToolkit.getInstance().getCursorContents();
		if (item == null)
		{
			popupDialog(StringUtil.getUiLabel("idw.pick.up.to.split"));
			return;
		}

		if (!item.isStackable())
		{
			popupDialog(StringUtil.getUiLabel("idw.not.stackable", item.getDisplayName()));
			return;
		}

		if (item.getStack().getCurrent() == 1)
		{
			popupDialog(StringUtil.getUiLabel("idw.cannot.split"));
			return;
		}

		new GetAmount(this, character, item.getStack().getCurrent()-1);
	}

	/*-------------------------------------------------------------------------*/
	public void craft()
	{
		Maze.getInstance().getUi().showDialog(
			new CraftItemDialog(character, this));
	}

	/*-------------------------------------------------------------------------*/
	public void use()
	{
		lastObj = useItem;
		new UseItem(StringUtil.getUiLabel("idw.use.item.title"), InventoryDisplayWidget.this, character);
	}

	/*-------------------------------------------------------------------------*/
	public void drop()
	{
		lastObj = dropItem;

		Object cursorContents = DIYToolkit.getInstance().getCursorContents();
		if (cursorContents instanceof Item)
		{
			useItem(
				(Item)cursorContents,
				character,
				Maze.getInstance().getParty().getPlayerCharacterIndex(character),
				null);
		}
		else
		{
			new UseItem(StringUtil.getUiLabel("idw.drop.item.title"), InventoryDisplayWidget.this, character);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void spell()
	{
		lastObj = castSpell;
		new CastSpell(InventoryDisplayWidget.this, character);
	}

	/*-------------------------------------------------------------------------*/
	public boolean amountChosen(int amount, PlayerCharacter user, int userIndex)
	{
		// split item has happened
		Item item = (Item)DIYToolkit.getInstance().getCursorContents();

		item.getStack().decCurrent(amount);

		ItemTemplate template = item.getTemplate();
		Item newItem = template.create(amount);

		if (!user.addInventoryItem(newItem))
		{
			List<Item> list = new ArrayList<Item>();
			list.add(newItem);
			Maze.getInstance().dropItemsOnCurrentTile(list);
		}

		refreshItemWidgets();

		return true;
	}

	/*-------------------------------------------------------------------------*/
	private class InventoryActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			if (event.getSource() instanceof ItemWidget
				&& event.getEvent() instanceof MouseEvent)
			{
				MouseEvent e = (MouseEvent)event.getEvent();
				ItemWidget itemWidget = (ItemWidget)event.getSource();
				if (e.getButton() == MouseEvent.BUTTON3)
				{
					// right click
					if (itemWidget.getItem() != null)
					{
						GameSys.getInstance().attemptManualIdentify(itemWidget.getItem(), Maze.getInstance().getParty());
						itemWidget.refresh();
					}
					popupItemDetailsDialog(itemWidget);
				}
				else if (e.getButton() == MouseEvent.BUTTON1)
				{
					// left click
				
					if (itemWidget.getItem() == null)
					{
						// drop the item
						if (dropItem(itemWidget))
						{
							Maze.getInstance().refreshCharacterData();
							SpeechUtil.getInstance().dropItemInInventorySpeech(character,
								itemWidget.getItem());
						}
					}
					else
					{
						// grab the item 
						grabItem(itemWidget);
						Maze.getInstance().refreshCharacterData();
					}
				}
			}
			else if (event.getSource() == castSpell)
			{
				spell();
			}
			else if (event.getSource() == dropItem)
			{
				drop();
			}
			else if (event.getSource() == useItem)
			{
				use();
			}
			else if (event.getSource() == craftItem)
			{
				craft();
			}
			else if (event.getSource() == splitStack)
			{
				split();
			}
			else if (event.getSource() == disassemble)
			{
				disassemble();
			}

			refreshCarryingCapacity();
		}
	}
}
