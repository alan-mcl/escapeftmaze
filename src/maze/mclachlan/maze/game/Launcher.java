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

package mclachlan.maze.game;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.audio.WavAudioPlayer;
import mclachlan.maze.data.v1.V1Utils;
import mclachlan.maze.editor.swing.CampaignPanel;
import mclachlan.maze.ui.UserInterface;
import mclachlan.maze.ui.diygui.DiyGuiUserInterface;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class Launcher implements ActionListener
{
	JButton ok, cancel;
	List<Campaign> campaignList;
	JFrame frame;
	Map<String, String> config;
	CampaignPanel campaignPanel;

	/*-------------------------------------------------------------------------*/
	public Launcher() throws Exception
	{
		frame = new JFrame("Escape From The Maze");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


		ok = new JButton("OK");
		ok.addActionListener(this);

		cancel = new JButton("Cancel");
		cancel.addActionListener(this);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(ok);
		buttonPanel.add(cancel);

		JPanel panel = new JPanel(new BorderLayout(5,5));
		panel.setBorder(BorderFactory.createRaisedBevelBorder());

		try
		{
			config = getConfig();
			campaignList = loadCampaigns();

			campaignPanel = new CampaignPanel(campaignList);
			panel.add(campaignPanel, BorderLayout.CENTER);
			panel.add(buttonPanel, BorderLayout.SOUTH);
			frame.add(panel);
		}
		catch (Exception x)
		{
			JOptionPane.showMessageDialog(frame, "ERROR");
			x.printStackTrace();
			System.exit(1);
		}

		//
		// Here's how it works:
		// If there is only one campaign, or if the maze.campaign config property
		// is set, launch straight into that one.  Else show the launcher dialog.
		// The launcher dialog can be explicitly shown using the
		// maze.show_launcher=true config property
		//

		boolean showLauncher = Boolean.valueOf(config.get(Maze.AppConfig.SHOW_LAUNCHER));

		if (campaignList.size() == 1 && !showLauncher)
		{
			frame.dispose();
			startGame(campaignList.get(0));
		}
		else if (config.get(Maze.AppConfig.CAMPAIGN) != null && !showLauncher)
		{
			frame.dispose();
			String campaign = config.get(Maze.AppConfig.CAMPAIGN);
			for (Campaign c : campaignList)
			{
				if (c.getName().equals(campaign))
				{
					startGame(c);
					break;
				}
			}
		}
		else
		{
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			int centerX = (int)(d.getWidth()/2);
			int centerY = (int)(d.getHeight()/2);
			int width = 500;
			int height = 500;

			frame.setBounds(centerX-width/2, centerY-height/2, width, height);
			frame.setVisible(true);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static List<Campaign> loadCampaigns() throws IOException
	{
		File dir = new File("./data");
		if (!dir.isDirectory())
		{
			throw new MazeException("Cannot locate data directory ["+dir.getCanonicalPath()+"]");
		}

		List<File> propertiesFiles = new ArrayList<File>();
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++)
		{
			if (files[i].isDirectory())
			{
				propertiesFiles.add(new File(files[i], "campaign.cfg"));
			}
		}

		List<Campaign> result = new ArrayList<Campaign>();

		for (File f : propertiesFiles)
		{
			if (!f.exists())
			{
				throw new MazeException("Cannot locate campaign file ["+f.getCanonicalPath()+"]");
			}

			Properties p = new Properties();
			FileInputStream fis = new FileInputStream(f);
			p.load(fis);
			fis.close();

			String name = f.getParentFile().getName().split("\\.")[0];
			String displayName = p.getProperty("displayName");
			String description = V1Utils.replaceNewlines(p.getProperty("description"));
			String startingScript = p.getProperty("startingScript");
			String defaultRace = p.getProperty("defaultRace");
			String defaultPortrait = p.getProperty("defaultPortrait");
			String introScript = p.getProperty("introScript");

			result.add(new Campaign(
				name,
				displayName,
				description,
				startingScript,
				defaultRace,
				defaultPortrait, 
				introScript));
		}

		if (result.size() == 0)
		{
			throw new MazeException("No campaigns found!");
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static Map<String, String> getConfig() throws IOException
	{
		Properties p = new Properties();
		FileInputStream inStream = new FileInputStream("maze.cfg");
		p.load(inStream);
		inStream.close();

		HashMap<String, String> config = new HashMap<String, String>();

		Iterator i = p.keySet().iterator();
		while (i.hasNext())
		{
			String key = (String)i.next();
			String value = (String)p.get(key);
			config.put(key, value);
		}

		return config;
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == cancel)
		{
			frame.setVisible(false);
			System.exit(0);
		}
		else if (e.getSource() == ok)
		{
			Campaign c = campaignPanel.getCampaign();
			try
			{
				startGame(c);
			}
			catch (Exception x)
			{
				// todo: cleanup
				JOptionPane.showMessageDialog(frame, "ERROR");
				x.printStackTrace();
				frame.setVisible(false);
				System.exit(1);
			}
			frame.setVisible(false);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void startGame(Campaign c)
		throws Exception
	{
		Maze maze = new Maze(config, c);

		maze.initAudio(new WavAudioPlayer());
		maze.initLog(getLog(config));
		maze.initState();
		// Beware the dependencies between components here.
		maze.initDb();
		maze.initSystems();
		maze.startThreads();
		maze.initUi(getUi(config));
	}

	/*-------------------------------------------------------------------------*/
	private Log getLog(Map<String, String> config) throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		String log_impl = this.config.get(Maze.AppConfig.LOG_IMPL);
		Class log_class = Class.forName(log_impl);
		Log log = (Log)log_class.newInstance();
		int logLevel = Integer.parseInt(config.get(Maze.AppConfig.LOG_LEVEL));
		log.setLevel(logLevel);

		return log;
	}

	/*-------------------------------------------------------------------------*/
	private UserInterface getUi(Map<String, String> config) throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		String ui_impl = this.config.get(Maze.AppConfig.UI_IMPL);
		Class ui_class = Class.forName(ui_impl);
		return (DiyGuiUserInterface)ui_class.newInstance();
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		new Launcher();
	}
}
