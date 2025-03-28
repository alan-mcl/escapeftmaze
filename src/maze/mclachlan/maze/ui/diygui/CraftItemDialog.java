/*
 * Copyright (c) 2012 Alan McLachlan
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
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.DIYScrollPane;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYFlowLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.*;

/**
 *
 */
public class CraftItemDialog extends GeneralDialog implements ActionListener
{
	private static final int DIALOG_WIDTH = DiyGuiUserInterface.SCREEN_WIDTH/4*3;
	private static final int DIALOG_HEIGHT = DiyGuiUserInterface.SCREEN_HEIGHT -DiyGuiUserInterface.SCREEN_EDGE_INSET*2;

	private final TradingWidget item1Widget, item2Widget;
	private final DIYButton craft, close;
	private final PlayerCharacter pc;
	private final InventoryDisplayWidget inventoryDisplayWidget;

	/*-------------------------------------------------------------------------*/
	public CraftItemDialog(
		PlayerCharacter pc, InventoryDisplayWidget inventoryDisplayWidget)
	{
		super();

		this.pc = pc;
		this.inventoryDisplayWidget = inventoryDisplayWidget;

		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DIALOG_WIDTH/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - DIALOG_HEIGHT/2;

		Rectangle dialogBounds = new Rectangle(startX, startY, DIALOG_WIDTH, DIALOG_HEIGHT);
		this.setBounds(dialogBounds);

		int titlePaneHeight = getTitlePaneHeight();
		int border = getBorder();
		int buttonPaneHeight = getButtonPaneHeight();
		int inset = getInset();

		Rectangle isBounds = new Rectangle(
			x +border +inset,
			y +border +inset +titlePaneHeight,
			width -border*2 -inset*2,
			height -border*2 -inset*2 -titlePaneHeight -buttonPaneHeight);

		item1Widget = new TradingWidget(
			pc, pc.getInventory(), 0, 0, pc.getInventory().size(), this, false);
		item2Widget = new TradingWidget(
			pc, pc.getInventory(), 0, 0, pc.getInventory().size(), this, false);

		DIYPane titlePane = getTitlePane(StringUtil.getUiLabel("cid.title"));

		DIYLabel skillLabel = new DIYLabel(StringUtil.getUiLabel(
			"cid.crafting.skill",pc.getName(), getCraftSkill()));
		skillLabel.setForegroundColour(Color.WHITE);
		skillLabel.setBounds(
			titlePane.x,
			titlePane.y + titlePane.height,
			titlePane.width,
			20);

		DIYPane buttonPane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));
		buttonPane.setBounds(
			x +border,
			y +height -buttonPaneHeight -border -inset,
			width -border*2,
			buttonPaneHeight);

		craft = new DIYButton(StringUtil.getUiLabel("cid.craft"));
		craft.addActionListener(this);
		buttonPane.add(craft);

		close = getCloseButton();
		close.addActionListener(this);
		this.add(close);

		int tradingPaneY = skillLabel.y +skillLabel.height +inset;
		int tradingPaneWidth = (DIALOG_WIDTH -border*2 -inset*4)/2;
		int tradingPaneHeight = DIALOG_HEIGHT -buttonPaneHeight -titlePaneHeight -skillLabel.height -inset*4 -border*2;

		DIYScrollPane leftPane = new DIYScrollPane(
			x +border +inset,
			tradingPaneY,
			tradingPaneWidth,
			tradingPaneHeight,
			item1Widget);

		DIYScrollPane rightPane = new DIYScrollPane(
			x +border +inset*2 +tradingPaneWidth,
			tradingPaneY,
			tradingPaneWidth,
			tradingPaneHeight,
			item2Widget);

		this.add(titlePane);
		this.add(skillLabel);
		this.add(leftPane);
		this.add(rightPane);
		this.add(buttonPane);
		this.doLayout();
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
			case KeyEvent.VK_C -> { e.consume(); craft(); }
			case KeyEvent.VK_ESCAPE, KeyEvent.VK_ENTER -> { e.consume(); exit(); }
		}
	}

	/*-------------------------------------------------------------------------*/
	private void craft()
	{
		ItemWidget sel1 = item1Widget.getSelected();
		ItemWidget sel2 = item2Widget.getSelected();

		if (sel1 == null || sel1.getItem() == null ||
			sel2 == null || sel2.getItem() == null ||
			sel1.getItem() == sel2.getItem())
		{
			popupDialog(StringUtil.getUiLabel("cid.select.two.different"));
			return;
		}

		Item item1 = sel1.getItem();
		Item item2 = sel2.getItem();

		CraftRecipe recipe = null;

		for (CraftRecipe cr : Database.getInstance().getCraftRecipes().values())
		{
			if (cr.isMatch(item1.getName(), item2.getName()))
			{
				recipe = cr;
				break;
			}
		}

		if (recipe != null)
		{
			// is the user good enough?
			StatModifier recipeRequirements = recipe.getRequirements();

			if (pc.meetsRequirements(recipeRequirements))
			{
				int nrToCreate;
				
				// if these are stacked items (potions, powders) how many to create?
				if (!item1.isStackable() || !item2.isStackable())
				{
					// non-stackable. just remove the two items
					nrToCreate = 1;
					pc.removeItem(item1, true);
					pc.removeItem(item2, true);
				}
				else
				{
					// stackable
					int stack1 = item1.getStack().getCurrent();
					int stack2 = item2.getStack().getCurrent();
					if (stack1 == stack2)
					{
						// same nr in either stack, just remove.
						nrToCreate = stack1;
						pc.removeItem(item1, true);
						pc.removeItem(item2, true);
					}
					else
					{
						// different: remove the lesser
						if (stack1 > stack2)
						{
							nrToCreate = stack2;
							item1.getStack().decCurrent(stack2);
							pc.removeItem(item2, true);
						}
						else
						{
							nrToCreate = stack1;
							pc.removeItem(item1, true);
							item2.getStack().decCurrent(stack1);
						}
					}
				}

				// create the new item
				ItemTemplate itemTemplate = Database.getInstance().getItemTemplate(
					recipe.getResultingItem());

				Item newItem;
				if (itemTemplate.getMaxItemsPerStack() > 1)
				{
					// stackable item: create the specified nr in one stack
					newItem = itemTemplate.create(nrToCreate);
				}
				else
				{
					// non stackable item: just create it
					newItem = itemTemplate.create();
				}

				// practise the craft recipe modifiers
				for (Stats.Modifier mod : recipeRequirements.getModifiers().keySet())
				{
					GameSys.getInstance().practice(pc, mod, nrToCreate);
				}

				// add the item to the inventory; there will be at least one open
				// space because at least one of the merged items will have been
				// consumed
				pc.addInventoryItem(newItem);

				// UI admin
				inventoryDisplayWidget.refreshItemWidgets();
				item1Widget.refresh(pc.getInventory().getItems());
				item1Widget.resetSelected();
				item2Widget.refresh(pc.getInventory().getItems());
				item2Widget.resetSelected();

				popupDialog(StringUtil.getUiLabel("cid.created", pc.getName(), newItem.getDisplayName()));
			}
			else
			{
				StringBuilder sb = new StringBuilder();
				sb.append(StringUtil.getUiLabel("cid.cannot.merge", pc.getName()));

				boolean first = true;
				for (Stats.Modifier s : recipeRequirements.getModifiers().keySet())
				{
					if (!first)
					{
						sb.append(",");
					}
					sb.append(StringUtil.getModifierName(s));
					sb.append(" ");
					sb.append(recipeRequirements.getModifier(s));
					first = false;
				}

				popupDialog(sb.toString());
			}
		}
		else
		{
			popupDialog(StringUtil.getUiLabel("cid.cannot.be.merged"));
		}
	}

	/*-------------------------------------------------------------------------*/
	private int getCraftSkill()
	{
		return pc.getLevel() + pc.getModifier(Stats.Modifier.CRAFT);
	}

	/*-------------------------------------------------------------------------*/
	private void exit()
	{
		Maze.getInstance().getUi().clearDialog();
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();
		if (obj == craft)
		{
			craft();
			return true;
		}
		else if (obj == close)
		{
			exit();
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
}
