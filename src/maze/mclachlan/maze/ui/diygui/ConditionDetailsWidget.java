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

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.*;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.data.StringUtil.getUiLabel;

/**
 * To be used as the popup dialog to display condition details.
 */
public class ConditionDetailsWidget extends GeneralDialog
{
	private int wrapWidth;

	/*-------------------------------------------------------------------------*/
	public ConditionDetailsWidget(Rectangle bounds, Condition condition)
	{
		super(bounds);
		buildGui(bounds, condition);
	}

	/*-------------------------------------------------------------------------*/
	private void buildGui(Rectangle bounds, Condition condition)
	{
		setStyle(Style.DIALOG);

		int rowHeight = 15;
		int xx = bounds.x + getInset() + getBorder();
		int yy = bounds.y + getInset() + getBorder() + getTitlePaneHeight();
		int width1 = bounds.width - getInset() * 2 - getBorder() * 2;

		wrapWidth = width1;

		DIYButton close = getCloseButton();

		close.addActionListener(event -> {
			DIYToolkit.getInstance().clearDialog();
			return true;
		});

		// Condition name
		DIYPane title = getTitlePane(condition.getDisplayName());

		// Condition image
		DIYLabel itemSlot = new DIYLabel(DIYToolkit.getInstance().getRendererProperties().getImageResource("icon/itemslot"));
		DIYLabel icon = new DIYLabel(Database.getInstance().getImage(condition.getDisplayIcon()));
		addRelative(itemSlot, getBorder(), getBorder(), 35, 35);
		icon.setBounds(itemSlot.getBounds());
		icon.setIconAlign(DIYToolkit.Align.CENTER);
		this.add(icon);

		// rows of condition details
		List<Widget> rows = new ArrayList<>();
		newRow(rows);

		addRow(rows, getLabel(
			getUiLabel(
				"cdw.type",
				condition.getSubtype().describe(),
				condition.getType().describe())));

		addRow(rows, getLabel(
			getUiLabel(
				"cdw.strength",
				condition.isStrengthIdentified() ? condition.getStrength() : getUiLabel("cdw.unknown.strength"))));

		addRow(rows, getLabel(
			getUiLabel(
				"cdw.source",
				condition.getSource().getDisplayName())));

		newRow(rows);

		addModifiers(
			condition.getModifiers(),
			rows,
			getUiLabel("cdw.modifiers"));

		newRow(rows);

		DIYPane pane = new DIYPane(xx, yy, width1, rows.size() * rowHeight);
		pane.setLayoutManager(new DIYGridLayout(1, rows.size(), 0, 0));

		for (int i = 0; i < rows.size(); i++)
		{
			pane.add(rows.get(i));
		}
		pane.doLayout();


		if (condition.isIdentified())
		{
//			String text = condition.getDescription();

//			if (text != null)
//			{
//				DIYTextArea desc = new DIYTextArea(condition.getDescription());
//				desc.setBounds(xx, yy+(rows.size()*rowHeight), width1, height1/5);
//				desc.setTransparent(true);
//				this.add(desc);
//			}
		}

		this.add(title);
		this.add(pane);
		this.add(close);
	}

	/*-------------------------------------------------------------------------*/
	private void addRow(List<Widget> rows, Widget w)
	{
		addToRow(rows, w);
		newRow(rows);
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
	private boolean addModifiers(Map<Stats.Modifier, Integer> modifiers,
		List<Widget> rows,
		String title)
	{
		if (modifiers.size() == 0)
		{
			// no modifiers section
			return false;
		}

		List<Stats.Modifier> sortedModifiers = new ArrayList<>(modifiers.keySet());
		Collections.sort(sortedModifiers);

		StringBuilder sb = new StringBuilder(title + " ");
		List<String> modDesc = new ArrayList<>();

		for (Stats.Modifier modifier : sortedModifiers)
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
					return getUiLabel("iw.cancel") + " " + modifierName;
				}
			case PERCENTAGE:
				return modifierName + " " + descPlainModifier(value) + "%";
			default:
				throw new MazeException(metric.name());
		}
	}

	/*-------------------------------------------------------------------------*/
	private String descPlainModifier(int value)
	{
		if (value >= 0)
		{
			return "+" + value;
		}
		else
		{
			return "" + value;
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
