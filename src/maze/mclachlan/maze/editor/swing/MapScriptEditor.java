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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import mclachlan.crusader.MapScript;
import mclachlan.crusader.script.RandomLightingScript;
import mclachlan.crusader.script.SinusoidalLightingScript;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.data.v1.V1Utils;
import mclachlan.maze.game.Maze;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class MapScriptEditor extends JDialog implements ActionListener
{
	protected static final String SEP = ",";

	protected static final int CUSTOM = 0;
	protected static final int SINE_LIGHTING = 1;
	protected static final int RANDOM_LIGHTING = 2;

	protected static final int MAX = 3;

	static Map<Class, Integer> types;

	static
	{
		types = new HashMap<Class, Integer>();

		types.put(SinusoidalLightingScript.class, SINE_LIGHTING);
		types.put(RandomLightingScript.class, RANDOM_LIGHTING);
	}

	protected MapScript result;

	protected JButton ok, cancel;
	protected JComboBox type;
	protected CardLayout cards;
	protected JPanel controls;
	protected JTextField impl;
	protected int dirtyFlag;
	protected JTextField randomLightScriptTiles;
	protected JSpinner randomLightScriptFreq,
		randomLightScriptMinLightLevel, randomLightScriptMaxLightLevel;
	protected JTextField sineLightScriptTiles;
	protected JSpinner sineLightScriptFreq,
		sineLightScriptMinLightLevel, sineLightScriptMaxLightLevel;

	/*-------------------------------------------------------------------------*/
	public MapScriptEditor(
		Frame owner,
		MapScript mapScript,
		int dirtyFlag)
		throws HeadlessException
	{
		super(owner, "Edit Map Script", true);
		this.dirtyFlag = dirtyFlag;

		JPanel top = new JPanel();

		JPanel top1 = new JPanel();
		Vector<String> vec = new Vector<String>();
		for (int i=0; i<MAX; i++)
		{
			vec.addElement(describeType(i));
		}
		type = new JComboBox(vec);
		type.addActionListener(this);
		top1.add(new JLabel("Type:"));
		top1.add(type);

		top.add(top1);

		cards = new CardLayout(3,3);
		controls = new JPanel(cards);
		for (int i=0; i<MAX; i++)
		{
			JPanel c = getControls(i);
			controls.add(c, String.valueOf(i));
		}

		ok = new JButton("OK");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);

		JPanel buttons = new JPanel();
		buttons.add(ok);
		buttons.add(cancel);

		this.setLayout(new BorderLayout(3,3));
		this.add(top, BorderLayout.NORTH);
		this.add(controls, BorderLayout.CENTER);
		this.add(buttons, BorderLayout.SOUTH);

		if (mapScript != null)
		{
			setState(mapScript);
		}

		this.pack();
		setLocationRelativeTo(owner);
		this.setVisible(true);
	}

	/*-------------------------------------------------------------------------*/
	protected void setState(MapScript ms)
	{
		int srType;
		if (types.containsKey(ms.getClass()))
		{
			srType = types.get(ms.getClass());
		}
		else
		{
			srType = CUSTOM;
		}
		type.setSelectedIndex(srType);

		switch (srType)
		{
			case CUSTOM:
				impl.setText(ms.getClass().getName()); break;
			case RANDOM_LIGHTING:
				RandomLightingScript rls = (RandomLightingScript)ms;
				randomLightScriptTiles.setText(V1Utils.toStringInts(rls.getAffectedTiles(), SEP));
				randomLightScriptFreq.setValue(rls.getFrequency());
				randomLightScriptMinLightLevel.setValue(rls.getMinLightLevel());
				randomLightScriptMaxLightLevel.setValue(rls.getMaxLightLevel());
				break;
			case SINE_LIGHTING:
				SinusoidalLightingScript sls = (SinusoidalLightingScript)ms;
				sineLightScriptTiles.setText(V1Utils.toStringInts(sls.getAffectedTiles(), SEP));
				sineLightScriptFreq.setValue(sls.getFrequency());
				sineLightScriptMinLightLevel.setValue(sls.getMinLightLevel());
				sineLightScriptMaxLightLevel.setValue(sls.getMaxLightLevel());
				break;

			default: throw new MazeException("Invalid type "+srType);
		}
	}

	/*-------------------------------------------------------------------------*/
	JPanel getControls(int type)
	{
		switch (type)
		{
			case CUSTOM: return getCustomPanel();
			case RANDOM_LIGHTING: return getRandomLightingPanel();
			case SINE_LIGHTING: return getSineLightingPanel();
			default: throw new MazeException("Invalid type "+type);
		}
	}

	/*-------------------------------------------------------------------------*/
	protected JPanel getSineLightingPanel()
	{
		sineLightScriptTiles = new JTextField(30);
		sineLightScriptFreq = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
		sineLightScriptMinLightLevel = new JSpinner(new SpinnerNumberModel(0, 0, 64, 1));
		sineLightScriptMaxLightLevel = new JSpinner(new SpinnerNumberModel(0, 0, 64, 1));

		return dirtyGridLayoutCrap(
			new JLabel("Tiles:"), sineLightScriptTiles,
			new JLabel("Frequency:"), sineLightScriptFreq,
			new JLabel("Min Light Level:"), sineLightScriptMinLightLevel,
			new JLabel("Max Light Level:"), sineLightScriptMaxLightLevel);
	}

	/*-------------------------------------------------------------------------*/
	protected JPanel getRandomLightingPanel()
	{
		randomLightScriptTiles = new JTextField(30);
		randomLightScriptFreq = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
		randomLightScriptMinLightLevel = new JSpinner(new SpinnerNumberModel(0, 0, 64, 1));
		randomLightScriptMaxLightLevel = new JSpinner(new SpinnerNumberModel(0, 0, 64, 1));

		return dirtyGridLayoutCrap(
			new JLabel("Tiles:"), randomLightScriptTiles,
			new JLabel("Frequency:"), randomLightScriptFreq,
			new JLabel("Min Light Level:"), randomLightScriptMinLightLevel,
			new JLabel("Max Light Level:"), randomLightScriptMaxLightLevel);
	}

	/*-------------------------------------------------------------------------*/
	protected JPanel getCustomPanel()
	{
		impl = new JTextField(20);
		return dirtyGridLayoutCrap(new JLabel("Custom Impl: "), impl);
	}

	/*-------------------------------------------------------------------------*/
	protected JPanel dirtyGridLayoutCrap(JLabel label, Component field)
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		result.add(label, gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		result.add(field, gbc);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	protected JPanel dirtyGridLayoutCrap(Component... comps)
	{
		JPanel panel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		for (int i=0; i<comps.length; i+=2)
		{
			if (i == comps.length-2)
			{
				gbc.weighty = 1;
			}
			gbc.gridx = 0;
			gbc.weightx = 0;
			if (comps[i+1] == null)
			{
				gbc.gridwidth = 2;
				gbc.weightx = 1;
			}
			panel.add(comps[i], gbc);
			gbc.gridx = 1;
			gbc.weightx = 1;
			if (comps[i+1] == null)
			{
				gbc.gridwidth = 1;
			}
			else
			{
				panel.add(comps[i+1], gbc);
			}
			gbc.gridy++;
		}

		return panel;
	}

	/*-------------------------------------------------------------------------*/
	static String describeType(int type)
	{
		switch (type)
		{
			case CUSTOM: return "Custom"; 
			case RANDOM_LIGHTING: return "Random Lighting";
			case SINE_LIGHTING: return "Sine Lighting";
			default: throw new MazeException("Invalid type "+type);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == type)
		{
			cards.show(controls, String.valueOf(type.getSelectedIndex()));
		}
		else if (e.getSource() == ok)
		{
			// save changes
			setResult();
			if (SwingEditor.instance != null)
			{
				SwingEditor.instance.setDirty(dirtyFlag);
			}
			setVisible(false);
		}
		else if (e.getSource() == cancel)
		{
			// discard changes
			setVisible(false);
		}
	}

	/*-------------------------------------------------------------------------*/
	public MapScript getResult()
	{
		return result;
	}

	/*-------------------------------------------------------------------------*/
	protected void setResult()
	{
		int srType = type.getSelectedIndex();
		switch (srType)
		{
			case CUSTOM:
				try
				{
					Class clazz = Class.forName(impl.getText());
					this.result = (MapScript)clazz.newInstance();
				}
				catch (Exception x)
				{
					throw new MazeException(x);
				}
				break;
			case RANDOM_LIGHTING:
				result = new RandomLightingScript(
					V1Utils.fromStringInts(randomLightScriptTiles.getText(), SEP),
					(Integer)randomLightScriptFreq.getValue(),
					(Integer)randomLightScriptMinLightLevel.getValue(),
					(Integer)randomLightScriptMaxLightLevel.getValue());
				break;
			case SINE_LIGHTING:
				result = new SinusoidalLightingScript(
					V1Utils.fromStringInts(sineLightScriptTiles.getText(), SEP),
					(Integer)sineLightScriptFreq.getValue(),
					(Integer)sineLightScriptMinLightLevel.getValue(),
					(Integer)sineLightScriptMaxLightLevel.getValue());
				break;
			default: throw new MazeException("Invalid type "+srType);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		Loader loader = new V1Loader();
		Saver saver = new V1Saver();
		new Database(loader, saver);
		loader.init(Maze.getStubCampaign());

		JFrame owner = new JFrame("test");
		owner.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		while (1==1)
		{
			MapScriptEditor test = new MapScriptEditor(owner, new RandomLightingScript(new int[]{1}, 1, 1, 1), -1);
			System.out.println("test.result = [" + test.result + "]");
		}
	}
}