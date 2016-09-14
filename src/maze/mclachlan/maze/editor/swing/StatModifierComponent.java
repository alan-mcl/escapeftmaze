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

package mclachlan.maze.editor.swing;

import javax.swing.*;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.data.v1.V1StatModifier;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import mclachlan.maze.stat.Stats;

/**
 *
 */
public class StatModifierComponent extends JButton implements ActionListener
{
	private StatModifier modifier;
	private StatModifierComponentCallback callback;
	private int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public StatModifierComponent(int dirtyFlag)
	{
		this(null, dirtyFlag, null);
	}

	/*-------------------------------------------------------------------------*/
	public StatModifierComponent(StatModifier modifier, int dirtyFlag)
	{
		this(modifier, dirtyFlag, null);
	}
	
	/*-------------------------------------------------------------------------*/
	public StatModifierComponent(
		StatModifier modifier, 
		int dirtyFlag, 
		StatModifierComponentCallback callback)
	{
		this.dirtyFlag = dirtyFlag;
		refresh(modifier);
		addActionListener(this);
		this.callback = callback;
	}

	/*-------------------------------------------------------------------------*/
	public Dimension getPreferredSize()
	{
		Dimension d = super.getPreferredSize();
		Dimension result = new Dimension(d);
		result.height -= 5;
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(StatModifier modifier)
	{
		this.modifier = modifier;
		if (modifier == null)
		{
			this.setText("null");
		}
		else
		{
			String text = V1StatModifier.toString(modifier);

			String toolTipText = createToolTipText(modifier);

			this.setToolTipText(toolTipText);

			if (text.length() > 10)
			{
				text = text.substring(0, 6)+"...";
			}
			this.setText(text);
		}
	}

	/*-------------------------------------------------------------------------*/
	private String createToolTipText(StatModifier sm)
	{
		StringBuilder b = new StringBuilder();

		for (Stats.Modifier s : sm.getModifiers().keySet())
		{
			b.append(s).append(" ").append(
				Stats.descModifier(s, sm.getModifier(s))).append(",");
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	public StatModifier getModifier()
	{
		return modifier;
	}

	/*-------------------------------------------------------------------------*/
	public void setModifier(StatModifier modifier)
	{
		this.modifier = modifier;
		this.refresh(modifier);
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == this)
		{
			StatModifierPanel dialog = new StatModifierPanel(SwingEditor.instance, modifier);
			if (dialog.getModifier() != null)
			{
				SwingEditor.instance.setDirty(dirtyFlag);
				this.setModifier(dialog.getModifier());
				if (callback != null)
				{
					callback.statModifierChanged(this);
				}
			}
		}
	}
}
