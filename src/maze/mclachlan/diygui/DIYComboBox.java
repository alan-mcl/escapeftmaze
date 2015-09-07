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

package mclachlan.diygui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.*;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.diygui.util.HashMapMutableTree;
import mclachlan.diygui.util.MutableTree;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class DIYComboBox<T> extends ContainerWidget
{
	/** If this is set the editor always display this text */
	private String editorText;
	private Stack<T> selected = new Stack<T>();
	private MutableTree<T> model;

	private EditorState editorState = EditorState.DEFAULT;
	private PopupState popupState = PopupState.HIDDEN;
	private DIYToolkit.Align align = DIYToolkit.Align.CENTER;
	private Stack<DIYPane> popupPanes = new Stack<DIYPane>();

	private PopupDirection popupDirection = PopupDirection.DOWN;
	private PopupExpansionDirection popupExpansionDirection = PopupExpansionDirection.RIGHT;

	public enum EditorState
	{
		DEFAULT, HOVER, DEPRESSED
	}

	public enum PopupState
	{
		HIDDEN, VISIBLE
	}

	public enum ComboItemState
	{
		DEFAULT, HOVER
	}

	/**
	 * The direction of the first popup when the editor is selected
	 */
	public enum PopupDirection
	{
		UP, DOWN, RIGHT, LEFT
	}

	/**
	 * The way that multi-level popups expand
	 */
	public enum PopupExpansionDirection
	{
		RIGHT, LEFT
	}

	/*-------------------------------------------------------------------------*/
	public DIYComboBox(List<T> items, Rectangle bounds)
	{
		super(bounds);

		HashMapMutableTree<T> model = new HashMapMutableTree<T>();

		for (T t : items)
		{
			model.add(t, null);
		}

		setModel(model);
	}

	/*-------------------------------------------------------------------------*/
	public DIYComboBox(MutableTree<T> model, Rectangle bounds)
	{
		super(bounds);
		setModel(model);
	}

	/*-------------------------------------------------------------------------*/
	public MutableTree<T> getModel()
	{
		return model;
	}

	/*-------------------------------------------------------------------------*/
	public void setModel(MutableTree<T> model)
	{
		this.model = model;
		if (model.getRoots().size() > 0)
		{
			setSelected(getModel().getRoots().get(0));
		}
	}

	/*-------------------------------------------------------------------------*/
	public T getSelected()
	{
		if (selected.isEmpty())
		{
			return null;
		}
		else
		{
			return selected.peek();
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setSelected(T selected)
	{
		this.selected.push((T)selected);
	}

	/*-------------------------------------------------------------------------*/
	public int getPopupLevel()
	{
		return selected.size()-1;
	}

	public String getEditorText()
	{
		return editorText;
	}

	public void setEditorText(String editorText)
	{
		this.editorText = editorText;
	}

	/*-------------------------------------------------------------------------*/
	public EditorState getEditorState()
	{
		return editorState;
	}

	/*-------------------------------------------------------------------------*/
	public PopupState getPopupState()
	{
		return popupState;
	}

	/*-------------------------------------------------------------------------*/
	public DIYToolkit.Align getAlignment()
	{
		return align;
	}

	/*-------------------------------------------------------------------------*/
	public PopupExpansionDirection getPopupExpansionDirection()
	{
		return popupExpansionDirection;
	}

	/*-------------------------------------------------------------------------*/
	public void setPopupExpansionDirection(PopupExpansionDirection popupExpansionDirection)
	{
		this.popupExpansionDirection = popupExpansionDirection;
	}

	/*-------------------------------------------------------------------------*/
	public PopupDirection getPopupDirection()
	{
		return popupDirection;
	}

	/*-------------------------------------------------------------------------*/
	public void setPopupDirection(PopupDirection popupDirection)
	{
		this.popupDirection = popupDirection;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void processMousePressed(MouseEvent e)
	{
		if (!isEnabled())
		{
			return;
		}
		this.editorState = EditorState.DEPRESSED;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void processMouseReleased(MouseEvent e)
	{
		if (!isEnabled())
		{
			return;
		}
		this.editorState = EditorState.HOVER;

		if (this.popupState == PopupState.HIDDEN)
		{
			DIYToolkit.getInstance().setOverlayPane(new DIYPane(
				DIYToolkit.getInstance().getContentPane().getBounds())
			{
				@Override
				public void processMouseReleased(MouseEvent e)
				{
					DIYComboBox.this.hideAllPopups();
					DIYToolkit.getInstance().setOverlayPane(null);
					DIYComboBox.this.editorState = EditorState.DEFAULT;
				}
			});
			showPopup(null);
		}
		else
		{
			hideAllPopups();
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void processMouseEntered(MouseEvent e)
	{
		if (!isEnabled())
		{
			return;
		}
		this.editorState = EditorState.HOVER;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void processMouseExited(MouseEvent e)
	{
		if (!isEnabled())
		{
			return;
		}
		this.editorState = EditorState.DEFAULT;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @param node
	 * 	The node who's children will be contained in this popup.
	 * 	<code>null</code> is the root.
	 */
	private void showPopup(T node)
	{
		popupState = PopupState.VISIBLE;

		List<T> options = getModel().getChildren(node);

		// if the depth of the node does not equal the current depth of the
		// popup stack, unwind the stack until it does
		int depth = getModel().getDepth(node);
		while (depth < popupPanes.size()-1)
		{
			hidePopup();
		}

		Rectangle popupBounds = getPopupBounds(node, options);

		DIYPane pane = new DIYPane(popupBounds);
		pane.setLayoutManager(new DIYGridLayout(1, options.size(), 1, 1));

		for (T t : options)
		{
			pane.add(new ComboItem(t));
		}

		DIYToolkit.getInstance().getOverlayPane().add(pane);
		DIYToolkit.getInstance().getOverlayPane().doLayout();
		popupPanes.push(pane);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @param node
	 * 	The node who's children will be contained in this popup.
	 * 	<code>null</code> is the root.
	 * @param options
	 * 	The list of options contained in this popup
	 */
	private Rectangle getPopupBounds(T node, List<T> options)
	{
		// calculate the next popup bounds
		int popupWidth = width; // todo: longer for subitems?
		int popupHeight = 20*options.size(); // todo: dynamic row height?
		int popupX, popupY;

		if (node == null)
		{
			// root node means it's the first popup
			switch (popupDirection)
			{
				case UP:
					popupX = x;
					popupY = y - popupHeight;
					break;
				case DOWN:
					popupX = x;
					popupY = y + height;
					break;
				case RIGHT:
					popupX = x + width;
					popupY = y - popupHeight/2;
					break;
				case LEFT:
					popupX = x - popupWidth;
					popupY = y - popupHeight/2;
					break;
				default:
					throw new MazeException(popupDirection.toString());
			}
		}
		else
		{
			// popup expansion

			DIYPane lastPane = popupPanes.peek();

			switch (popupExpansionDirection)
			{
				case RIGHT:
					popupX = lastPane.x + popupWidth;
					popupY = lastPane.y + lastPane.height/2 - popupHeight/2;
					break;
				case LEFT:
					popupX = lastPane.x - popupWidth;
					popupY = lastPane.y + lastPane.height/2 - popupHeight/2;
					break;
				default:
					throw new MazeException(popupExpansionDirection.toString());
			}
		}

		return new Rectangle(popupX, popupY, popupWidth, popupHeight);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Hides the highest level popup
	 */
	private void hidePopup()
	{
		DIYToolkit.getInstance().getOverlayPane().remove(popupPanes.pop());
		selected.pop();
		if (popupPanes.isEmpty())
		{
			popupState = PopupState.HIDDEN;
			DIYToolkit.getInstance().setOverlayPane(null);
		}
	}

	/*-------------------------------------------------------------------------*/

	private void hideAllPopups()
	{
		T current = getSelected();
		while (!popupPanes.isEmpty())
		{
			hidePopup();
		}
		selected.clear();
		selected.push(current);
		this.editorState = EditorState.DEFAULT;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getWidgetName()
	{
		return DIYToolkit.COMBO_BOX;
	}

	/*-------------------------------------------------------------------------*/
	public class ComboItem extends Widget
	{
		private T item;
		private ComboItemState state;

		/*----------------------------------------------------------------------*/
		public ComboItem(T item)
		{
			super(0, 0, 1, 1);
			this.item = item;
			this.state = ComboItemState.DEFAULT;
		}

		/*----------------------------------------------------------------------*/
		public Dimension getPreferredSize()
		{
			Dimension dimension = DIYToolkit.getDimension(this.item.toString());
			dimension.setSize(dimension.getWidth(), dimension.getHeight());
			return dimension;
		}

		/*----------------------------------------------------------------------*/
		public String getWidgetName()
		{
			// the least of the hacks going on here
			return DIYToolkit.COMBO_ITEM;
		}

		/*----------------------------------------------------------------------*/
		public void processMouseClicked(MouseEvent e)
		{
			if (isEnabled())
			{
				DIYComboBox.this.setSelected(this.item);
				if (DIYComboBox.this.getModel().getChildren((T)this.item).isEmpty())
				{
					DIYComboBox.this.hideAllPopups();
				}
				else
				{
					DIYComboBox.this.showPopup(this.item);
				}
			}

			// dodgy hack to ensure that the parent ActionListeners see this
			DIYComboBox.this.processMouseClicked(e);
		}

		/*-------------------------------------------------------------------------*/
		public void processMouseEntered(MouseEvent e)
		{
			if (isEnabled())
			{
				this.state = ComboItemState.HOVER;
			}
		}

		/*-------------------------------------------------------------------------*/
		public void processMouseExited(MouseEvent e)
		{
			this.state = ComboItemState.DEFAULT;
		}

		/*----------------------------------------------------------------------*/
		public DIYComboBox getParent()
		{
			return DIYComboBox.this;
		}

		/*----------------------------------------------------------------------*/
		public Object getItem()
		{
			return item;
		}

		/*-------------------------------------------------------------------------*/
		public ComboItemState getState()
		{
			return state;
		}

		/*-------------------------------------------------------------------------*/
		public String toString()
		{
			return item.toString();
		}
	}
}
