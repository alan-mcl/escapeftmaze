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

import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.*;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Trap;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class PickLockWidget extends GeneralDialog implements ActionListener
{
	private PlayerCharacter pc;
	private DIYButton cancel;
	private DIYButton[] buttons;
	private List<DIYButton> buttonList;
	private DIYLabel[] statusIndicators = new DIYLabel[8];
	private int[] toolStatus = new int[8];
	private BitSet picked = new BitSet(8);
	private Portal portal;

	/*-------------------------------------------------------------------------*/
	public PickLockWidget(
		Portal portal,
		Rectangle bounds,
		PlayerCharacter pc)
	{
		super(bounds);
		this.portal = portal;
		this.pc = pc;

		for (int i = 0; i < toolStatus.length; i++)
		{
			if (portal.getRequired().get(i))
			{
				toolStatus[i] = Trap.InspectionResult.PRESENT;
			}
			else
			{
				toolStatus[i] = Trap.InspectionResult.NOT_PRESENT;
			}
		}

		this.buildGui();
		this.updateToolStatus(toolStatus);
	}

	/*-------------------------------------------------------------------------*/
	private void buildGui()
	{
		setBackground();

		DIYPane gridCol1 = new DIYPane(new DIYGridLayout(1, 5, 4, 4));
		int buttonPaneHeight = 40;
		gridCol1.setBounds(x, y, width/3, height- buttonPaneHeight);
		gridCol1.setInsets(new Insets(10, 10, 0, 0));

		DIYPane gridCol2 = new DIYPane(new DIYGridLayout(1, 5, 4, 4));
		gridCol2.setBounds(x+width/3, y, width/6, height- buttonPaneHeight);
		gridCol2.setInsets(new Insets(10, 0, 0, 0));

		DIYPane gridCol3 = new DIYPane(new DIYGridLayout(1, 5, 4, 4));
		gridCol3.setBounds(x+width/3+width/6, y, width/6, height- buttonPaneHeight);
		gridCol3.setInsets(new Insets(10, 0, 0, 0));

		DIYPane gridCol4 = new DIYPane(new DIYGridLayout(1, 5, 4, 4));
		gridCol4.setBounds(x+width/3*2, y, width/3, height- buttonPaneHeight);
		gridCol4.setInsets(new Insets(10, 0, 0, 10));

		DIYButton chisel = new DIYButton("(C)hisel");
		chisel.addActionListener(this);

		DIYButton crowbar = new DIYButton("C(r)owbar");
		crowbar.addActionListener(this);

		DIYButton drill = new DIYButton("(D)rill");
		drill.addActionListener(this);

		DIYButton hammer = new DIYButton("(H)ammer");
		hammer.addActionListener(this);

		DIYButton jackknife = new DIYButton("(J)ackknife");
		jackknife.addActionListener(this);

		DIYButton lockpick = new DIYButton("(L)ock Pick");
		lockpick.addActionListener(this);

		DIYButton skeletonKey = new DIYButton("(S)keleton Key");
		skeletonKey.addActionListener(this);

		DIYButton tensionWrench = new DIYButton("(T)ension Wrench");
		tensionWrench.addActionListener(this);

		for (int i = 0; i < statusIndicators.length; i++)
		{
			statusIndicators[i] = new DIYLabel("?");
		}

		buttons = new DIYButton[]
			{chisel, crowbar, drill, hammer, jackknife, lockpick, skeletonKey, tensionWrench};
		buttonList = Arrays.asList(buttons);

		gridCol1.add(new DIYLabel(pc.getName()));
		gridCol1.add(chisel);
		gridCol1.add(crowbar);
		gridCol1.add(drill);
		gridCol1.add(hammer);

		gridCol2.add(new DIYLabel());
		gridCol2.add(statusIndicators[0]);
		gridCol2.add(statusIndicators[1]);
		gridCol2.add(statusIndicators[2]);
		gridCol2.add(statusIndicators[3]);

		gridCol3.add(new DIYLabel());
		gridCol3.add(statusIndicators[4]);
		gridCol3.add(statusIndicators[5]);
		gridCol3.add(statusIndicators[6]);
		gridCol3.add(statusIndicators[7]);

		gridCol4.add(new DIYLabel());
		gridCol4.add(jackknife);
		gridCol4.add(lockpick);
		gridCol4.add(skeletonKey);
		gridCol4.add(tensionWrench);

		DIYPane buttonPane = new DIYPane(new DIYFlowLayout(10, 5, DIYToolkit.Align.CENTER));
		buttonPane.setBounds(x, y+height- buttonPaneHeight, width, buttonPaneHeight);

		cancel = new DIYButton("Cancel");
		cancel.addActionListener(this);

		buttonPane.add(cancel);

		this.add(gridCol1);
		this.add(gridCol2);
		this.add(gridCol3);
		this.add(gridCol4);
		this.add(buttonPane);

		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	private void updateToolStatus(int[] status)
	{
		if (status == null)
		{
			return;
		}

		// update the existing status if it's better
		for (int i = 0; i < status.length; i++)
		{
			if (toolStatus[i] == Trap.InspectionResult.UNKNOWN)
			{
				toolStatus[i] = status[i];
			}

			switch (toolStatus[i])
			{
				case Trap.InspectionResult.UNKNOWN:
					statusIndicators[i].setText("?");
					break;
				case Trap.InspectionResult.PRESENT:
					if (picked.get(i))
					{
						statusIndicators[i].setText("picked");
					}
					else
					{
						statusIndicators[i].setText("required");
					}
					break;
				case Trap.InspectionResult.NOT_PRESENT:
					statusIndicators[i].setText("-");
					buttons[i].setEnabled(false);
					break;
				default:
					throw new MazeException("Illegal status: "+status[i]);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();

		if (obj == cancel)
		{
			cancel();
		}
		else if (buttonList.contains(obj))
		{
			int tool = buttonList.indexOf(obj);
			manipulateTool(tool);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void manipulateTool(int tool)
	{
		if (!buttonList.get(tool).isEnabled())
		{
			return;
		}
		
		MazeScript script = Database.getInstance().getScript("_THIEF_TOOL_");
		Maze.getInstance().resolveEvents(script.getEvents());
		int result = GameSys.getInstance().pickLock(pc, portal, tool);

		switch (result)
		{
			case Trap.DisarmResult.NOTHING:
				break;
			case Trap.DisarmResult.DISARMED:
				buttons[tool].setEnabled(false);
				statusIndicators[tool].setText("picked");
				picked.set(tool);

				if (picked.equals(portal.getRequired()))
				{
					// all required are disarmed
					cancel();
					script = Database.getInstance().getScript("_UNLOCK_");
					Maze.getInstance().resolveEvents(script.getEvents());
					DiyGuiUserInterface.instance.addMessage("UNLOCKED!");
					portal.setState(Portal.State.UNLOCKED);
					Maze.getInstance().setState(Maze.State.MOVEMENT);
				}
				break;
			case Trap.DisarmResult.SPRING_TRAP:
				DiyGuiUserInterface.instance.addMessage("OOPS!");
				cancel();
				break;
			default:
				throw new MazeException("Invalid result: "+result);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		if (e.getID() != KeyEvent.KEY_PRESSED)
		{
			return;
		}
		
		switch(e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE:
				cancel();
				break;
			case KeyEvent.VK_C:
				manipulateTool(0);
				break;
			case KeyEvent.VK_R:
				manipulateTool(1);
				break;
			case KeyEvent.VK_D:
				manipulateTool(2);
				break;
			case KeyEvent.VK_H:
				manipulateTool(3);
				break;
			case KeyEvent.VK_J:
				manipulateTool(4);
				break;
			case KeyEvent.VK_L:
				manipulateTool(5);
				break;
			case KeyEvent.VK_S:
				manipulateTool(6);
				break;
			case KeyEvent.VK_T:
				manipulateTool(7);
				break;
		}
	}

	/*-------------------------------------------------------------------------*/
	private void cancel()
	{
		Maze.getInstance().getUi().clearDialog();
	}
}
