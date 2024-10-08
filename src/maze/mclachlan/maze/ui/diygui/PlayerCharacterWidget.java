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
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYComboBox;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPanel;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.RendererProperties;
import mclachlan.diygui.util.HashMapMutableTree;
import mclachlan.diygui.util.MutableTree;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.EquipIntention;
import mclachlan.maze.stat.condition.Condition;

/**
 * Widget to display an actor's details.
 */
public class PlayerCharacterWidget extends DIYPanel
	implements ActionListener, ActorActionOption.ActionOptionCallback
{
	private final DIYLabel portrait, portraitFrame, nameLabel, classLabel, nameBoard;
	private final DIYLabel leftHandSlot, rightHandSlot, leftHandItem, rightHandItem;
	private final FilledBarWidget hpBar, apBar, mpBar;
	private PlayerCharacter playerCharacter;
	private final int index;

	private Rectangle portraitBounds, leftHandBounds, rightHandBounds,
		hpBarBounds, apBarBounds, mpBarBounds, conditionsArea;

	private final DIYButton levelUp;
	private final DIYComboBox<ActorActionOption> action;
	private final DIYComboBox<PlayerCharacter.Stance> stance;

	private final Object pcMutex = new Object();

	private Map<Rectangle, Condition> conditionBounds;
	private Rectangle nameLabelBounds;
	private Rectangle classLabelBounds;

	/*-------------------------------------------------------------------------*/
	public PlayerCharacterWidget(int index, Rectangle bounds)
	{
		super(bounds);
		this.index = index;
		setStyle(Style.PANEL_LIGHT);
		RendererProperties rp = DIYToolkit.getInstance().getRendererProperties();

		// portrait, name, class
		portrait = new DIYLabel();
		portraitFrame = new DIYLabel();

		portrait.addActionListener(this);
		portraitFrame.addActionListener(this);

		nameLabel = new DIYLabel("", DIYToolkit.Align.CENTER);
		nameLabel.setForegroundColour(Constants.Colour.GOLD);
		classLabel = new DIYLabel("", DIYToolkit.Align.CENTER);
//		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
//		Font f = defaultFont.deriveFont(Font.BOLD, defaultFont.getSize() -2);
//		classLabel.setFont(f);
		nameBoard = new DIYLabel();

		nameLabel.addActionListener(this);
		classLabel.addActionListener(this);
		nameBoard.addActionListener(this);

		// hand contents
		leftHandSlot = new DIYLabel(rp.getImageResource("icon/itemslot"));
		rightHandSlot = new DIYLabel(rp.getImageResource("icon/itemslot"));
		leftHandItem = new DIYLabel();
		rightHandItem = new DIYLabel();

		leftHandSlot.addActionListener(this);
		leftHandItem.addActionListener(this);
		rightHandSlot.addActionListener(this);
		rightHandItem.addActionListener(this);

		// resources
		hpBar = new FilledBarWidget(0,0);
		hpBar.setBarColour(Constants.Colour.COMBAT_RED);
		hpBar.setOrientation(FilledBarWidget.Orientation.VERTICAL);
//		hpBar.setTextType(FilledBarWidget.InnerTextType.CURRENT);
		apBar = new FilledBarWidget(0,0);
		apBar.setBarColour(Constants.Colour.STEALTH_GREEN);
		apBar.setOrientation(FilledBarWidget.Orientation.VERTICAL);
//		apBar.setTextType(FilledBarWidget.InnerTextType.CURRENT);
		mpBar = new FilledBarWidget(0,0);
		mpBar.setBarColour(Constants.Colour.MAGIC_BLUE);
		mpBar.setOrientation(FilledBarWidget.Orientation.VERTICAL);
//		mpBar.setTextType(FilledBarWidget.InnerTextType.CURRENT);

		hpBar.addActionListener(this);
		apBar.addActionListener(this);
		mpBar.addActionListener(this);

		// lvl up
		levelUp = new DIYButton(StringUtil.getUiLabel("pcw.levelup"));
		levelUp.addActionListener(this);
		levelUp.setTooltip(StringUtil.getUiLabel("pcw.levelup.tooltip"));

		// actions
		MutableTree<ActorActionOption> options = new HashMapMutableTree<>();
		action = new DIYComboBox<>(options, new Rectangle(0, 0, 1, 1));
		action.setEditorText(StringUtil.getUiLabel("pcw.take.an.action", ""));
		if (index % 2 == 0)
		{
			action.setPopupDirection(DIYComboBox.PopupDirection.RIGHT);
			action.setPopupExpansionDirection(DIYComboBox.PopupExpansionDirection.RIGHT);
		}
		else
		{
			action.setPopupDirection(DIYComboBox.PopupDirection.LEFT);
			action.setPopupExpansionDirection(DIYComboBox.PopupExpansionDirection.LEFT);
		}
		action.addActionListener(this);

		// stances
		ArrayList<PlayerCharacter.Stance> stances = new ArrayList<>();
		stance = new DIYComboBox<>(stances, new Rectangle(0, 0, 1, 1));
		stance.addActionListener(this);

		add(portraitFrame);
		add(portrait);
//		add(nameBoard);
		add(nameLabel);
		add(classLabel);
		add(leftHandSlot);
		add(rightHandSlot);
		add(leftHandItem);
		add(rightHandItem);
		add(hpBar);
		add(apBar);
		add(mpBar);

		add(action);
//		add(stance); todo: stance widget
		add(levelUp);
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public void doLayout()
	{
		RendererProperties rp = DIYToolkit.getInstance().getRendererProperties();

		int border = rp.getProperty(RendererProperties.Property.PANEL_LIGHT_BORDER);
		int inset = rp.getProperty(RendererProperties.Property.INSET);

		int twoThirdsHeight = height*2/3;

		// portrait frame
		BufferedImage pf = rp.getImageResource("pcw/portrait_frame");
		portraitFrame.setIcon(pf);
		portraitFrame.setBounds(new Rectangle(
			x +border +inset,
			y +border +inset,
			pf.getWidth(),
			pf.getHeight()));

		// portrait bounds
		int portraitHeight = rp.getProperty(RendererProperties.Property.PCW_PORTRAIT_HEIGHT);
		int portraitWidth = rp.getProperty(RendererProperties.Property.PCW_PORTRAIT_WIDTH);
		int portraitFrameBorder = rp.getProperty(RendererProperties.Property.PCW_PORTRAIT_FRAME_BORDER);
		Rectangle portraitBounds = new Rectangle(
			portraitFrame.x +portraitFrame.width/2 -portraitWidth/2,
			portraitFrame.y +portraitFrame.height -portraitFrameBorder -portraitHeight,
			portraitWidth,
			portraitHeight);

		this.portrait.setBounds(portraitBounds);
		this.portraitBounds = portraitBounds;

		// hand icon bounds
		int handSize = rp.getProperty(RendererProperties.Property.ITEM_WIDGET_SIZE);
		rightHandBounds = new Rectangle(
			portraitFrame.x +portraitFrame.width +inset/2,
			portraitFrame.y +portraitFrame.height -inset/2,
			handSize,
			handSize);
		leftHandBounds = new Rectangle(
			rightHandBounds.x +rightHandBounds.width +inset/2,
			rightHandBounds.y,
			handSize,
			handSize);

		leftHandSlot.setBounds(leftHandBounds);
		rightHandSlot.setBounds(rightHandBounds);

		leftHandItem.setBounds(leftHandSlot.getBounds());
		rightHandItem.setBounds(rightHandSlot.getBounds());

		// resource bars
		int startX = rightHandBounds.x;
		int barTop = portraitFrame.y;
		int barWidth = (width -portraitFrame.width -inset/2 -inset*4 -border*2) /3;
		int barHeight = portraitFrame.height -inset;

		hpBarBounds = new Rectangle(startX, barTop, barWidth, barHeight);
		startX += (barWidth+ inset);
		apBarBounds = new Rectangle(startX, barTop, barWidth, barHeight);
		startX += (barWidth+ inset);
		mpBarBounds = new Rectangle(startX, barTop, barWidth, barHeight);

		hpBar.setBounds(hpBarBounds);
		apBar.setBounds(apBarBounds);
		mpBar.setBounds(mpBarBounds);

		// name and class labels

		int nameLabelHeight = 15;
		BufferedImage nb = rp.getImageResource("pcw/name_board");
		nameBoard.setIcon(nb);
		nameBoard.setBounds(
			x +border +inset,
			y +border +inset +portraitFrame.height +inset,
			portraitFrame.width,
			nameLabelHeight*2);

		nameLabelBounds = new Rectangle(
			nameBoard.x,
			nameBoard.y,
			nameBoard.width,
			nameLabelHeight);
		classLabelBounds = new Rectangle(
			nameBoard.x,
			nameBoard.y +nameLabelHeight,
			nameBoard.width,
			nameLabelHeight);
		nameLabel.setBounds(nameLabelBounds);
		classLabel.setBounds(classLabelBounds);

		// todo: conditions
		conditionsArea = new Rectangle(
			x + border,
			y + border,
			handSize,
			twoThirdsHeight-border);

		// action button bounds
		Rectangle actionBounds = new Rectangle(
			x + border +inset,
			classLabelBounds.y +classLabelBounds.height +inset,
			width -border*2 -inset*2,
			height -border*2 -portraitFrame.height -nameLabelHeight*2 -inset*4);
		getAction().setBounds(actionBounds);
		getAction().setVisible(false);

		// todo: stance button bounds
		Rectangle stanceBounds = new Rectangle(
			x + border,
			y + border + portraitHeight + nameLabelHeight*2 + inset*2,
			width -border*2 -handSize -inset*2,
			25 -inset);
		getStance().setBounds(stanceBounds);
		getStance().setVisible(false);

		// lvl up button
		Dimension ps = levelUp.getPreferredSize();
		levelUp.setBounds(
			x +border +inset,
			y +border +inset,
			ps.width,
			ps.height);
		levelUp.setVisible(false);
	}

	/*-------------------------------------------------------------------------*/
//	public String getWidgetName()
//	{
//		return MazeRendererFactory.PLAYER_CHARACTER_WIDGET;
//	}

	/*-------------------------------------------------------------------------*/
	public void setPlayerCharacter(PlayerCharacter playerCharacter)
	{
		synchronized (pcMutex)
		{
			this.playerCharacter = playerCharacter;
		}
		refresh();
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		refreshStates();
	}

	private void refreshStates()
	{
		boolean thisEnabled = this.isEnabled();
		if (playerCharacter == null)
		{
			portrait.setIcon(null);
			nameLabel.setVisible(false);
			classLabel.setVisible(false);
			leftHandSlot.setVisible(false);
			rightHandSlot.setVisible(false);
			leftHandItem.setVisible(false);
			rightHandItem.setVisible(false);
			hpBar.setVisible(false);
			apBar.setVisible(false);
			mpBar.setVisible(false);

			levelUp.setVisible(false);
			action.setVisible(false);
			stance.setVisible(false);
			return;
		}
		else
		{
			nameLabel.setVisible(true);
			classLabel.setVisible(true);
			leftHandSlot.setVisible(true);
			rightHandSlot.setVisible(true);
			leftHandItem.setVisible(true);
			rightHandItem.setVisible(true);
			hpBar.setVisible(true);
			apBar.setVisible(true);
			mpBar.setVisible(true);

			Combat combat = Maze.getInstance().getCurrentCombat();
			action.setVisible(true);
			action.setEnabled(thisEnabled && !action.getModel().isEmpty() && !(action.getModel().size() == 1));

			stance.setVisible(true);
			stance.setEnabled(thisEnabled && !stance.getModel().isEmpty() && !(stance.getModel().size() == 1));

			if (playerCharacter.isLevelUpPending())
			{
				levelUp.setVisible(true);
				levelUp.setEnabled(true);
			}
			else
			{
				levelUp.setVisible(false);
				levelUp.setEnabled(false);
			}

			if (Maze.getInstance().getState() == Maze.State.MOVEMENT ||
				Maze.getInstance().getState() == Maze.State.COMBAT)
			{
				if (combat == null)
				{
					action.setEditorText(StringUtil.getUiLabel("pcw.take.an.action", playerCharacter.getDisplayName()));
					action.setEnabled(!action.getModel().isEmpty());

					stance.setEnabled(false);
				}
				else
				{
					action.setEditorText(null);
					action.getSelected().select(playerCharacter, combat, this);

					stance.setEnabled(thisEnabled);
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void refresh()
	{
		boolean thisEnabled = this.isEnabled();

		conditionBounds = new HashMap<>();

		if (playerCharacter == null)
		{
			portrait.setIcon(null);
			nameLabel.setVisible(false);
			classLabel.setVisible(false);

			leftHandSlot.setVisible(false);
			rightHandSlot.setVisible(false);
			leftHandItem.setVisible(false);
			rightHandItem.setVisible(false);

			hpBar.setVisible(false);
			apBar.setVisible(false);
			mpBar.setVisible(false);

			levelUp.setVisible(false);
			action.setVisible(false);
			stance.setVisible(false);
			return;
		}
		else
		{
			portrait.setIcon(Database.getInstance().getImage(playerCharacter.getPortrait()));

			nameLabel.setVisible(true);
			classLabel.setVisible(true);
			nameLabel.setText(playerCharacter.getDisplayName());
			String raceClassText = playerCharacter.getRace().getName() + " " + playerCharacter.getCharacterClass().getName();
			FontMetrics fm = DIYToolkit.getInstance().getComponent().getFontMetrics(DiyGuiUserInterface.instance.getDefaultFont());
			if (fm.stringWidth(raceClassText) > classLabel.width)
			{
				// too long, just use the class name
				raceClassText = playerCharacter.getCharacterClass().getName();
			}
			classLabel.setText(raceClassText);

			// resource bars
			hpBar.setVisible(true);
			apBar.setVisible(true);
			mpBar.setVisible(true);

			CurMaxSub hp = playerCharacter.getHitPoints();
			CurMax ap = playerCharacter.getActionPoints();
			CurMax mp = playerCharacter.getMagicPoints();

			hpBar.set(hp.getCurrent(), hp.getMaximum(), hp.getSub());
			apBar.set(ap.getCurrent(), ap.getMaximum());
			mpBar.set(mp.getCurrent(), mp.getMaximum());

			if (hp.getSub() > 0)
			{
				hpBar.setTooltip(StringUtil.getUiLabel("pcw.hp.fatigue.tooltip", hp.getCurrent(), hp.getMaximum(), hp.getSub()));
			}
			else
			{
				hpBar.setTooltip(StringUtil.getUiLabel("pcw.hp.tooltip", hp.getCurrent(), hp.getMaximum()));
			}

			apBar.setTooltip(StringUtil.getUiLabel("pcw.ap.tooltip", ap.getCurrent(), ap.getMaximum()));
			mpBar.setTooltip(StringUtil.getUiLabel("pcw.mp.tooltip", mp.getCurrent(), mp.getMaximum()));

			// left hand item
			if (playerCharacter.getSecondaryWeapon() != null)
			{
				leftHandItem.setIcon(Database.getInstance().getImage(playerCharacter.getSecondaryWeapon().getImage()));
				leftHandItem.setTooltip(playerCharacter.getSecondaryWeapon().getDisplayName());
			}
			else
			{
				leftHandItem.setIcon(Database.getInstance().getImage(playerCharacter.getLeftHandIcon()));
				leftHandItem.setTooltip(null);
			}

			// right hand item
			if (playerCharacter.getPrimaryWeapon() != null)
			{
				rightHandItem.setIcon(Database.getInstance().getImage(playerCharacter.getPrimaryWeapon().getImage()));
				rightHandItem.setTooltip(playerCharacter.getPrimaryWeapon().getDisplayName());
			}
			else
			{
				rightHandItem.setIcon(Database.getInstance().getImage(playerCharacter.getRightHandIcon()));
				rightHandItem.setTooltip(null);
			}

			leftHandSlot.setVisible(true);
			rightHandSlot.setVisible(true);
			leftHandItem.setVisible(true);
			rightHandItem.setVisible(true);

			Combat combat = Maze.getInstance().getCurrentCombat();
			action.setModel(playerCharacter.getCharacterActionOptions(Maze.getInstance(), combat));
			action.setVisible(true);
			action.setEnabled(thisEnabled && !action.getModel().isEmpty() && !(action.getModel().size() == 1));

			stance.setModel(playerCharacter.getCharacterStanceOptions(Maze.getInstance(), combat));
			stance.setVisible(true);
			stance.setEnabled(thisEnabled && !stance.getModel().isEmpty() && !(stance.getModel().size() == 1));

			if (playerCharacter.isLevelUpPending())
			{
				levelUp.setVisible(true);
				levelUp.setEnabled(true);
			}
			else
			{
				levelUp.setVisible(false);
				levelUp.setEnabled(false);
			}

			if (Maze.getInstance().getState() == Maze.State.MOVEMENT ||
				Maze.getInstance().getState() == Maze.State.COMBAT)
			{
				if (combat == null)
				{
					action.setEditorText(StringUtil.getUiLabel("pcw.take.an.action", playerCharacter.getDisplayName()));
					action.setEnabled(!action.getModel().isEmpty());

					stance.setEnabled(false);
				}
				else
				{
					action.setEditorText(null);
					action.getSelected().select(playerCharacter, combat, this);

					stance.setEnabled(thisEnabled);
				}
			}

			this.getPlayerCharacter().setStance(stance.getSelected());
		}
	}

	/*-------------------------------------------------------------------------*/
	public void processMouseClicked(MouseEvent e)
	{
		// this shit only works in movement mode
		if (Maze.getInstance().getState() == Maze.State.MOVEMENT)
		{
			handleMovementModeMouseClick(e);
		}
		else if (Maze.getInstance().getState() == Maze.State.COMBAT)
		{
			// nope
		}
	}

	/*-------------------------------------------------------------------------*/
	private void handleMovementModeMouseClick(MouseEvent e)
	{
		inventory();
//		super.processMouseClicked(e);

/*
		if (leftHandBounds.contains(e.getPoint())
			|| rightHandBounds.contains(e.getPoint()))
		{
			handleHandWidgetClick(e);
		}
		else if (!popupConditionDialog(e.getPoint()))
		{
			inventory();
			super.processMouseClicked(e);
		}
*/
	}

	/*-------------------------------------------------------------------------*/
	private void handleHandWidgetClick(MouseEvent e)
	{
		if (e.getButton() == MouseEvent.BUTTON3)
		{
			Item item;
			// right click to bring up item details
			if (rightHandBounds.contains(e.getPoint()))
			{
				item = playerCharacter.getPrimaryWeapon();
			}
			else
			{
				item = playerCharacter.getSecondaryWeapon();
			}

			if (item != null)
			{
				DiyGuiUserInterface.instance.popupItemDetailsWidget(item);
			}
		}
		else
		{
			// interpret any other click on the weapons as a swap
			playerCharacter.swapWeapons();
			refresh();
		}
	}

	/*-------------------------------------------------------------------------*/
	private boolean popupConditionDialog(Point p)
	{
		for (Map.Entry<Rectangle, Condition> e : conditionBounds.entrySet())
		{
			if (e.getKey().contains(p))
			{
				DiyGuiUserInterface.instance.popupConditionDetailsDialog(e.getValue());
				return true;
			}
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		Object source = event.getSource();
		if (source == levelUp && Maze.getInstance().getState() == Maze.State.MOVEMENT)
		{
			Maze.getInstance().levelUp(this.playerCharacter);
			return true;
		}
		else if (source == action)
		{
			ActorActionOption selected = action.getSelected();

			if (Maze.getInstance().getState() == Maze.State.MOVEMENT)
			{
				selected.select(this.getPlayerCharacter(), null, this);
				return true;
			}
			else
			{
				selected.select(this.getPlayerCharacter(), Maze.getInstance().getCurrentCombat(), this);
				return true;
			}
		}
		else if (source == stance)
		{
			this.getPlayerCharacter().setStance(stance.getSelected());
			return true;
		}
		else if (event.getEvent() instanceof MouseEvent &&
			(source == leftHandItem || source == leftHandSlot ||
			source == rightHandItem || source == rightHandSlot))
		{
			handleHandWidgetClick((MouseEvent)event.getEvent());
		}
		else if (Maze.getInstance().getState() == Maze.State.MOVEMENT)
		{
			// anything else = dive into the inventory
			inventory();
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	public Object getPcMutex()
	{
		return pcMutex;
	}

	/*-------------------------------------------------------------------------*/
	public PlayerCharacter getPlayerCharacter()
	{
		return playerCharacter;
	}

	/*-------------------------------------------------------------------------*/
	public DIYButton getLevelUp()
	{
		return levelUp;
	}

	/*-------------------------------------------------------------------------*/
	public int getIndex()
	{
		return index;
	}

	/*-------------------------------------------------------------------------*/
	public DIYComboBox<ActorActionOption> getAction()
	{
		return action;
	}

	/*-------------------------------------------------------------------------*/
	public DIYComboBox<PlayerCharacter.Stance> getStance()
	{
		return stance;
	}

	/*-------------------------------------------------------------------------*/
	public void setLeftHandBounds(Rectangle leftHandBounds)
	{
		this.leftHandBounds = leftHandBounds;
	}

	/*-------------------------------------------------------------------------*/
	public void setRightHandBounds(Rectangle rightHandBounds)
	{
		this.rightHandBounds = rightHandBounds;
	}

	/*-------------------------------------------------------------------------*/
	public void setPortraitBounds(Rectangle portraitBounds)
	{
		this.portrait.setBounds(portraitBounds);
		this.portraitBounds = portraitBounds;
	}

	/*-------------------------------------------------------------------------*/
	public Rectangle getPortraitBounds()
	{
		return portraitBounds;
	}

	public Rectangle getLeftHandBounds()
	{
		return leftHandBounds;
	}

	public Rectangle getRightHandBounds()
	{
		return rightHandBounds;
	}

	public Rectangle getNameLabelBounds()
	{
		return nameLabelBounds;
	}

	public Rectangle getClassLabelBounds()
	{
		return classLabelBounds;
	}

	public Rectangle getHpBarBounds()
	{
		return hpBarBounds;
	}

	public Rectangle getApBarBounds()
	{
		return apBarBounds;
	}

	public Rectangle getMpBarBounds()
	{
		return mpBarBounds;
	}

	public Rectangle getConditionsArea()
	{
		return conditionsArea;
	}

	/*-------------------------------------------------------------------------*/
	public void clearConditionBounds()
	{
		conditionBounds.clear();
	}

	/*-------------------------------------------------------------------------*/
	public void addConditionBounds(Rectangle r, Condition c)
	{
		conditionBounds.put(r, c);
	}

	/*-------------------------------------------------------------------------*/
	public void selected(ActorActionIntention intention)
	{
		if (Maze.getInstance().getState() != Maze.State.COMBAT)
		{
			if (intention instanceof EquipIntention)
			{
				inventory();
			}
			else
			{
				GameSys.getInstance().processPlayerCharacterIntentionOutsideCombat(
					intention, playerCharacter);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void inventory()
	{
		Maze.getInstance().getUi().characterSelected(this.playerCharacter);
		Maze.getInstance().setState(Maze.State.INVENTORY);
	}
}
