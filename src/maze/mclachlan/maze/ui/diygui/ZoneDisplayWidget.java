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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPanel;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.Zone;

/**
 *
 */
public class ZoneDisplayWidget extends DIYPanel implements ActionListener
{
	private final DIYLabel zoneName, terrainType;
	private final ColourMagicDisplayWidget colourMagicDisplay;

	/*-------------------------------------------------------------------------*/
	public ZoneDisplayWidget(Rectangle bounds)
	{
		super(bounds);
		this.setStyle(Style.PANEL_HEAVY);
		this.setLayoutManager(null);

		int panelBorder = 33;
		int internalInset = 5;
		int rowHeight = (bounds.height - panelBorder * 2 - internalInset * 2);
		int labelWidth = bounds.width / 3;

		zoneName = new DIYLabel("", DIYToolkit.Align.LEFT);
		zoneName.setBounds(
			bounds.x + panelBorder + internalInset,
			bounds.y + panelBorder + internalInset,
			labelWidth,
			rowHeight);

		terrainType = new DIYLabel("", DIYToolkit.Align.RIGHT);
		terrainType.setBounds(
			bounds.x + bounds.width - panelBorder - internalInset - labelWidth,
			bounds.y + panelBorder + internalInset,
			labelWidth,
			rowHeight);

		CompassWidget compass = new CompassWidget();
		compass.setBounds(
			bounds.x + bounds.width / 2 - labelWidth / 2,
			bounds.y/* + bounds.height / 2 - rowHeight / 2*/,
			labelWidth,
			bounds.height /*rowHeight*/);

		colourMagicDisplay = new ColourMagicDisplayWidget("present");
		Dimension md = colourMagicDisplay.getPreferredSize();
		colourMagicDisplay.setBounds(
			bounds.x + bounds.width / 2 - md.width / 2,
			bounds.y + bounds.height - md.height,
			md.width,
			md.height);

		this.add(zoneName);
		this.add(compass);
		this.add(terrainType);

		this.add(colourMagicDisplay);
	}

	/*-------------------------------------------------------------------------*/
	public void setZone(Zone z)
	{
		this.zoneName.setText(z.getName());
	}

	/*-------------------------------------------------------------------------*/
	public void setTile(Zone zone, Tile t, Point tile)
	{
		this.terrainType.setText(t.getTerrainType() + " (" + t.getTerrainSubType() + ")");

		colourMagicDisplay.refresh(
			t.getAmountRedMagic(),
			t.getAmountBlackMagic(),
			t.getAmountPurpleMagic(),
			t.getAmountGoldMagic(),
			t.getAmountWhiteMagic(),
			t.getAmountGreenMagic(),
			t.getAmountBlueMagic());
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		return false;
	}
}
