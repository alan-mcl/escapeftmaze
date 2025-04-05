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
import mclachlan.crusader.Map.SkyConfig;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.ui.diygui.NullProgressListener;
import mclachlan.maze.util.MazeException;


/**
 *
 */
public class SkyConfigEditor extends JDialog implements ActionListener
{
	public static final int MAX = 400;
	
	private static String[] FACINGS =
		{
			"Unchanged",
			"North",
			"South",
			"East",
			"West",
		};

	private SkyConfig result;

	private int dirtyFlag;
	private CardLayout cards;
	private JPanel controls;
	private JButton ok, cancel;
	private JComboBox type;
	private JComboBox cylinderSkyImage;
	private JButton bottomColour;
	private JButton topColour;
	private JComboBox ceilingImage;
	private JSpinner ceilingHeight, ceilingHeightObject;
	private JComboBox cubeNorth;
	private JComboBox cubeSouth;
	private JComboBox cubeEast;
	private JComboBox cubeWest;
	private JComboBox objectTexture, objectTextureSphere;
	private JSpinner sphereRadius;

	/*-------------------------------------------------------------------------*/
	public SkyConfigEditor(Frame owner, SkyConfig event, int dirtyFlag) throws HeadlessException
	{
		super(owner, "Edit Sky Config", true);
		this.dirtyFlag = dirtyFlag;

		JPanel top = new JPanel();
		Vector<String> types = new Vector<>();
		for (SkyConfig.Type t : SkyConfig.Type.values())
		{
			String str = describeType(t);
			if (str != null)
			{
				types.addElement(str);
			}
		}

		type = new JComboBox(types);
		type.addActionListener(this);
		top.add(new JLabel("Type"));
		top.add(type);

		cards = new CardLayout(3, 3);
		controls = new JPanel(cards);
		for (SkyConfig.Type t : SkyConfig.Type.values())
		{
			JPanel c = getControls(t);
			if (c != null)
			{
				controls.add(c, t.name());
			}
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

		if (event != null)
		{
			setState(event);
		}

		this.pack();
		setLocationRelativeTo(owner);
		this.setVisible(true);
	}

	/*-------------------------------------------------------------------------*/
	private void setState(SkyConfig e)
	{
		type.setSelectedItem(describeType(e.getType()));

		switch (e.getType())
		{
			case CYLINDER_IMAGE ->
			{
				if (e.getCylinderSkyImage() != null)
					cylinderSkyImage.setSelectedItem(e.getCylinderSkyImage().getName());
			}
			case CYLINDER_GRADIENT ->
			{
				bottomColour.setBackground(new Color(e.getBottomColour()));
				topColour.setBackground(new Color(e.getTopColour()));
			}
			case HIGH_CEILING_IMAGE ->
			{
				if (e.getCeilingImage() != null)
					ceilingImage.setSelectedItem(e.getCeilingImage().getName());
				ceilingHeight.setValue(e.getCeilingHeight());
			}
			case CUBEMAP_IMAGES ->
			{
				if (e.getCubeNorth() != null)
					cubeNorth.setSelectedItem(e.getCubeNorth().getName());
				if (e.getCubeSouth() != null)
					cubeSouth.setSelectedItem(e.getCubeSouth().getName());
				if (e.getCubeEast() != null)
					cubeEast.setSelectedItem(e.getCubeEast().getName());
				if (e.getCubeWest() != null)
					cubeWest.setSelectedItem(e.getCubeWest().getName());
			}
			case OBJECTS_HIGH_CEILING ->
			{
				if (e.getObjectTexture() != null)
					objectTexture.setSelectedItem(e.getObjectTexture().getName());
				ceilingHeightObject.setValue(e.getCeilingHeight());
			}
			case OBJECTS_SPHERE ->
			{
				if (e.getObjectTexture() != null)
					objectTextureSphere.setSelectedItem(e.getObjectTexture().getName());
				sphereRadius.setValue(e.getSphereRadius());
			}
			default -> throw new MazeException("invalid "+e.getType());
		}
	}

	/*-------------------------------------------------------------------------*/
	private void saveResult()
	{
		switch (SkyConfig.Type.valueOf((String)type.getSelectedItem()))
		{
			case CYLINDER_IMAGE ->
			{
				result = new SkyConfig(
					SkyConfig.Type.CYLINDER_IMAGE,
					Database.getInstance().getMazeTexture((String)cylinderSkyImage.getSelectedItem()).getTexture(),
					0,0,null, 0, null, null, null, null, null, 0);
			}
			case CYLINDER_GRADIENT ->
			{
				result = new SkyConfig(
					SkyConfig.Type.CYLINDER_GRADIENT,
					null,
					bottomColour.getBackground().getRGB(),
					topColour.getBackground().getRGB(),
					null, 0, null, null, null, null, null, 0);
			}
			case HIGH_CEILING_IMAGE ->
			{
				result = new SkyConfig(
					SkyConfig.Type.HIGH_CEILING_IMAGE,
					null, 0,0,
					Database.getInstance().getMazeTexture((String)ceilingImage.getSelectedItem()).getTexture(),
					(int)ceilingHeight.getValue(),
					null, null, null, null, null, 0);

			}
			case CUBEMAP_IMAGES ->
			{
				result = new SkyConfig(
					SkyConfig.Type.CUBEMAP_IMAGES,
					null, 0,0,
					null, 0,
					Database.getInstance().getMazeTexture((String)cubeNorth.getSelectedItem()).getTexture(),
					Database.getInstance().getMazeTexture((String)cubeSouth.getSelectedItem()).getTexture(),
					Database.getInstance().getMazeTexture((String)cubeEast.getSelectedItem()).getTexture(),
					Database.getInstance().getMazeTexture((String)cubeWest.getSelectedItem()).getTexture(),
					null, 0);
			}
			case OBJECTS_HIGH_CEILING ->
			{
				result = new SkyConfig(
					SkyConfig.Type.OBJECTS_HIGH_CEILING,
					null, 0,0,
					null,
					(int)ceilingHeightObject.getValue(),
					null, null, null, null,
					Database.getInstance().getMazeTexture((String)objectTexture.getSelectedItem()).getTexture(), 0);
			}
			case OBJECTS_SPHERE ->
			{
				result = new SkyConfig(
					SkyConfig.Type.OBJECTS_SPHERE,
					null, 0,0,
					null,
					0,
					null, null, null, null,
					Database.getInstance().getMazeTexture((String)objectTextureSphere.getSelectedItem()).getTexture(),
					(int)sphereRadius.getValue());
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public SkyConfig getResult()
	{
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getControls(SkyConfig.Type type)
	{
		return switch (type)
		{
			case CYLINDER_IMAGE -> getCylinderImagePanel();
			case CYLINDER_GRADIENT -> getCylinderGradientPanel();
			case HIGH_CEILING_IMAGE -> getHighCeilingImagePanel();
			case CUBEMAP_IMAGES -> getCubeMapPanel();
			case OBJECTS_HIGH_CEILING -> getObjectsHighCeilingPanel();
			case OBJECTS_SPHERE -> getObjectsSpherePanel();
			default -> throw new MazeException("invalid "+type);
		};
	}

	private JPanel getObjectsSpherePanel()
	{
		Vector<String> vec =
			new Vector<>(Database.getInstance().getMazeTextures().keySet());
		Collections.sort(vec);

		objectTextureSphere = new JComboBox(vec);
		sphereRadius = new JSpinner(new SpinnerNumberModel(10, 10, 99, 1));

		return dirtyGridBagCrap(
			new JLabel("Object Texture:"), objectTextureSphere,
			new JLabel("Sphere Radius:"), sphereRadius);
	}

	private JPanel getObjectsHighCeilingPanel()
	{
		Vector<String> vec =
			new Vector<>(Database.getInstance().getMazeTextures().keySet());
		Collections.sort(vec);

		ceilingHeightObject = new JSpinner(new SpinnerNumberModel(10, 1, 99, 1));
		objectTexture = new JComboBox(vec);

		return dirtyGridBagCrap(
			new JLabel("Object Texture:"), objectTexture,
			new JLabel("Ceiling Height:"), ceilingHeightObject);
	}

	private JPanel getCubeMapPanel()
	{
		Vector<String> vec =
			new Vector<>(Database.getInstance().getMazeTextures().keySet());
		Collections.sort(vec);

		cubeNorth = new JComboBox(vec);
		cubeSouth = new JComboBox(vec);
		cubeEast = new JComboBox(vec);
		cubeWest = new JComboBox(vec);

		return dirtyGridBagCrap(
			new JLabel("North Texture:"), cubeNorth,
			new JLabel("South Texture:"), cubeSouth,
			new JLabel("East Texture:"), cubeEast,
			new JLabel("West Texture:"), cubeWest);
	}

	private JPanel getHighCeilingImagePanel()
	{
		Vector<String> vec =
			new Vector<>(Database.getInstance().getMazeTextures().keySet());
		Collections.sort(vec);

		ceilingImage = new JComboBox(vec);
		ceilingHeight = new JSpinner(new SpinnerNumberModel(10, 1, 99, 1));

		return dirtyGridBagCrap(
			new JLabel("Ceiling Image:"), ceilingImage,
			new JLabel("Ceiling Height:"), ceilingHeight);
	}

	private JPanel getCylinderGradientPanel()
	{
		bottomColour = new JButton("...");
		topColour = new JButton("...");

		bottomColour.addActionListener(this);
		topColour.addActionListener(this);

		return dirtyGridBagCrap(
			new JLabel("Bottom Colour:"), bottomColour,
			new JLabel("Top Colour:"), topColour);
	}

	private JPanel getCylinderImagePanel()
	{
		Vector<String> vec =
			new Vector<>(Database.getInstance().getMazeTextures().keySet());
		Collections.sort(vec);

		cylinderSkyImage = new JComboBox(vec);

		return dirtyGridBagCrap(
			new JLabel("Sky Image:"), cylinderSkyImage);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel dirtyGridBagCrap(Component... comps)
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();
		for (int i=0; i<comps.length; i+=2)
		{
			dodgyGridBagShite(result, comps[i], comps[i+1], gbc);
		}

		gbc.weighty = 1.0;
		dodgyGridBagShite(result, new JLabel(), new JLabel(), gbc);

		return result;
	}
	
	/*-------------------------------------------------------------------------*/
	protected void dodgyGridBagShite(JPanel panel, Component a, Component b, GridBagConstraints gbc)
	{
		gbc.weightx = 0.0;
		gbc.gridx=0;
		panel.add(a, gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		panel.add(b, gbc);
		gbc.gridy++;
	}

	/*-------------------------------------------------------------------------*/
	protected GridBagConstraints createGridBagConstraints()
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.WEST;
		return gbc;
	}

	/*-------------------------------------------------------------------------*/
	private void dirtyGridLayoutCrap(JPanel panel, Component... comps)
	{
		panel.setLayout(new GridBagLayout());
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
	}

	/*-------------------------------------------------------------------------*/
	private String describeType(SkyConfig.Type type)
	{
		return type.name();
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == type)
		{
			cards.show(controls, String.valueOf(type.getSelectedItem()));
		}
		else if (e.getSource() == ok)
		{
			// save changes
			saveResult();
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
		else if (e.getSource() == topColour)
		{
			Color c = JColorChooser.showDialog(
				SwingEditor.instance,
				"Gradient Colour",
				Color.BLACK);

			topColour.setBackground(c);

			SwingEditor.instance.setDirty(dirtyFlag);
		}
		else if (e.getSource() == bottomColour)
		{
			Color c = JColorChooser.showDialog(
				SwingEditor.instance,
				"Gradient Colour",
				Color.BLACK);

			bottomColour.setBackground(c);

			SwingEditor.instance.setDirty(dirtyFlag);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		Loader loader = new V1Loader();
		Saver saver = new V1Saver();
		Database db = new Database(loader, saver, Maze.getStubCampaign());
		db.initImpls();
		db.initCaches(new NullProgressListener());


		JFrame owner = new JFrame("test");
		owner.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		while (1==1)
		{
			SkyConfigEditor test = new SkyConfigEditor(owner, null, -1);
			System.out.println("test.result = [" + test.result + "]");
		}
	}

}
