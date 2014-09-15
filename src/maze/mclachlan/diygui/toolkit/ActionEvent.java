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

package mclachlan.diygui.toolkit;

import java.awt.event.InputEvent;

/**
 *
 */
public class ActionEvent
{
	private String message;
	private Object source;
	private Object payload;
	private InputEvent event;

	public ActionEvent(Object source, Object payload, String message, InputEvent event)
	{
		this.payload = payload;
		this.event = event;
		this.message = message;
		this.source = source;
	}

	public Object getPayload()
	{
		return payload;
	}

	public InputEvent getEvent()
	{
		return event;
	}

	public String getMessage()
	{
		return message;
	}

	public Object getSource()
	{
		return source;
	}
}
