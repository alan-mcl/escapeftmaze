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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Maze;

/**
 *
 */
public class EditorPanelDialog extends JDialog implements WindowListener
{
	private SwingEditor.EditingControls editingControls;

	/*-------------------------------------------------------------------------*/
	public EditorPanelDialog(String title, final EditorPanel editorPanel)
	{
		super(SwingEditor.instance, title, true);

		addWindowListener(this);

		editingControls = new SwingEditor.EditingControls(
			SwingEditor.instance)
		{
			public void exit()
			{
				SwingEditor.instance.refreshEditorPanel(editorPanel);
				EditorPanelDialog.this.setVisible(false);
			}

			public EditorPanel getEditorPanel()
			{
				return editorPanel;
			}
		};
		JPanel bottom = editingControls.getBottomPanel();

		setLayout(new BorderLayout());

		this.add(editorPanel, BorderLayout.CENTER);
		this.add(bottom, BorderLayout.SOUTH);

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)(d.getWidth()/2);
		int centerY = (int)(d.getHeight()/2);
		int width = (int)(d.getWidth()-200);
		int height = (int)(d.getHeight()-200);
		this.setBounds(centerX-width/2, centerY-height/2, width, height);

		this.setLocationRelativeTo(SwingEditor.instance);
		setVisible(true);
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		Loader loader = new V1Loader();
		Saver saver = new V1Saver();
		new Database(loader, saver, Maze.getStubCampaign());

		new EditorPanelDialog("TESTING", new MazeScriptPanel());
		System.exit(0);
	}

	public void windowOpened(WindowEvent e)
	{
	}

	public void windowClosing(WindowEvent e)
	{
		editingControls.exit();
	}

	public void windowClosed(WindowEvent e)
	{
	}

	public void windowIconified(WindowEvent e)
	{
	}

	public void windowDeiconified(WindowEvent e)
	{
	}

	public void windowActivated(WindowEvent e)
	{
	}

	public void windowDeactivated(WindowEvent e)
	{
	}
}
