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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import mclachlan.maze.data.v1.V1TileScript;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.Zone;

/**
 *
 */
public class TileScriptComponent extends JButton implements ActionListener
{
	private TileScript tileScript;
	private int dirtyFlag;
	private TileScriptComponentCallback callback;
	private Zone zone;

	/*-------------------------------------------------------------------------*/
	public TileScriptComponent(int dirtyFlag, Zone zone)
	{
		this(null, dirtyFlag, null, zone);
	}

	/*-------------------------------------------------------------------------*/
	public TileScriptComponent(TileScript tileScript, int dirtyFlag, Zone zone)
	{
		this(tileScript, dirtyFlag, null, zone);
	}
	
	/*-------------------------------------------------------------------------*/
	public TileScriptComponent(
		TileScript tileScript, 
		int dirtyFlag, 
		TileScriptComponentCallback callback,
		Zone zone)
	{
		this.dirtyFlag = dirtyFlag;
		this.callback = callback;
		refresh(tileScript, zone);
		addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(TileScript script, Zone zone)
	{
		this.tileScript = script;
		this.zone = zone;
		if (script == null)
		{
			this.setText("null");
		}
		else
		{
			String tooltip = V1TileScript.toString(script);
			this.setToolTipText(tooltip);

			String text = script.getClass().getSimpleName();
			if (text.length() > 30)
			{
				text = text.substring(0, 28)+"...";
			}
			this.setText(text);
		}
	}

	/*-------------------------------------------------------------------------*/
	public TileScript getTileScript()
	{
		return tileScript;
	}

	/*-------------------------------------------------------------------------*/
	public void setResult(TileScript spellResult, Zone zone)
	{
		this.tileScript = spellResult;
		this.refresh(spellResult, zone);
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == this)
		{
			TileScriptEditor dialog = new TileScriptEditor(
				SwingEditor.instance, 
				tileScript, 
				dirtyFlag,
				zone);
			this.tileScript = dialog.getResult();
			refresh(this.tileScript, zone);
			SwingEditor.instance.setDirty(dirtyFlag);
			if (callback != null)
			{
				callback.tileScriptChanged(this);
			}
		}
	}
}
