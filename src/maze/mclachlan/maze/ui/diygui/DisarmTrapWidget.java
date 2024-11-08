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
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.map.Trap;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class DisarmTrapWidget extends GeneralDialog implements ActionListener
{
	private final Trap trap;
	private final ChestOptionsCallback callback;
	private final PlayerCharacter pc;
	private DIYButton inspect, close;
	private DIYButton[] buttons;
	private List<DIYButton> buttonList;
	private final DIYLabel[] statusIndicators = new DIYLabel[8];
	private final int[] toolStatus = new int[8];
	private final BitSet disarmed = new BitSet(8);

	/*-------------------------------------------------------------------------*/
	public DisarmTrapWidget(
		Trap trap,
		ChestOptionsCallback callback,
		PlayerCharacter pc)
	{
		int width = DiyGuiUserInterface.SCREEN_WIDTH / 2;
		int height = DiyGuiUserInterface.SCREEN_HEIGHT / 2;

		int x = DiyGuiUserInterface.SCREEN_WIDTH/2 -width/2;
		int y = DiyGuiUserInterface.SCREEN_HEIGHT/2 -height/2;

		Rectangle bounds = new Rectangle(x, y, width, height);

		this.setBounds(bounds);
		this.trap = trap;
		this.callback = callback;
		this.pc = pc;

		for (int i = 0; i < toolStatus.length; i++)
		{
			if (pc.getModifier(Stats.Modifier.TRAP_SENSE) > 0)
			{
				toolStatus[i] = trap.getRequired().get(i) ?
					Trap.InspectionResult.PRESENT : Trap.InspectionResult.NOT_PRESENT;
			}
			else
			{
				toolStatus[i] = Trap.InspectionResult.UNKNOWN;
			}
		}

		this.buildGui();
	}

	/*-------------------------------------------------------------------------*/
	private void buildGui()
	{
		int buttonPaneHeight = getButtonPaneHeight();
		int titlePaneHeight = getTitlePaneHeight();
		int border = getBorder();
		int inset = getInset();

		int contentTop = y +border +inset +titlePaneHeight;
		int contentHeight = height -border*2 -inset*2 -titlePaneHeight -buttonPaneHeight;

		int column1x = x +border +inset;
		int column1Width = (width -border*2 -inset*5) /3;

		int column2x = column1x +column1Width +inset;
		int column2width = column1Width /2;

		int column3x = column2x +column2width +inset;
		int column4x = column3x +column2width +inset;

		String title;
		if (pc.getModifier(Stats.Modifier.TRAP_SENSE) > 0)
		{
			title = StringUtil.getUiLabel("dtw.name.trap.sense", pc.getName());
		}
		else
		{
			title = StringUtil.getUiLabel("dtw.title", pc.getName());
		}

		DIYPane titlePane = getTitlePane(title);

		DIYPane gridCol1 = new DIYPane(new DIYGridLayout(1, 4, 4, 4));
		gridCol1.setBounds(
			column1x,
			contentTop,
			column1Width,
			contentHeight);

		DIYPane gridCol2 = new DIYPane(new DIYGridLayout(1, 4, 4, 4));
		gridCol2.setBounds(
			column2x,
			contentTop, column2width,
			contentHeight);

		DIYPane gridCol3 = new DIYPane(new DIYGridLayout(1, 4, 4, 4));
		gridCol3.setBounds(
			column3x,
			contentTop,
			column2width,
			contentHeight);

		DIYPane gridCol4 = new DIYPane(new DIYGridLayout(1, 4, 4, 4));
		gridCol4.setBounds(
			column4x,
			contentTop,
			column1Width,
			contentHeight);

		DIYButton chisel = new DIYButton(StringUtil.getUiLabel("plw.chisel"));
		chisel.addActionListener(this);

		DIYButton crowbar = new DIYButton(StringUtil.getUiLabel("plw.crowbar"));
		crowbar.addActionListener(this);

		DIYButton drill = new DIYButton(StringUtil.getUiLabel("plw.drill"));
		drill.addActionListener(this);

		DIYButton hammer = new DIYButton(StringUtil.getUiLabel("plw.hammer"));
		hammer.addActionListener(this);

		DIYButton jackknife = new DIYButton(StringUtil.getUiLabel("plw.jackknife"));
		jackknife.addActionListener(this);

		DIYButton lockpick = new DIYButton(StringUtil.getUiLabel("plw.lockpick"));
		lockpick.addActionListener(this);

		DIYButton skeletonKey = new DIYButton(StringUtil.getUiLabel("plw.skeleton.key"));
		skeletonKey.addActionListener(this);

		DIYButton tensionWrench = new DIYButton(StringUtil.getUiLabel("plw.tension.wrench"));
		tensionWrench.addActionListener(this);

		for (int i = 0; i < statusIndicators.length; i++)
		{
			statusIndicators[i] = new DIYLabel("?");
		}

		buttons = new DIYButton[]
			{chisel, crowbar, drill, hammer, jackknife, lockpick, skeletonKey, tensionWrench};
		buttonList = Arrays.asList(buttons);

		gridCol1.add(chisel);
		gridCol1.add(crowbar);
		gridCol1.add(drill);
		gridCol1.add(hammer);

		gridCol2.add(statusIndicators[0]);
		gridCol2.add(statusIndicators[1]);
		gridCol2.add(statusIndicators[2]);
		gridCol2.add(statusIndicators[3]);

		gridCol3.add(statusIndicators[4]);
		gridCol3.add(statusIndicators[5]);
		gridCol3.add(statusIndicators[6]);
		gridCol3.add(statusIndicators[7]);

		gridCol4.add(jackknife);
		gridCol4.add(lockpick);
		gridCol4.add(skeletonKey);
		gridCol4.add(tensionWrench);

		DIYPane buttonPane = getButtonPane();

		inspect = new DIYButton(StringUtil.getUiLabel("dtw.inspect"));
		inspect.addActionListener(this);

		close = getCloseButton();
		close.addActionListener(this);

		buttonPane.add(inspect);

		this.add(titlePane);
		this.add(gridCol1);
		this.add(gridCol2);
		this.add(gridCol3);
		this.add(gridCol4);
		this.add(buttonPane);
		this.add(close);

		updateToolStatus(null);

		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	private void updateToolStatus(int[] status)
	{
		if (status == null)
		{
			status = toolStatus;
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
					statusIndicators[i].setText(StringUtil.getUiLabel("dtw.unknown"));
					break;
				case Trap.InspectionResult.PRESENT:
					if (disarmed.get(i))
					{
						statusIndicators[i].setText(StringUtil.getUiLabel("dtw.disarmed"));
					}
					else
					{
						statusIndicators[i].setText(StringUtil.getUiLabel("dtw.armed"));
					}
					break;
				case Trap.InspectionResult.NOT_PRESENT:
					statusIndicators[i].setText(StringUtil.getUiLabel("dtw.not.present"));
					buttons[i].setEnabled(false);
					break;
				default:
					throw new MazeException("Illegal status: "+status[i]);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();

		if (obj == inspect)
		{
			inspect();
			return true;
		}
		else if (obj == close)
		{
			cancelled();
			return true;
		}
		else if (buttonList.contains(obj))
		{
			int tool = buttonList.indexOf(obj);
			manipulateTool(tool);
			return true;
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private void manipulateTool(int tool)
	{
		MazeScript script = Database.getInstance().getMazeScript("_THIEF_TOOL_");
		Maze.getInstance().appendEvents(script.getEvents());
		int result = GameSys.getInstance().disarm(pc, trap, tool);

		switch (result)
		{
			case Trap.DisarmResult.NOTHING:
				break;
			case Trap.DisarmResult.DISARMED:
				buttons[tool].setEnabled(false);
				statusIndicators[tool].setText(StringUtil.getUiLabel("dtw.disarmed"));
				disarmed.set(tool);

				if (disarmed.equals(trap.getRequired()))
				{
					// all required are disarmed
					cancelled();
					Maze.getInstance().appendEvents(callback.executeChestContents());
				}
				break;
			case Trap.DisarmResult.SPRING_TRAP:
				cancelled();
				Maze.getInstance().appendEvents(callback.springTrap());
				break;
			default:
				throw new MazeException("Invalid result: "+result);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void inspect()
	{
		MazeScript script = Database.getInstance().getMazeScript("_INSPECT_TRAP_");
		Maze.getInstance().appendEvents(script.getEvents());
		
		int[] insResult = GameSys.getInstance().inspectTrap(pc, trap);

		if (insResult == null)
		{
			cancelled();
			Maze.getInstance().appendEvents(callback.springTrap());
			return;
		}

		updateToolStatus(insResult);
	}

	/*-------------------------------------------------------------------------*/
	private void cancelled()
	{
		Maze.getInstance().getUi().clearDialog();
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
			case KeyEvent.VK_ESCAPE -> cancelled();
			case KeyEvent.VK_I -> inspect();
			case KeyEvent.VK_C -> manipulateTool(0);
			case KeyEvent.VK_R -> manipulateTool(1);
			case KeyEvent.VK_D -> manipulateTool(2);
			case KeyEvent.VK_H -> manipulateTool(3);
			case KeyEvent.VK_J -> manipulateTool(4);
			case KeyEvent.VK_L -> manipulateTool(5);
			case KeyEvent.VK_S -> manipulateTool(6);
			case KeyEvent.VK_T -> manipulateTool(7);
		}
	}

}
