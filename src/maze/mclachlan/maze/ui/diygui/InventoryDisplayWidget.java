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
import java.util.List;
import java.util.*;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.DIYPanel;
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

import static mclachlan.maze.ui.diygui.Constants.Colour.GOLD;

/**
 *
 */
public class InventoryDisplayWidget extends ContainerWidget
	implements CastSpellCallback, UseItemCallback, GetAmountCallback
{
	private PlayerCharacter character;

	private final ItemWidget primaryWeapon = new ItemWidget();
	private final ItemWidget secondaryWeapon = new ItemWidget();
	private final ItemWidget altPrimaryWeapon = new ItemWidget();
	private final ItemWidget altSecondaryWeapon = new ItemWidget();
	private final ItemWidget helm = new ItemWidget();
	private final ItemWidget torsoArmour = new ItemWidget();
	private final ItemWidget legArmour = new ItemWidget();
	private final ItemWidget gloves = new ItemWidget();
	private final ItemWidget boots = new ItemWidget();
	private final ItemWidget bannerItem = new ItemWidget();
	private final ItemWidget miscItem1 = new ItemWidget();
	private final ItemWidget miscItem2 = new ItemWidget();

	private final Map<ItemWidget, Integer> widgetToSlot = new HashMap<>();
	private final Map<Widget, Integer> packMap = new HashMap<>();
	private final ItemWidget[] packItems = new ItemWidget[UnifiedActor.MAX_PACK_ITEMS];

	private final DIYLabel nameLabel = new DIYLabel("", DIYToolkit.Align.LEFT);
	private final FilledBarWidget carrying = new FilledBarWidget(0, 0);
	private final DIYLabel goldLabel = new DIYLabel("", DIYToolkit.Align.LEFT);
	private final DIYLabel suppliesLabel = new DIYLabel("", DIYToolkit.Align.LEFT);

	private final DIYButton castSpell = new DIYButton(StringUtil.getUiLabel("idw.cast.spell"));
	private final DIYButton useItem = new DIYButton(StringUtil.getUiLabel("idw.use.item"));
	private final DIYButton craftItem = new DIYButton(StringUtil.getUiLabel("idw.craft"));
	private final DIYButton disassemble = new DIYButton(StringUtil.getUiLabel("idw.disassemble"));
	private final DIYButton dropItem = new DIYButton(StringUtil.getUiLabel("idw.drop.item"));
	private final DIYButton splitStack = new DIYButton(StringUtil.getUiLabel("idw.split.stack"));

	// horrible hackery
	private Spell lastSpell;
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
		for (int i = 0; i < packItems.length; i++)
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

		carrying.setTextType(FilledBarWidget.InnerTextType.CUSTOM);

		RendererProperties rp = DIYToolkit.getInstance().getRendererProperties();

		int inset = rp.getProperty(RendererProperties.Property.INSET);
//		int titleHeight = rp.getProperty(RendererProperties.Property.TITLE_PANE_HEIGHT);
		int titleHeight = 20;
		int buttonPaneHeight = rp.getProperty(RendererProperties.Property.BUTTON_PANE_HEIGHT);
		int headerOffset = titleHeight + DiyGuiUserInterface.SCREEN_EDGE_INSET;
		int contentTop = headerOffset + inset;
		int contentHeight = height - contentTop - buttonPaneHeight -inset;
		int panelBorderInset = rp.getProperty(RendererProperties.Property.PANEL_MED_BORDER);
		int frameBorderInset = rp.getProperty(RendererProperties.Property.PANEL_LIGHT_BORDER);

		int column1x = bounds.x + inset;
		int columnWidth = (width -5*inset) / 3;

		int column2x = column1x + columnWidth + inset;
		int column3x = column2x + columnWidth + inset;

		// screen title
		DIYLabel inventoryTitle = getSubTitle(StringUtil.getUiLabel("idw.title"));
		inventoryTitle.setBounds(
			200, DiyGuiUserInterface.SCREEN_EDGE_INSET,
			DiyGuiUserInterface.SCREEN_WIDTH - 400, titleHeight);

		// personal info

		DIYPanel personalPanel = new DIYPanel();
		personalPanel.setStyle(DIYPanel.Style.PANEL_LIGHT);
		personalPanel.setLayoutManager(null);
		personalPanel.setBounds(
			column1x,
			contentTop,
			columnWidth * 2 + inset,
			panelBorderInset*2 + 30);

		nameLabel.setBounds(
			personalPanel.x +panelBorderInset,
			personalPanel.y +panelBorderInset,
			personalPanel.width -panelBorderInset*2,
			(personalPanel.height -panelBorderInset*2)/2);

		DIYLabel carryingLabel = getLabel(StringUtil.getUiLabel("idw.carrying"), Color.LIGHT_GRAY);
		carryingLabel.setBounds(
			personalPanel.x +panelBorderInset,
			nameLabel.y + nameLabel.height +inset/2,
			nameLabel.width/4,
			nameLabel.height);

		carrying.setBounds(
			carryingLabel.x + carryingLabel.width +inset,
			personalPanel.y + personalPanel.height/2,
			nameLabel.width/4*3,
			personalPanel.height/3);

		personalPanel.add(nameLabel);
		personalPanel.add(carryingLabel);
		personalPanel.add(carrying);

		// party info

		DIYPanel partyPanel = new DIYPanel();
		partyPanel.setStyle(DIYPanel.Style.PANEL_LIGHT);
		partyPanel.setBounds(
			column3x,
			contentTop,
			columnWidth,
			personalPanel.height);

		partyPanel.setLayoutManager(new DIYGridLayout(1, 2, 0, 0));
		partyPanel.setInsets(new Insets(frameBorderInset, frameBorderInset+inset/2, frameBorderInset, frameBorderInset));

		partyPanel.add(goldLabel);
		partyPanel.add(suppliesLabel);

		// equipped items

		DIYPanel equippedItemsPanel = new DIYPanel();
		equippedItemsPanel.setStyle(DIYPanel.Style.PANEL_MED);
		equippedItemsPanel.setLayoutManager(null);
		equippedItemsPanel.setBounds(
			column1x,
			partyPanel.y + partyPanel.height + inset,
			columnWidth,
			contentHeight - partyPanel.height - inset*3);

		DIYLabel equippedItemsTitle = getSubTitle(StringUtil.getUiLabel("idw.equipped.items"));
		equippedItemsTitle.setBounds(
			equippedItemsPanel.x +panelBorderInset,
			equippedItemsPanel.y +panelBorderInset,
			equippedItemsPanel.width -panelBorderInset*2,
			titleHeight);

		DIYPane equippedItemsLabelsPane = new DIYPane();
		equippedItemsLabelsPane.setLayoutManager(new DIYGridLayout(1, 12, 0, 0));
		equippedItemsLabelsPane.setBounds(
			equippedItemsPanel.x +panelBorderInset,
			equippedItemsPanel.y +panelBorderInset +equippedItemsTitle.height,
			50+inset/2,
			equippedItemsPanel.height -panelBorderInset*2 -titleHeight);

		DIYPane equippedItemsItemWidgetsPane = new DIYPane();
		equippedItemsItemWidgetsPane.setLayoutManager(new DIYGridLayout(1, 12, 0, 0));
		equippedItemsItemWidgetsPane.setBounds(
			equippedItemsLabelsPane.x +equippedItemsLabelsPane.width,
			equippedItemsLabelsPane.y,
			equippedItemsPanel.width -equippedItemsLabelsPane.width -panelBorderInset*2,
			equippedItemsLabelsPane.height);

		equippedItemsLabelsPane.add(getLabel(StringUtil.getUiLabel("idw.primary"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		equippedItemsLabelsPane.add(getLabel(StringUtil.getUiLabel("idw.secondary"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		equippedItemsLabelsPane.add(getLabel(StringUtil.getUiLabel("idw.alt.primary"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		equippedItemsLabelsPane.add(getLabel(StringUtil.getUiLabel("idw.alt.secondary"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		equippedItemsLabelsPane.add(getLabel(StringUtil.getUiLabel("idw.helm"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		equippedItemsLabelsPane.add(getLabel(StringUtil.getUiLabel("idw.torso"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		equippedItemsLabelsPane.add(getLabel(StringUtil.getUiLabel("idw.legs"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		equippedItemsLabelsPane.add(getLabel(StringUtil.getUiLabel("idw.gloves"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		equippedItemsLabelsPane.add(getLabel(StringUtil.getUiLabel("idw.boots"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		equippedItemsLabelsPane.add(getLabel(StringUtil.getUiLabel("idw.banner"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		equippedItemsLabelsPane.add(getLabel(StringUtil.getUiLabel("idw.misc1"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));
		equippedItemsLabelsPane.add(getLabel(StringUtil.getUiLabel("idw.misc2"), Color.LIGHT_GRAY, DIYToolkit.Align.RIGHT));

		equippedItemsItemWidgetsPane.add(primaryWeapon);
		equippedItemsItemWidgetsPane.add(secondaryWeapon);
		equippedItemsItemWidgetsPane.add(altPrimaryWeapon);
		equippedItemsItemWidgetsPane.add(altSecondaryWeapon);
		equippedItemsItemWidgetsPane.add(helm);
		equippedItemsItemWidgetsPane.add(torsoArmour);
		equippedItemsItemWidgetsPane.add(legArmour);
		equippedItemsItemWidgetsPane.add(gloves);
		equippedItemsItemWidgetsPane.add(boots);
		equippedItemsItemWidgetsPane.add(bannerItem);
		equippedItemsItemWidgetsPane.add(miscItem1);
		equippedItemsItemWidgetsPane.add(miscItem2);

		equippedItemsPanel.add(equippedItemsTitle);
		equippedItemsPanel.add(equippedItemsLabelsPane);
		equippedItemsPanel.add(equippedItemsItemWidgetsPane);

		// pack items

		DIYPanel packItemsPanel = new DIYPanel();
		int itemGridHeight = equippedItemsItemWidgetsPane.height * 10 / 12;
		packItemsPanel.setStyle(DIYPanel.Style.PANEL_MED);
		packItemsPanel.setLayoutManager(null);
		packItemsPanel.setBounds(
			column2x,
			partyPanel.y + partyPanel.height + inset,
			columnWidth*2 +inset,
			itemGridHeight + titleHeight +panelBorderInset*2);

		DIYLabel packItemsTitle = getSubTitle(StringUtil.getUiLabel("idw.pack.items"));
		packItemsTitle.setBounds(
			packItemsPanel.x +panelBorderInset,
			packItemsPanel.y +panelBorderInset,
			packItemsPanel.width -panelBorderInset*2,
			titleHeight);

		DIYPane packItemsGrid = new DIYPane();
		packItemsGrid.setLayoutManager(new DIYGridLayout(2, 10, 0, 0));
		packItemsGrid.setBounds(
			packItemsPanel.x +panelBorderInset,
			packItemsPanel.y +panelBorderInset +titleHeight,
			packItemsPanel.width -panelBorderInset*2,
			itemGridHeight);

		for (int i=0; i<packItems.length; i++)
		{
			packItemsGrid.add(packItems[i]);
		}

		packItemsPanel.add(packItemsTitle);
		packItemsPanel.add(packItemsGrid);

		// all them special buttons

		DIYPane buttonPane = new DIYPane();
		buttonPane.setLayoutManager(new DIYGridLayout(3, 2, 2, 2));

		buttonPane.setBounds(
			column2x,
			packItemsPanel.y +packItemsPanel.height +inset,
			columnWidth*2 +inset,
			contentHeight -personalPanel.height - packItemsPanel.height -inset*4);

		buttonPane.add(castSpell);
		buttonPane.add(useItem);
		buttonPane.add(craftItem);
		buttonPane.add(dropItem);
		buttonPane.add(splitStack);
		buttonPane.add(disassemble);

		this.add(inventoryTitle);
		this.add(personalPanel);
		this.add(partyPanel);
		this.add(equippedItemsPanel);
		this.add(packItemsPanel);
		this.add(buttonPane);
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

		for (int i = 0; i < PlayerCharacter.MAX_PACK_ITEMS; i++)
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
			carrying.setBarColour(Color.BLUE);
			carrying.setForegroundColour(Color.LIGHT_GRAY);
		}
		else if (cur <= max * .75)
		{
			carrying.setBarColour(Color.YELLOW);
			carrying.setForegroundColour(Color.WHITE);
		}
		else
		{
			carrying.setBarColour(Color.RED);
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
			new Point(0, 0),
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

				if (other + current <= item.getStack().getMaximum())
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
					item.getStack().setCurrent(other + current - item.getStack().getMaximum());
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
			throw new MazeException("invalid widget: " + widget);
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
	public boolean castSpell(Spell spell, PlayerCharacter caster,
		int casterIndex, int castingLevel, int target)
	{
		if (spell.getTargetType() == MagicSys.SpellTargetType.ITEM)
		{
			setSpellStateHack(spell, castingLevel);
			// proceed with item selection
			new UseItem(StringUtil.getUiLabel("idw.cast.spell.on.item"), this, character);
			return true;
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private void setSpellStateHack(Spell spell, int castingLevel)
	{
		this.lastSpell = spell;
		this.lastCastingLevel = castingLevel;
	}

	/*-------------------------------------------------------------------------*/
	private void setItemStateHack(Item item)
	{
		this.lastItem = item;
	}

	/*-------------------------------------------------------------------------*/
	public boolean useItem(final Item item, PlayerCharacter user, int userIndex,
		SpellTarget target)
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
			setSpellStateHack(null, -1);
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

			setSpellStateHack(null, -1);
			return true;
		}
		else if (lastItem != null)
		{
			// we've gotta invoke the given item on the chosen item

			UseItemIntention useItemIntention = new UseItemIntention(lastItem, item);
			GameSys.getInstance().resolveActorActionIntention(Maze.getInstance(), user, useItemIntention);

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
		int x = DiyGuiUserInterface.SCREEN_WIDTH / 4;
		int y = DiyGuiUserInterface.SCREEN_HEIGHT / 3;

		Rectangle rectangle = new Rectangle(x, y,
			DiyGuiUserInterface.SCREEN_WIDTH / 2, DiyGuiUserInterface.SCREEN_HEIGHT / 3);

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
					"idw.cannot.disassemble.req", character.getDisplayName()));

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

		new GetAmount(this, character, item.getStack().getCurrent() - 1);
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
			List<Item> list = new ArrayList<>();
			list.add(newItem);
			Maze.getInstance().dropItemsOnCurrentTile(list);
		}

		refreshItemWidgets();

		return true;
	}

	/*-------------------------------------------------------------------------*/
	private class InventoryActionListener implements ActionListener
	{
		public boolean actionPerformed(ActionEvent event)
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
					return true;
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
					return true;
				}
			}
			else if (event.getSource() == castSpell)
			{
				spell();
				return true;
			}
			else if (event.getSource() == dropItem)
			{
				drop();
				return true;
			}
			else if (event.getSource() == useItem)
			{
				use();
				return true;
			}
			else if (event.getSource() == craftItem)
			{
				craft();
				return true;
			}
			else if (event.getSource() == splitStack)
			{
				split();
				return true;
			}
			else if (event.getSource() == disassemble)
			{
				disassemble();
				return true;
			}

			refreshCarryingCapacity();
			return false;
		}
	}

	/*-------------------------------------------------------------------------*/
	private DIYLabel getSubTitle(String titleText)
	{
		DIYLabel title = new DIYLabel(titleText);
		title.setForegroundColour(GOLD);
		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.PLAIN, defaultFont.getSize() + 3);
		title.setFont(f);
		return title;
	}

}
