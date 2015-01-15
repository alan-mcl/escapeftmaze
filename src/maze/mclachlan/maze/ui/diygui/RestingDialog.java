/*
 * Copyright (c) 2013 Alan McLachlan
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
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Log;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.event.ModifySuppliesEvent;
import mclachlan.maze.game.event.RestingCheckpointEvent;
import mclachlan.maze.game.event.StartRestingEvent;
import mclachlan.maze.game.event.StopRestingEvent;
import mclachlan.maze.map.Tile;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.PlayerParty;

/**
 *
 */
public class RestingDialog extends GeneralDialog implements ActionListener
{
	private DIYButton rest, cancel;
	private int suppliesToConsume;

	/*-------------------------------------------------------------------------*/
	public RestingDialog(
		String title)
	{
		super();

		int buttonHeight = 20;
		int inset = 10;
		int buttonPaneHeight = 18;

		int dialogWidth = DiyGuiUserInterface.SCREEN_WIDTH/3;
		int dialogHeight = DiyGuiUserInterface.SCREEN_WIDTH/3;

		int startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - dialogWidth/2;
		int startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - dialogHeight/2;

		Rectangle dialogBounds = new Rectangle(startX, startY, dialogWidth, dialogHeight);

		this.setBounds(dialogBounds);

		Tile tile = Maze.getInstance().getCurrentTile();
		PlayerParty party = Maze.getInstance().getParty();

		DIYPane infoPane = new DIYPane(new DIYGridLayout(2,12,0,0));
		infoPane.setBounds(x+inset, y+inset+buttonPaneHeight, width, dialogHeight-buttonPaneHeight*3);

		infoPane.add(new DIYLabel(StringUtil.getUiLabel("rd.resting.danger"), DIYToolkit.Align.LEFT));
		infoPane.add(new DIYLabel(tile.getRestingDanger().toString(), DIYToolkit.Align.LEFT));

		infoPane.add(new DIYLabel(StringUtil.getUiLabel("rd.resting.efficiency"), DIYToolkit.Align.LEFT));
		infoPane.add(new DIYLabel(tile.getRestingEfficiency().toString(), DIYToolkit.Align.LEFT));

		infoPane.add(new DIYLabel(StringUtil.getUiLabel("rd.supplies.available"), DIYToolkit.Align.LEFT));
		infoPane.add(new DIYLabel(StringUtil.getUiLabel("rd.supplies.units",
			String.valueOf(party.getSupplies())), DIYToolkit.Align.LEFT));

		infoPane.add(new DIYLabel());
		infoPane.add(new DIYLabel());

		infoPane.add(new DIYLabel(StringUtil.getUiLabel("rd.supplies.needed"), DIYToolkit.Align.LEFT));
		infoPane.add(new DIYLabel());

		for (PlayerCharacter pc : party.getPlayerCharacters())
		{
			infoPane.add(new DIYLabel(pc.getDisplayName()));
			int suppliesNeededToRest = GameSys.getInstance().getSuppliesNeededToRest(pc);
			suppliesToConsume += suppliesNeededToRest;
			infoPane.add(new DIYLabel(StringUtil.getUiLabel("rd.supplies.units",
				String.valueOf(suppliesNeededToRest))));
		}

		DIYPane titlePane = new DIYPane(new DIYFlowLayout(0,0,DIYToolkit.Align.CENTER));

		titlePane.setBounds(x, y + inset, width, buttonPaneHeight);
		titlePane.add(new DIYLabel(title));

		DIYPane buttonPane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));
		buttonPane.setBounds(x, y+height- buttonPaneHeight - inset, width, buttonPaneHeight);

		rest = new DIYButton(StringUtil.getUiLabel("rd.rest"));
		rest.addActionListener(this);
		buttonPane.add(rest);

		cancel = new DIYButton(StringUtil.getUiLabel("common.cancel"));
		cancel.addActionListener(this);
		buttonPane.add(cancel);

		setBackground();

		this.add(titlePane);
		this.add(infoPane);
		this.add(buttonPane);
		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		switch(e.getKeyCode())
		{
			case KeyEvent.VK_ENTER:
				rest();
				break;
			case KeyEvent.VK_ESCAPE:
				cancel();
				break;
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == cancel)
		{
			cancel();
		}
		else if (event.getSource() == rest)
		{
			rest();
		}
	}

	/*-------------------------------------------------------------------------*/
	private void rest()
	{
		RestingProgressDialog dialog = new RestingProgressDialog(
			StringUtil.getUiLabel("rd.resting.progress",
				Maze.getInstance().getZone().getName()));

		// clear this dialog
		Maze.getInstance().getUi().clearDialog();

		// show the new dialog
		Maze.getInstance().getUi().showDialog(dialog);

		Tile tile = Maze.getInstance().getCurrentTile();
		Maze.log(Log.MEDIUM, "Party begins resting @ ["+
			Maze.getInstance().getZone().getName()+"] ["+tile.getCoords().x+","+tile.getCoords().y+"]");

		int nrTurns = 100;

		ProgressListener prog = dialog.getProgressListener();
		PlayerParty party = Maze.getInstance().getParty();

		RestingCheckpointEvent r4 = new RestingCheckpointEvent(
			10, nrTurns, 90, true, prog, party, tile, new StopRestingEvent());
		RestingCheckpointEvent r3 = new RestingCheckpointEvent(
			40, nrTurns, 50, true, prog, party, tile, r4);
		RestingCheckpointEvent r2 = new RestingCheckpointEvent(
			40, nrTurns, 10, true, prog, party, tile, r3);
		RestingCheckpointEvent r1 = new RestingCheckpointEvent(
			10, nrTurns, 0, false, prog, party, tile, r2);

		Maze.getInstance().appendEvents(
			new ModifySuppliesEvent(
				-suppliesToConsume,
				prog,
				StringUtil.getUiLabel(
					"rd.supplies.consumed",
					String.valueOf(suppliesToConsume))),
			new StartRestingEvent(),
			r1);
	}

	/*-------------------------------------------------------------------------*/
	private void cancel()
	{
		Maze.getInstance().setState(Maze.State.MOVEMENT);
		Maze.getInstance().getUi().clearDialog();
	}
}
